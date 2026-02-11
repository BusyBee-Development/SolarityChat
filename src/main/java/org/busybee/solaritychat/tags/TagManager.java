package org.busybee.solaritychat.tags;

import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.util.MessageUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagManager {

    private final SolarityChat plugin;
    private FileConfiguration tagsConfig;
    private File tagsFile;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, String> equippedTags;

    public TagManager(SolarityChat plugin) {
        this.plugin = plugin;
        this.equippedTags = new HashMap<>();
        loadConfig();
        loadData();
    }

    private void loadConfig() {
        tagsFile = new File(plugin.getDataFolder(), "chat" + File.separator + "tags.yml");
        if (!tagsFile.exists()) {
            plugin.saveResource("chat" + File.separator + "tags.yml", false);
        }
        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "tag-data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("equipped");
        if (section != null) {
            for (String uuidString : section.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                String tagId = dataConfig.getString("equipped." + uuidString);
                equippedTags.put(uuid, tagId);
            }
        }
    }

    public void reload() {
        loadConfig();
        loadData();
    }

    public void saveData() {
        for (Map.Entry<UUID, String> entry : equippedTags.entrySet()) {
            dataConfig.set("equipped." + entry.getKey().toString(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTag(UUID uuid, String tagId) {
        if (tagId == null) {
            equippedTags.remove(uuid);
            dataConfig.set("equipped." + uuid.toString(), null);
        } else {
            equippedTags.put(uuid, tagId);
            dataConfig.set("equipped." + uuid.toString(), tagId);
        }
        saveData();
    }

    public String getEquippedTag(UUID uuid) {
        return equippedTags.get(uuid);
    }

    public String getTagDisplay(String tagId) {
        if (tagId == null) return null;
        String display = tagsConfig.getString("tags." + tagId + ".display");
        if (display == null) return null;
        return MessageUtil.legacyToMini(display);
    }

    public boolean hasPermission(Player player, String tagId) {
        String permission = tagsConfig.getString("tags." + tagId + ".permission");
        return permission == null || permission.isEmpty() || player.hasPermission(permission);
    }

    public ConfigurationSection getTagsSection() {
        return tagsConfig.getConfigurationSection("tags");
    }

    public FileConfiguration getTagsConfig() {
        return tagsConfig;
    }
}