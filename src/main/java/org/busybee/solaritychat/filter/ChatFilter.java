package org.busybee.solaritychat.filter;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class ChatFilter {

    protected final SolarityChat plugin;
    protected final String name;
    protected final ConfigurationSection config;
    protected final List<String> actions;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public ChatFilter(SolarityChat plugin, String name, ConfigurationSection config) {
        this.plugin = plugin;
        this.name = name;
        this.config = config;
        this.actions = config.getStringList("actions");
    }

    public abstract boolean check(Player player, String message);
    public String getName() {
        return name;
    }
    public List<String> getActions() {
        return actions;
    }

    protected void executeActions(Player player, String message) {
        for (String action : actions) {
            processAction(player, message, action);
        }
    }

    private void processAction(Player player, String message, String action) {
        String actionUpper = action.toUpperCase();

        if (actionUpper.equals("CANCEL_MESSAGE")) {
            return;
        } else if (actionUpper.equals("TELL_PLAYER")) {
            String msg = plugin.getConfigManager().getMessage("player-violation");
            player.sendMessage(MM.deserialize(MessageUtil.legacyToMini(msg)));
        } else if (actionUpper.equals("NOTIFY_STAFF")) {
            notifyStaff(player, message);
        } else if (actionUpper.equals("DISCORD_WEBHOOK")) {
            sendDiscordWebhook(player, message);
        } else if (actionUpper.startsWith("INCREMENT_WARNINGS:")) {
            String[] parts = action.split(":");
            if (parts.length == 2) {
                try {
                    int amount = Integer.parseInt(parts[1].trim());
                    plugin.getWarningManager().addWarnings(player.getUniqueId(), player.getName(), amount);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid warning amount in filter " + name + ": " + parts[1]);
                }
            }
        } else if (actionUpper.startsWith("RUN_COMMANDS:")) {
            String commandStr = action.substring(action.indexOf("[") + 1, action.lastIndexOf("]"));
            String[] commands = commandStr.split(",");
            for (String command : commands) {
                String processedCommand = command.trim()
                        .replace("%player%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .replace("%message%", sanitizeCommand(message))
                        .replace("%filter_name%", name);
                Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand)
                );
            }
        }
    }

    private String sanitizeCommand(String input) {
        return input.replaceAll("[;&|`$]", "");
    }

    private void notifyStaff(Player player, String message) {
        String alertMessage = plugin.getConfigManager().getMessage("staff-alert")
                .replace("%player%", player.getName())
                .replace("%filter_name%", name)
                .replace("%message%", message);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("solaritychat.alerts")) {
                staff.sendMessage(MM.deserialize(MessageUtil.legacyToMini(alertMessage)));
            }
        }
    }

    private void sendDiscordWebhook(Player player, String message) {
        String webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        if (webhookUrl.isEmpty()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                org.busybee.solaritychat.util.DiscordWebhook webhook = new org.busybee.solaritychat.util.DiscordWebhook(webhookUrl);

                String actionType = determineActionType();
                String actionVerb = getActionVerb(actionType);

                String title = plugin.getConfig().getString("discord.embed.title", "%action_type%")
                        .replace("%action_type%", actionType)
                        .replace("%action_verb%", actionVerb)
                        .replace("%player%", player.getName())
                        .replace("%message%", message)
                        .replace("%filter_name%", name);

                String description = plugin.getConfig().getString("discord.embed.description", "%player% has been %action_verb% because they said `%message%`")
                        .replace("%action_type%", actionType)
                        .replace("%action_verb%", actionVerb)
                        .replace("%player%", player.getName())
                        .replace("%message%", message)
                        .replace("%filter_name%", name);

                String footer = plugin.getConfig().getString("discord.embed.footer", "");
                if (!footer.isEmpty()) {
                    footer = footer
                            .replace("%action_type%", actionType)
                            .replace("%action_verb%", actionVerb)
                            .replace("%player%", player.getName())
                            .replace("%message%", message)
                            .replace("%filter_name%", name);
                }

                String color = plugin.getConfig().getString("discord.embed.color", "#3498db");

                String timestampFormat = plugin.getConfig().getString("discord.embed.timestamp-format", "");
                String timestamp = "";
                if (!timestampFormat.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
                    timestamp = sdf.format(new Date());
                }

                StringBuilder fullDescription = new StringBuilder(description);
                if (!footer.isEmpty()) {
                    fullDescription.append("\n").append(footer);
                }
                if (!timestamp.isEmpty()) {
                    fullDescription.append("\n\n").append(timestamp);
                }

                webhook.setUsername("SolarityChat");
                webhook.addEmbed(new org.busybee.solaritychat.util.DiscordWebhook.EmbedObject()
                        .setTitle(title)
                        .setDescription(fullDescription.toString())
                        .setColor(Color.decode(color)));
                webhook.execute();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    private String determineActionType() {
        for (String action : actions) {
            String actionUpper = action.toUpperCase();
            if (actionUpper.startsWith("RUN_COMMANDS:")) {
                String commandStr = action.substring(action.indexOf("[") + 1, action.lastIndexOf("]"));
                String commandLower = commandStr.toLowerCase();
                if (commandLower.contains("mute")) {
                    return "AUTOMUTE";
                } else if (commandLower.contains("ban")) {
                    return "AUTOBAN";
                } else if (commandLower.contains("kick")) {
                    return "AUTOKICK";
                } else if (commandLower.contains("warn")) {
                    return "AUTOWARN";
                }
            }
        }
        return "AUTOMUTE";
    }

    private String getActionVerb(String actionType) {
        return switch (actionType) {
            case "AUTOBAN" -> "banned";
            case "AUTOKICK" -> "kicked";
            case "AUTOWARN" -> "warned";
            default -> "muted";
        };
    }
}
