package org.busybee.solaritychat.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConfigValidator {

    private final Plugin plugin;
    private final ConfigBackupManager backupManager;
    private int totalFieldsAdded = 0;
    private int totalFilesUpdated = 0;

    private static final String[] CONFIG_FILES = {
        "config.yml",
        "messages.yml"
    };

    public ConfigValidator(Plugin plugin) {
        this.plugin = plugin;
        this.backupManager = new ConfigBackupManager(plugin);
    }

    public void validateAllConfigs() {
        plugin.getLogger().info("Starting configuration validation...");

        for (String configFile : CONFIG_FILES) {
            validateConfig(configFile);
        }

        if (totalFilesUpdated > 0) {
            plugin.getLogger().info("Config validation complete - " + totalFieldsAdded +
                " new field(s) added across " + totalFilesUpdated + " file(s)");
        } else {
            plugin.getLogger().info("Config validation complete - all configs up to date");
        }
    }

    private void validateConfig(String configPath) {
        File configFile = new File(plugin.getDataFolder(), configPath);

        if (!configFile.exists()) {
            return;
        }

        InputStream defaultStream = plugin.getResource(configPath.replace('\\', '/'));
        if (defaultStream == null) {
            plugin.getLogger().warning("No default resource found for: " + configPath);
            return;
        }

        try {
            FileConfiguration existingConfig = new YamlConfiguration();
            try {
                existingConfig.load(configFile);
            } catch (InvalidConfigurationException | IOException e) {
                plugin.getLogger().severe("Detected corruption in " + configPath + "! Backing up and regenerating...");
                backupManager.createBackup(configFile, true);
                configFile.delete();
                plugin.saveResource(configPath.replace('\\', '/'), true);
                totalFilesUpdated++;
                return;
            }

            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );

            ConfigMerger merger = new ConfigMerger();
            if (merger.needsMigration(existingConfig, defaultConfig)) {
                File backup = backupManager.createBackup(configFile);
                if (backup == null) {
                    plugin.getLogger().warning("Failed to create backup for " + configPath + ", skipping update");
                    return;
                }

                FileConfiguration mergedConfig = merger.merge(existingConfig, defaultConfig);
                mergedConfig.save(configFile);

                List<String> addedKeys = merger.getAddedKeys();
                totalFieldsAdded += addedKeys.size();
                totalFilesUpdated++;

                plugin.getLogger().info("Updated " + configPath + ": Added " + addedKeys.size() + " new field(s)");
                for (String key : addedKeys) {
                    plugin.getLogger().info("  + " + key);
                }
            }

            defaultStream.close();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to validate config " + configPath + ": " + e.getMessage());
        }
    }

    public ConfigBackupManager getBackupManager() {
        return backupManager;
    }
    public int getTotalFieldsAdded() {
        return totalFieldsAdded;
    }
    public int getTotalFilesUpdated() {
        return totalFilesUpdated;
    }
}
