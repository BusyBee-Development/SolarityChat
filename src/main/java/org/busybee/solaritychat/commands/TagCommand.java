package org.busybee.solaritychat.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.tags.TagGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TagCommand implements CommandExecutor {

    private final SolarityChat plugin;
    private final TagGUI tagGUI;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public TagCommand(SolarityChat plugin, TagGUI tagGUI) {
        this.plugin = plugin;
        this.tagGUI = tagGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("solaritychat.tags.use")) {
            player.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        tagGUI.openGUI(player);
        return true;
    }
}
