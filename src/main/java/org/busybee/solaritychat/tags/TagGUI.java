package org.busybee.solaritychat.tags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.util.ConfigManager;
import org.busybee.solaritychat.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TagGUI implements Listener {

    private final SolarityChat plugin;
    private final TagManager tagManager;
    private final ConfigManager configManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<UUID, Map<Integer, String>> playerSlotMappings = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerPages = new ConcurrentHashMap<>();

    public TagGUI(SolarityChat plugin, TagManager tagManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.tagManager = tagManager;
        this.configManager = configManager;
    }

    public void openGUI(Player player) {
        openGUI(player, 0);
    }

    public void openGUI(Player player, int page) {
        UUID uuid = player.getUniqueId();
        playerPages.put(uuid, page);
        Map<Integer, String> slotToTag = new ConcurrentHashMap<>();
        playerSlotMappings.put(uuid, slotToTag);

        ConfigurationSection guiSection = tagManager.getTagsConfig().getConfigurationSection("gui");
        int rows = guiSection.getInt("rows", 6);
        String title = guiSection.getString("title", "Select Your Tag");
        boolean fillEmpty = guiSection.getBoolean("fill-empty", true);
        String fillMaterialName = guiSection.getString("fill-material", "GRAY_STAINED_GLASS_PANE");
        Material fillMaterial = Material.getMaterial(fillMaterialName.toUpperCase());
        if (fillMaterial == null) {
            fillMaterial = Material.GRAY_STAINED_GLASS_PANE;
        }

        Inventory inv = Bukkit.createInventory(null, rows * 9, MessageUtil.parse(title));

        if (fillEmpty) {
            ItemStack filler = new ItemStack(fillMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.displayName(Component.text(" "));
                filler.setItemMeta(fillerMeta);
            }
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, filler);
            }
        }

        int previousPageSlot = (rows - 1) * 9;
        int resetSlot = (rows - 1) * 9 + 4;
        int nextPageSlot = (rows * 9) - 1;

        ConfigurationSection tagsSection = tagManager.getTagsSection();
        if (tagsSection != null) {
            List<String> tagIds = new ArrayList<>(tagsSection.getKeys(false));
            int tagsPerPage = (rows - 1) * 9;
            int startIndex = page * tagsPerPage;
            int endIndex = Math.min(startIndex + tagsPerPage, tagIds.size());

            for (int i = startIndex; i < endIndex; i++) {
                String tagId = tagIds.get(i);
                ConfigurationSection tagSection = tagsSection.getConfigurationSection(tagId);
                if (tagSection == null) continue;
                ConfigurationSection itemSection = tagSection.getConfigurationSection("item");
                if (itemSection == null) continue;

                int slot = i - startIndex;
                if (slot >= tagsPerPage) continue;

                String materialName = itemSection.getString("material", "PAPER");
                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material == null) {
                    material = Material.PAPER;
                }
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    String name = itemSection.getString("name", tagId);
                    meta.displayName(MessageUtil.parse(name));

                    List<Component> lore = new ArrayList<>();
                    for (String line : itemSection.getStringList("lore")) {
                        lore.add(MessageUtil.parse(line));
                    }

                    String equippedTag = tagManager.getEquippedTag(player.getUniqueId());
                    if (tagId.equals(equippedTag)) {
                        lore.add(Component.empty());
                        lore.add(mm.deserialize("<green>✔ Currently Equipped"));
                    }

                    if (!tagManager.hasPermission(player, tagId)) {
                        lore.add(Component.empty());
                        lore.add(mm.deserialize("<red>✘ No Permission"));
                    }

                    meta.lore(lore);
                    item.setItemMeta(meta);
                }

                inv.setItem(slot, item);
                slotToTag.put(slot, tagId);
            }

            if (page > 0) {
                ItemStack previousPage = new ItemStack(Material.ARROW);
                ItemMeta previousMeta = previousPage.getItemMeta();
                previousMeta.displayName(mm.deserialize("<green>Previous Page"));
                previousPage.setItemMeta(previousMeta);
                inv.setItem(previousPageSlot, previousPage);
            }

            if (tagIds.size() > endIndex) {
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = nextPage.getItemMeta();
                nextMeta.displayName(mm.deserialize("<green>Next Page"));
                nextPage.setItemMeta(nextMeta);
                inv.setItem(nextPageSlot, nextPage);
            }
        }

        ItemStack resetItem = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = resetItem.getItemMeta();
        resetMeta.displayName(mm.deserialize("<red><bold>Reset Tag"));
        resetMeta.lore(List.of(mm.deserialize("<gray>Click to remove your tag")));
        resetItem.setItemMeta(resetMeta);
        inv.setItem(resetSlot, resetItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String guiTitle = tagManager.getTagsConfig().getString("gui.title", "Select Your Tag");
        String viewTitle = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        String expectedTitle = PlainTextComponentSerializer.plainText().serialize(MessageUtil.parse(guiTitle));

        if (!viewTitle.equals(expectedTitle)) return;

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();
        Map<Integer, String> slotToTag = playerSlotMappings.get(uuid);
        if (slotToTag == null) return;

        int slot = event.getRawSlot();
        int currentPage = playerPages.getOrDefault(uuid, 0);
        int rows = tagManager.getTagsConfig().getInt("gui.rows", 6);

        int previousPageSlot = (rows - 1) * 9;
        int resetSlot = (rows - 1) * 9 + 4;
        int nextPageSlot = (rows * 9) - 1;

        if (slot == previousPageSlot) {
            if (currentPage > 0) {
                openGUI(player, currentPage - 1);
            }
            return;
        }

        if (slot == nextPageSlot) {
            int tagsPerPage = (rows - 1) * 9;
            ConfigurationSection tagsSection = tagManager.getTagsSection();
            int totalTags = tagsSection != null ? tagsSection.getKeys(false).size() : 0;
            if ((currentPage + 1) * tagsPerPage < totalTags) {
                openGUI(player, currentPage + 1);
            }
            return;
        }

        if (slot == resetSlot) {
            tagManager.setTag(uuid, null);
            player.sendMessage(mm.deserialize(configManager.getFormattedMessage("tag-reset", true)));
            player.closeInventory();
            return;
        }

        if (slotToTag.containsKey(slot)) {
            String tagId = slotToTag.get(slot);

            if (!tagManager.hasPermission(player, tagId)) {
                player.sendMessage(mm.deserialize(configManager.getFormattedMessage("no-tag-permission", true)));
                player.closeInventory();
                return;
            }

            tagManager.setTag(uuid, tagId);
            String display = tagManager.getTagDisplay(tagId);
            String displayStr = display != null ? display : tagId;
            player.sendMessage(mm.deserialize(configManager.getFormattedMessage("tag-selected", true) + displayStr));
            player.closeInventory();
        }
    }
}