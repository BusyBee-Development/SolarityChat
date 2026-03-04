package org.busybee.solaritychat.storage;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.busybee.solaritychat.SolarityChat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ColorManager {

    private final SolarityChat plugin;
    private final DatabaseManager databaseManager;
    private final LoadingCache<UUID, Optional<String>> colorCache;
    private final Map<String, ColorDefinition> colorDefinitions = new LinkedHashMap<>();

    public ColorManager(SolarityChat plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.colorCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(this::loadColorFromDatabase);
        createTable();
        loadDefinitions();
    }

    public void loadDefinitions() {
        colorDefinitions.clear();
        File colorFile = new File(plugin.getDataFolder(), "chat" + File.separator + "colors.yml");
        if (!colorFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(colorFile);
        ConfigurationSection section = config.getConfigurationSection("colors");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection colorSection = section.getConfigurationSection(key);
            if (colorSection == null) continue;

            String displayName = colorSection.getString("display-name", key);
            String material = colorSection.getString("material", "WHITE_WOOL");
            String code = colorSection.getString("code", "");
            String permission = colorSection.getString("permission", "solaritychat.color." + key);

            colorDefinitions.put(key, new ColorDefinition(key, displayName, material, code, permission));
        }
    }

    public Map<String, ColorDefinition> getColorDefinitions() {
        return Collections.unmodifiableMap(colorDefinitions);
    }

    public ColorDefinition getColorDefinition(String id) {
        return colorDefinitions.get(id);
    }

    private void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS player_colors (" +
                "uuid TEXT PRIMARY KEY," +
                "color_code TEXT NOT NULL" +
                ")";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create player_colors table: " + e.getMessage());
        }
    }

    private Optional<String> loadColorFromDatabase(UUID uuid) {
        String query = "SELECT color_code FROM player_colors WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("color_code"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load player color: " + e.getMessage());
        }
        return Optional.empty();
    }

    public String getPlayerColorId(UUID uuid) {
        try {
            Optional<String> result = colorCache.get(uuid);
            return result.orElse(null);
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting player color: " + e.getMessage());
            return null;
        }
    }

    public String getPlayerColorCode(UUID uuid) {
        String id = getPlayerColorId(uuid);
        if (id == null) return null;
        ColorDefinition def = getColorDefinition(id);
        if (def != null) return def.code();
        if (id.startsWith("&") || id.startsWith("§") || id.startsWith("<") || id.startsWith("#")) {
            return id;
        }
        return null;
    }

    @Deprecated
    public String getPlayerColor(UUID uuid) {
        return getPlayerColorCode(uuid);
    }

    public void preloadPlayerColor(UUID uuid) {
        colorCache.get(uuid);
    }

    public void setPlayerColor(UUID uuid, String colorCode) {
        if (colorCode == null) {
            colorCache.put(uuid, Optional.empty());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                String query = "DELETE FROM player_colors WHERE uuid = ?";
                try (Connection conn = databaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Could not delete player color: " + e.getMessage());
                }
            });
            return;
        }

        colorCache.put(uuid, Optional.of(colorCode));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "INSERT OR REPLACE INTO player_colors (uuid, color_code) VALUES (?, ?)";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, colorCode);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not save player color: " + e.getMessage());
            }
        });
    }
}
