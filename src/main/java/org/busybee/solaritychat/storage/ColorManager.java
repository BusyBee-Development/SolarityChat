package org.busybee.solaritychat.storage;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.busybee.solaritychat.SolarityChat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ColorManager {

    private final SolarityChat plugin;
    private final DatabaseManager databaseManager;
    private final AsyncLoadingCache<UUID, Optional<String>> colorCache;

    public ColorManager(SolarityChat plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.colorCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .buildAsync(this::loadColorFromDatabase);
        createTable();
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

    public String getPlayerColor(UUID uuid) {
        CompletableFuture<Optional<String>> future = colorCache.get(uuid);
        Optional<String> result = future.getNow(Optional.empty());
        return result.orElse(null);
    }

    public void preloadPlayerColor(UUID uuid) {
        colorCache.get(uuid);
    }

    public void setPlayerColor(UUID uuid, String colorCode) {
        if (colorCode == null) {
            colorCache.put(uuid, CompletableFuture.completedFuture(Optional.empty()));
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

        colorCache.put(uuid, CompletableFuture.completedFuture(Optional.of(colorCode)));
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