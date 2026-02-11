package org.busybee.solaritychat.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.busybee.solaritychat.SolarityChat;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final SolarityChat plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(SolarityChat plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, "data.db");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setPoolName("SolarityChatPool");
            config.setMaximumPoolSize(1);
            config.setMinimumIdle(1);
            config.setMaxLifetime(60000);
            config.setConnectionTestQuery("SELECT 1");
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("synchronous", "NORMAL");

            dataSource = new HikariDataSource(config);
            createTables();

            plugin.getLogger().info("Database initialized successfully with HikariCP!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() {
        String createWarningsTable = "CREATE TABLE IF NOT EXISTS warnings (" +
                "uuid TEXT PRIMARY KEY," +
                "player_name TEXT NOT NULL," +
                "warning_count INTEGER DEFAULT 0," +
                "last_updated INTEGER DEFAULT 0" +
                ")";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createWarningsTable);
            stmt.execute("PRAGMA journal_mode=WAL");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create tables: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}