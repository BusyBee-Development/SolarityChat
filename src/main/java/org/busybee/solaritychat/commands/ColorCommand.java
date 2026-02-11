package org.busybee.solaritychat.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ColorCommand implements CommandExecutor {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ColorCommand(SolarityChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("solaritychat.colors")) {
            player.sendMessage(mm.deserialize("<red>You don't have permission to change your chat color."));
            return true;
        }

        openColorGUI(player);
        return true;
    }

    public void openColorGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, mm.deserialize("<gradient:#FFD700:#FFA500>Chat Colors</gradient>"));

        gui.setItem(10, createColorItem(Material.RED_WOOL, "<red>Red", "&c", "solaritychat.color.red"));
        gui.setItem(11, createColorItem(Material.BLUE_WOOL, "<blue>Blue", "&9", "solaritychat.color.blue"));
        gui.setItem(12, createColorItem(Material.GREEN_WOOL, "<green>Green", "&a", "solaritychat.color.green"));
        gui.setItem(13, createColorItem(Material.YELLOW_WOOL, "<yellow>Yellow", "&e", "solaritychat.color.yellow"));

        gui.setItem(14, createColorItem(Material.PINK_DYE, "<light_purple>Pink", "&#FF69B4", "solaritychat.color.pink"));
        gui.setItem(15, createColorItem(Material.CYAN_DYE, "<gradient:aqua:blue>Aqua Gradient</gradient>", "<gradient:aqua:blue>", "solaritychat.color.gradient.aqua"));
        gui.setItem(16, createColorItem(Material.ORANGE_DYE, "<gradient:gold:red>Fire Gradient</gradient>", "<gradient:gold:red>", "solaritychat.color.gradient.fire"));

        gui.setItem(22, createColorItem(Material.BARRIER, "<red>Reset Color", "reset", "solaritychat.colors"));

        player.openInventory(gui);
    }

    private ItemStack createColorItem(Material material, String name, String colorCode, String permission) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Click to select this color!"));
            lore.add(mm.deserialize("<dark_gray>Code: " + colorCode));
            lore.add(mm.deserialize("<dark_gray>Permission: " + permission));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}