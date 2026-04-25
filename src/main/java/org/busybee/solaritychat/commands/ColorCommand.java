package org.busybee.solaritychat.commands;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.busybee.solaritychat.storage.ColorDefinition;
import org.busybee.solaritychat.util.MessageUtil;
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
import java.util.stream.Collectors;

public class ColorCommand implements CommandExecutor {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private YamlConfiguration colorConfig;
    private final String PREVIEW_TEXT = "This is your chat color!";

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

        openColorGUI(player, 0);
        return true;
    }

    public void openColorGUI(Player player, int page) {
        if (colorConfig == null) {
            player.sendMessage(mm.deserialize("<red>Color configuration not loaded!"));
            return;
        }

        ConfigurationSection guiSection = colorConfig.getConfigurationSection("gui");
        String title = guiSection != null ? guiSection.getString("title", "<gradient:#FFD700:#FFA500>Chat Colors</gradient>") : "<gradient:#FFD700:#FFA500>Chat Colors</gradient>";
        int size = guiSection != null ? guiSection.getInt("size", 54) : 54;

        Inventory gui = Bukkit.createInventory(null, size, MessageUtil.parseGui(title + (page > 0 ? " - Page " + (page + 1) : "")));

        if (guiSection != null && guiSection.contains("fill")) {
            ConfigurationSection fillSection = guiSection.getConfigurationSection("fill");
            if (fillSection != null) {
                String mat = fillSection.getString("material", "BLACK_STAINED_GLASS_PANE");
                String name = fillSection.getString("name", " ");
                List<Integer> slots = fillSection.getIntegerList("slots");
                ItemStack filler = createFillerItem(mat, name);
                for (int slot : slots) {
                    if (slot >= 0 && slot < size) {
                        gui.setItem(slot, filler);
                    }
                }
            }
        }

        List<ColorDefinition> colors = new ArrayList<>(plugin.getColorManager().getColorDefinitions().values());
        List<Integer> reservedSlots = getReservedSlots(guiSection, size);
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (!reservedSlots.contains(i)) {
                availableSlots.add(i);
            }
        }

        int itemsPerPage = availableSlots.size();
        int totalPages = (int) Math.ceil((double) colors.size() / itemsPerPage);
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, colors.size());

        String currentColorId = plugin.getColorManager().getPlayerColorId(player.getUniqueId());

        for (int i = start; i < end; i++) {
            ColorDefinition def = colors.get(i);
            int slot = availableSlots.get(i - start);
            gui.setItem(slot, createColorItem(player, def, currentColorId));
        }

        if (page > 0 && guiSection != null && guiSection.contains("previous-page")) {
            gui.setItem(guiSection.getInt("previous-page.slot"), createNavigationItem(guiSection.getConfigurationSection("previous-page"), page - 1));
        }
        if (end < colors.size() && guiSection != null && guiSection.contains("next-page")) {
            gui.setItem(guiSection.getInt("next-page.slot"), createNavigationItem(guiSection.getConfigurationSection("next-page"), page + 1));
        }

        if (guiSection != null && guiSection.contains("reset-item")) {
            gui.setItem(guiSection.getInt("reset-item.slot"), createResetItem(guiSection.getConfigurationSection("reset-item")));
        }

        player.openInventory(gui);
    }

    private List<Integer> getReservedSlots(ConfigurationSection guiSection, int size) {
        List<Integer> reserved = new ArrayList<>();
        if (guiSection == null) return reserved;
        if (guiSection.contains("fill.slots")) reserved.addAll(guiSection.getIntegerList("fill.slots"));
        if (guiSection.contains("previous-page.slot")) reserved.add(guiSection.getInt("previous-page.slot"));
        if (guiSection.contains("next-page.slot")) reserved.add(guiSection.getInt("next-page.slot"));
        if (guiSection.contains("reset-item.slot")) reserved.add(guiSection.getInt("reset-item.slot"));
        return reserved.stream().distinct().filter(s -> s >= 0 && s < size).collect(Collectors.toList());
    }

    private ItemStack createFillerItem(String mat, String name) {
        ItemStack item = XMaterial.matchXMaterial(mat).map(XMaterial::parseItem).orElse(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtil.parseGui(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNavigationItem(ConfigurationSection section, int targetPage) {
        ItemStack item = XMaterial.matchXMaterial(section.getString("material", "ARROW")).map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.ARROW));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtil.parseGui(section.getString("name", "Page " + (targetPage + 1))));
            NamespacedKey pageKey = new NamespacedKey(plugin, "gui_page");
            meta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, targetPage);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createResetItem(ConfigurationSection section) {
        ItemStack item = XMaterial.matchXMaterial(section.getString("material", "BARRIER")).map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.BARRIER));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtil.parseGui(section.getString("name", "<red>Reset Color")));
            List<String> loreStrings = section.getStringList("lore");
            if (!loreStrings.isEmpty()) {
                meta.lore(MessageUtil.parseGui(loreStrings));
            }
            NamespacedKey actionKey = new NamespacedKey(plugin, "gui_action");
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "reset");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createColorItem(Player player, ColorDefinition def, String currentId) {
        boolean hasPerm = player.hasPermission(def.permission());
        boolean isSelected = def.id().equals(currentId);

        String templateKey = isSelected ? "selected" : (hasPerm ? "available" : "locked");
        ConfigurationSection template = colorConfig.getConfigurationSection("templates." + templateKey);
        if (template == null) return new ItemStack(org.bukkit.Material.BARRIER);

        String materialName = template.getString("material", def.material()).replace("{material}", def.material());
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(XMaterial.PAPER.parseItem());
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = template.getString("name", def.displayName())
                    .replace("{display_name}", def.displayName())
                    .replace("{id}", def.id());
            meta.displayName(MessageUtil.parseGui(name));

            String previewText = def.code().contains("gradient") || def.code().contains("rainbow") 
                    ? def.code() + PREVIEW_TEXT + (def.code().contains("gradient") ? "</gradient>" : "</rainbow>")
                    : def.code() + PREVIEW_TEXT;

            List<String> loreStrings = template.getStringList("lore");
            meta.lore(MessageUtil.parseGui(loreStrings.stream().map(l -> l
                    .replace("{preview_text}", previewText)
                    .replace("{permission}", def.permission())
                    .replace("{id}", def.id())
                    .replace("{display_name}", def.displayName())
            ).collect(Collectors.toList())));

            if (template.getBoolean("glow", false)) {
                XEnchantment.matchXEnchantment("unbreaking").ifPresent(xe -> {
                    org.bukkit.enchantments.Enchantment ench = xe.getEnchant();
                    if (ench != null) meta.addEnchant(ench, 1, true);
                });
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            NamespacedKey colorIdKey = new NamespacedKey(plugin, "color_id");
            meta.getPersistentDataContainer().set(colorIdKey, PersistentDataType.STRING, def.id());

            item.setItemMeta(meta);
        }
        return item;
    }
}
