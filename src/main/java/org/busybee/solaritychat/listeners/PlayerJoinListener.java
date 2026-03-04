package org.busybee.solaritychat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.util.MessageUtil;
import org.busybee.solaritychat.util.PlayerHeadUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener implements Listener {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlayerJoinListener(SolarityChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getColorManager().preloadPlayerColor(player.getUniqueId());

        String url = PlayerHeadUtil.getSkinUrl(player);

        CompletableFuture.supplyAsync(() -> {
            try {
                BufferedImage skin = PlayerHeadUtil.downloadSkin(url);
                return PlayerHeadUtil.getHeadLinesFromSkin(skin);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to download skin for " + player.getName() + ": " + e.getMessage());
                return Collections.<String>emptyList();
            }
        }).thenAccept(headLines -> {
            if (!player.isOnline()) return;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                var messagesConfig = plugin.getConfigManager().getConfig("messages");
                if (messagesConfig == null) return;

                List<String> motdLines = messagesConfig.getStringList("chatMotd");

                boolean hasPapi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

                for (int i = 0; i < Math.max(headLines.size(), motdLines.size()); i++) {
                    String headLine = (i < headLines.size()) ? headLines.get(i) : "";
                    String motdLine = (i < motdLines.size()) ? motdLines.get(i) : "";

                    Component headComponent = mm.deserialize(headLine);
                    String motdProcessed = hasPapi ? PlaceholderAPI.setPlaceholders(player, motdLine) : motdLine;
                    Component motdComponent = MessageUtil.parse(motdProcessed);

                    Component combinedLine = headComponent.append(Component.text(" ")).append(motdComponent);
                    player.sendMessage(combinedLine);
                }
            });
        });
    }
}
