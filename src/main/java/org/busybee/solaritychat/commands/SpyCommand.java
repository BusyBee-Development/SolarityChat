package org.busybee.solaritychat.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpyCommand implements CommandExecutor {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SpyCommand(SolarityChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("solaritychat.spy")) {
            player.sendMessage(mm.deserialize("<red>You do not have permission to use this command."));
            return true;
        }

        plugin.getSpyManager().toggleSpy(player.getUniqueId());
        
        if (plugin.getSpyManager().isSpyEnabled(player.getUniqueId())) {
            player.sendMessage(mm.deserialize("<green>Spy mode enabled. You will now see private messages."));
        } else {
            player.sendMessage(mm.deserialize("<red>Spy mode disabled. You will no longer see private messages."));
        }

        return true;
    }
}
