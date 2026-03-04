package org.busybee.solaritychat.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.commands.ColorCommand;
import org.busybee.solaritychat.storage.ColorDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        PersistentDataContainer data = meta.getPersistentDataContainer();

        NamespacedKey pageKey = new NamespacedKey(plugin, "gui_page");
        if (data.has(pageKey, PersistentDataType.INTEGER)) {
            Integer targetPage = data.get(pageKey, PersistentDataType.INTEGER);
            if (targetPage != null && plugin.getCommand("colors").getExecutor() instanceof ColorCommand colorCommand) {
                colorCommand.openColorGUI(player, targetPage);
            }
            return;
        }

        NamespacedKey actionKey = new NamespacedKey(plugin, "gui_action");
        if (data.has(actionKey, PersistentDataType.STRING)) {
            String action = data.get(actionKey, PersistentDataType.STRING);
            if ("reset".equalsIgnoreCase(action)) {
                plugin.getColorManager().setPlayerColor(player.getUniqueId(), null);
                player.sendMessage(mm.deserialize("<green>Your chat color has been reset!"));
                player.closeInventory();
            }
            return;
        }

        NamespacedKey colorIdKey = new NamespacedKey(plugin, "color_id");
        String colorId = data.get(colorIdKey, PersistentDataType.STRING);

        if (colorId == null) return;

        ColorDefinition def = plugin.getColorManager().getColorDefinition(colorId);
        if (def == null) return;

        if (!player.hasPermission(def.permission())) {
            player.sendMessage(mm.deserialize("<red>You don't have permission for this color."));
            return;
        }

        plugin.getColorManager().setPlayerColor(player.getUniqueId(), colorId);
        player.sendMessage(mm.deserialize("<green>Chat color updated!"));
        player.closeInventory();
    }
}
