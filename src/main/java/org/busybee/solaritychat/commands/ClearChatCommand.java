package org.busybee.solaritychat.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearChatCommand implements CommandExecutor {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("solaritychat.clearchat")) {
            sender.sendMessage(mm.deserialize("<red>You do not have permission to use this command."));
            return true;
        }

        Component blankLine = Component.text(" ");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("solaritychat.clearchat")) {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage(blankLine);
                }
            }
        }

        Bukkit.broadcast(mm.deserialize("<gray>Chat has been cleared by <yellow>" + sender.getName() + "</yellow>."));
        return true;
    }
}
