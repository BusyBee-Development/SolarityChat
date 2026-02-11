package org.busybee.solaritychat.commands;

import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ColorCommand implements CommandExecutor {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private YamlConfiguration colorConfig;

    public ColorCommand(SolarityChat plugin) {
        this.plugin = plugin;
        loadColorConfig();
    }

    public void loadColorConfig() {
        File colorFile = new File(plugin.getDataFolder(), "chat" + File.separator + "colors.yml");
        if (colorFile.exists()) {
            colorConfig = YamlConfiguration.loadConfiguration(colorFile);
        }
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
        if (colorConfig == null) {
            player.sendMessage(mm.deserialize("<red>Color configuration not loaded!"));
            return;
        }

        ConfigurationSection guiSection = colorConfig.getConfigurationSection("gui");
        String title = guiSection != null ? guiSection.getString("title", "<gradient:#FFD700:#FFA500>Chat Colors</gradient>") : "<gradient:#FFD700:#FFA500>Chat Colors</gradient>";
        int size = guiSection != null ? guiSection.getInt("size", 54) : 54;

        Inventory gui = Bukkit.createInventory(null, size, mm.deserialize(title));

        ConfigurationSection colorsSection = colorConfig.getConfigurationSection("colors");
        if (colorsSection != null) {
            for (String colorKey : colorsSection.getKeys(false)) {
                ConfigurationSection colorSection = colorsSection.getConfigurationSection(colorKey);
                if (colorSection != null) {
                    String materialName = colorSection.getString("material", "WHITE_WOOL");
                    String displayName = colorSection.getString("display-name", "<white>Color");
                    String code = colorSection.getString("code", "&f");
                    String permission = colorSection.getString("permission", "solaritychat.color." + colorKey);
                    int slot = colorSection.getInt("slot", -1);

                    if (slot >= 0 && slot < size) {
                        ItemStack item = createColorItem(materialName, displayName, code, permission);
                        if (item != null) {
                            gui.setItem(slot, item);
                        }
                    }
                }
            }
        }

        if (guiSection != null && guiSection.contains("reset-item")) {
            ConfigurationSection resetSection = guiSection.getConfigurationSection("reset-item");
            if (resetSection != null) {
                String material = resetSection.getString("material", "BARRIER");
                String displayName = resetSection.getString("display-name", "<red>Reset Color");
                int slot = resetSection.getInt("slot", 49);

                ItemStack resetItem = createColorItem(material, displayName, "reset", "solaritychat.colors");
                if (resetItem != null && slot >= 0 && slot < size) {
                    gui.setItem(slot, resetItem);
                }
            }
        }

        player.openInventory(gui);
    }

    private ItemStack createColorItem(String materialName, String name, String colorCode, String permission) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (item == null) return null;

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