package org.busybee.solaritychat.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.busybee.solaritychat.SolarityChat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WarningManager {

    private final SolarityChat plugin;
    private final DatabaseManager databaseManager;
    private final Cache<UUID, Integer> warningCache;

    public WarningManager(SolarityChat plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.warningCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
    }

    public int getWarnings(UUID uuid) {
        Integer cached = warningCache.getIfPresent(uuid);
        if (cached != null) {
            return cached;
        }

        if (!plugin.getConfig().getBoolean("warnings.persistent", true)) {
            warningCache.put(uuid, 0);
            return 0;
        }

        int warnings = loadWarningsFromDatabase(uuid);
        warningCache.put(uuid, warnings);
        return warnings;
    }

    private int loadWarningsFromDatabase(UUID uuid) {
        String query = "SELECT warning_count FROM warnings WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("warning_count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load warnings for " + uuid + ": " + e.getMessage());
        }
        return 0;
    }

    public void setWarnings(UUID uuid, String playerName, int amount) {
        warningCache.put(uuid, amount);

        if (!plugin.getConfig().getBoolean("warnings.persistent", true)) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "INSERT OR REPLACE INTO warnings (uuid, player_name, warning_count, last_updated) VALUES (?, ?, ?, ?)";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.setInt(3, amount);
                stmt.setLong(4, System.currentTimeMillis());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save warnings for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public void addWarnings(UUID uuid, String playerName, int amount) {
        int current = getWarnings(uuid);
        int newAmount = current + amount;
        setWarnings(uuid, playerName, newAmount);
    }

    public void clearWarnings(UUID uuid, String playerName) {
        setWarnings(uuid, playerName, 0);
    }
}