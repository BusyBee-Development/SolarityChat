package org.busybee.solaritychat.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.busybee.solaritychat.SolarityChat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ColorGUIListener implements Listener {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ColorGUIListener(SolarityChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains("Chat Colors")) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> lore = meta.lore();
        if (lore == null || lore.size() < 3) return;

        String codeLine = PlainTextComponentSerializer.plainText().serialize(lore.get(1));
        String colorCode = codeLine.replace("Code: ", "").trim();

        String permLine = PlainTextComponentSerializer.plainText().serialize(lore.get(2));
        String permission = permLine.replace("Permission: ", "").trim();

        if (!player.hasPermission(permission)) {
            player.sendMessage(mm.deserialize("<red>You don't have permission for this color."));
            player.closeInventory();
            return;
        }

        if (colorCode.equalsIgnoreCase("reset")) {
            plugin.getColorManager().setPlayerColor(player.getUniqueId(), null);
            player.sendMessage(mm.deserialize("<green>Your chat color has been reset!"));
            player.closeInventory();
            return;
        }

        plugin.getColorManager().setPlayerColor(player.getUniqueId(), colorCode);
        player.sendMessage(mm.deserialize("<green>Chat color updated!"));
        player.closeInventory();
    }
}