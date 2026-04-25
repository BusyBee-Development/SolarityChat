package org.busybee.solaritychat.tags;

import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.storage.DatabaseManager;
import org.busybee.solaritychat.util.MessageUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagManager {

    private final SolarityChat plugin;
    private final DatabaseManager databaseManager;
    private FileConfiguration tagsConfig;
    private File tagsFile;
    private final Map<UUID, String> equippedTags;

    public TagManager(SolarityChat plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
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
        equippedTags.clear();
        File oldDataFile = new File(plugin.getDataFolder(), "tag-data.yml");
        if (oldDataFile.exists()) {
            plugin.getLogger().info("Found legacy tag-data.yml, migrating to database...");
            FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(oldDataFile);
            ConfigurationSection section = dataConfig.getConfigurationSection("equipped");
            if (section != null) {
                for (String uuidString : section.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        String tagId = dataConfig.getString("equipped." + uuidString);
                        equippedTags.put(uuid, tagId);
                        saveTagToDatabaseSync(uuid, tagId);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            if (oldDataFile.renameTo(new File(plugin.getDataFolder(), "tag-data.yml.old"))) {
                plugin.getLogger().info("Tag migration complete.");
            } else {
                plugin.getLogger().warning("Tag migration finished but could not rename tag-data.yml!");
            }
        } else {
            loadTagsFromDatabase();
        }
    }

    private void loadTagsFromDatabase() {
        String query = "SELECT uuid, tag_id FROM player_tags";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                equippedTags.put(UUID.fromString(rs.getString("uuid")), rs.getString("tag_id"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load player tags: " + e.getMessage());
        }
    }

    private void saveTagToDatabaseSync(UUID uuid, String tagId) {
        String query = "INSERT OR REPLACE INTO player_tags (uuid, tag_id) VALUES (?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, tagId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save player tag sync: " + e.getMessage());
        }
    }

    public void reload() {
        loadConfig();
        loadData();
    }

    public void saveData() {
    }

    public void setTag(UUID uuid, String tagId) {
        if (tagId == null) {
            equippedTags.remove(uuid);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                String query = "DELETE FROM player_tags WHERE uuid = ?";
                try (Connection conn = databaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Could not delete player tag: " + e.getMessage());
                }
            });
        } else {
            equippedTags.put(uuid, tagId);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                saveTagToDatabaseSync(uuid, tagId);
            });
        }
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
