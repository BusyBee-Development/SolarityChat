package org.busybee.solaritychat.listener;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.channels.ChannelManager;
import org.busybee.solaritychat.filter.FilterManager;
import org.busybee.solaritychat.format.FormatManager;
import org.busybee.solaritychat.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final SolarityChat plugin;
    private final Cache<UUID, Long> cooldowns;

    private boolean mentionEnabled;
    private String mentionReplacement;
    private String mentionSoundName;
    private float mentionSoundVolume;
    private float mentionSoundPitch;
    private final MiniMessage miniMessage;

    public ChatListener(SolarityChat plugin) {
        this.plugin = plugin;
        this.cooldowns = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
        this.miniMessage = MiniMessage.miniMessage();
        loadMentionConfig();
    }

    private void loadMentionConfig() {
        mentionEnabled = plugin.getConfig().getBoolean("chat.mention.enabled", false);
        mentionReplacement = plugin.getConfig().getString("chat.mention.replacement", "<yellow>@%player_name%</yellow>");
        mentionSoundName = plugin.getConfig().getString("chat.mention.sound.name", "BLOCK_NOTE_BLOCK_PLING").toUpperCase();
        mentionSoundVolume = (float) plugin.getConfig().getDouble("chat.mention.sound.volume", 1.0);
        mentionSoundPitch = (float) plugin.getConfig().getDouble("chat.mention.sound.pitch", 1.0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cooldowns.invalidate(uuid);
        plugin.getChannelManager().removePlayer(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());

        if (!player.hasPermission("solaritychat.grammar.bypass")) {
            plainMessage = plugin.getGrammarManager().process(plainMessage);
        }

        ChannelManager.ChatChannel channel = plugin.getChannelManager().getPlayerChannel(player);
        if (channel == null) {
            return;
        }

        if (channel.getRadius() > 0) {
            event.viewers().removeIf(audience -> {
                if (audience instanceof Player recipient) {
                    return !recipient.getWorld().equals(player.getWorld()) ||
                           recipient.getLocation().distance(player.getLocation()) > channel.getRadius();
                }
                return false;
            });
        } else if (!channel.getPermission().isEmpty()) {
            event.viewers().removeIf(audience -> {
                if (audience instanceof Player recipient) {
                    return !recipient.hasPermission(channel.getPermission());
                }
                return false;
            });
        }

        if (player.hasPermission("solaritychat.bypass")) {
            applyFormatting(event, player, plainMessage, channel);
            return;
        }

        FilterManager.FilterResult result = plugin.getFilterManager().checkMessage(player, plainMessage);
        if (result.isTriggered()) {
            event.setCancelled(true);
            if (!result.hasAction("RUN_COMMANDS")) {
                player.sendMessage(miniMessage.deserialize(MessageUtil.legacyToMini(plugin.getConfigManager().getMessage("player-violation"))));
            }
            return;
        }

        double cooldownTime = plugin.getConfig().getDouble("chat.cooldown", 0);
        if (cooldownTime > 0) {
            UUID uuid = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            Long lastMessage = cooldowns.getIfPresent(uuid);
            if (lastMessage != null) {
                double timePassed = (currentTime - lastMessage) / 1000.0;
                if (timePassed < cooldownTime) {
                    event.setCancelled(true);
                    String cooldownMsg = plugin.getConfigManager().getMessage("chat-cooldown")
                            .replace("%time%", String.format("%.1f", cooldownTime - timePassed));
                    player.sendMessage(miniMessage.deserialize(MessageUtil.legacyToMini(cooldownMsg)));
                    return;
                }
            }
            cooldowns.put(uuid, currentTime);
        }

        applyFormatting(event, player, plainMessage, channel);
    }

    private void applyFormatting(AsyncChatEvent event, Player player, String plainMessage, ChannelManager.ChatChannel channel) {
        var config = plugin.getConfigManager().getConfig("format");
        if (config == null || !config.getBoolean("enabled", true)) return;

        FormatManager.ChatFormat format = plugin.getFormatManager().getFormat(player);
        if (format == null) return;

        String customColor = plugin.getColorManager().getPlayerColor(player.getUniqueId());
        Component message;

        if (customColor != null) {
            String sanitized = sanitizeUserInput(plainMessage);
            if (customColor.startsWith("&#") && customColor.length() >= 8) {
                String hex = customColor.substring(1);
                message = miniMessage.deserialize("<color:" + hex + ">" + sanitized);
            } else if (customColor.startsWith("&") || customColor.startsWith("§")) {
                String miniColor = MessageUtil.legacyToMini(customColor);
                message = miniMessage.deserialize(miniColor + sanitized);
            } else if (customColor.startsWith("<")) {
                message = miniMessage.deserialize(customColor + sanitized);
            } else if (customColor.startsWith("#")) {
                message = miniMessage.deserialize("<color:" + customColor + ">" + sanitized);
            } else {
                message = Component.text(plainMessage);
            }
        } else {
            message = Component.text(plainMessage);
        }

        final Component finalMessage = message;
        final Component channelPrefix = MessageUtil.parse(channel.getPrefix() + " ");

        org.bukkit.Sound resolvedMentionSound = null;
        if (mentionEnabled) {
            try {
                resolvedMentionSound = org.bukkit.Sound.valueOf(mentionSoundName);
            } catch (IllegalArgumentException ignored) {
            }
        }
        final org.bukkit.Sound mentionSound = resolvedMentionSound;

        event.renderer((source, sourceDisplayName, msg, viewer) -> {
            Component formatted = plugin.getChatFormatter().format(source, format, finalMessage);
            formatted = channelPrefix.append(formatted);

            if (mentionEnabled && viewer instanceof Player recipient && mentionSound != null) {
                return handleMentions(formatted, recipient, source, mentionSound);
            }
            return formatted;
        });
    }

    private String sanitizeUserInput(String input) {
        return input
                .replace("<", "\\<")
                .replace(">", "\\>");
    }

    private Component handleMentions(Component message, Player recipient, Player sender, org.bukkit.Sound sound) {
        if (recipient.equals(sender)) return message;
        String recipientName = recipient.getName();
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);
        Pattern namePattern = Pattern.compile("\\b" + Pattern.quote(recipientName) + "\\b", Pattern.CASE_INSENSITIVE);

        if (namePattern.matcher(plainMessage).find()) {
            recipient.playSound(recipient.getLocation(), sound, mentionSoundVolume, mentionSoundPitch);
            String replacement = mentionReplacement.replace("%player_name%", recipientName);
            Component mentionComponent = miniMessage.deserialize(replacement);
            return message.replaceText(builder -> builder.match(namePattern).replacement(mentionComponent));
        }
        return message;
    }
}