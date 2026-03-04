package org.busybee.solaritychat.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.channels.ChannelManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChannelCommand implements CommandExecutor, TabCompleter {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ChannelCommand(SolarityChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!plugin.getChannelManager().isEnabled()) {
            sender.sendMessage(mm.deserialize("<red>Channels are currently disabled."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(mm.deserialize("<gold>Available Channels:"));
            for (ChannelManager.ChatChannel channel : plugin.getChannelManager().getAvailableChannels()) {
                if (channel.getPermission().isEmpty() || player.hasPermission(channel.getPermission())) {
                    player.sendMessage(mm.deserialize("<gray>- <yellow>" + channel.getName() + " <gray>(/" + label + " " + channel.getId() + ")"));
                }
            }
            return true;
        }

        String channelId = args[0].toLowerCase();
        ChannelManager.ChatChannel channel = plugin.getChannelManager().getChannel(channelId);

        if (channel == null) {
            player.sendMessage(mm.deserialize("<red>Channel not found."));
            return true;
        }

        if (!channel.getPermission().isEmpty() && !player.hasPermission(channel.getPermission())) {
            player.sendMessage(mm.deserialize("<red>You don't have permission to join this channel."));
            return true;
        }

        plugin.getChannelManager().setPlayerChannel(player, channelId);
        player.sendMessage(mm.deserialize("<green>You are now chatting in the <yellow>" + channel.getName() + "</yellow> channel."));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!plugin.getChannelManager().isEnabled()) {
            return List.of();
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && sender instanceof Player player) {
            String input = args[0].toLowerCase();
            for (ChannelManager.ChatChannel channel : plugin.getChannelManager().getAvailableChannels()) {
                if (channel.getPermission().isEmpty() || player.hasPermission(channel.getPermission())) {
                    if (channel.getId().startsWith(input)) {
                        completions.add(channel.getId());
                    }
                }
            }
        }
        return completions;
    }
}
