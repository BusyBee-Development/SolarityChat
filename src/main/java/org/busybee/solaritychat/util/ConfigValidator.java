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

/**
 * Validates and updates configuration files by comparing them with defaults
 * and merging missing fields while preserving user customizations.
 */
public class ConfigValidator {

    private final Plugin plugin;
    private final ConfigBackupManager backupManager;
    private int totalFieldsAdded = 0;
    private int totalFilesUpdated = 0;

    // Config files to validate (relative to plugin data folder)
    private static final String[] CONFIG_FILES = {
        "config.yml",
        "messages.yml"
    };

    // Chat folder config files (relative to plugin data folder)
    private static final String[] CHAT_CONFIG_FILES = {
        "chat/announcements.yml",
        "chat/channels.yml",
        "chat/colors.yml",
        "chat/filters.yml",
        "chat/format.yml",
        "chat/tags.yml"
    };

    public ConfigValidator(Plugin plugin) {
        this.plugin = plugin;
        this.backupManager = new ConfigBackupManager(plugin);
    }

    /**
     * Validates and updates all configuration files.
     * This should be called during plugin startup.
     */
    public void validateAllConfigs() {
        plugin.getLogger().info("Starting configuration validation...");

        // Validate main config files
        for (String configFile : CONFIG_FILES) {
            validateConfig(configFile);
        }

        // Validate chat folder configs
        for (String chatConfig : CHAT_CONFIG_FILES) {
            validateConfig(chatConfig);
        }

        // Log summary
        if (totalFilesUpdated > 0) {
            plugin.getLogger().info("Config validation complete - " + totalFieldsAdded +
                " new field(s) added across " + totalFilesUpdated + " file(s)");
        } else {
            plugin.getLogger().info("Config validation complete - all configs up to date");
        }
    }

    /**
     * Validates a single configuration file.
     *
     * @param configPath Path to config file relative to plugin data folder
     */
    private void validateConfig(String configPath) {
        File configFile = new File(plugin.getDataFolder(), configPath);

        // If config doesn't exist, it will be created normally by the plugin
        if (!configFile.exists()) {
            return;
        }

        // Load default config from resources
        InputStream defaultStream = plugin.getResource(configPath.replace('\\', '/'));
        if (defaultStream == null) {
            plugin.getLogger().warning("No default resource found for: " + configPath);
            return;
        }

        try {
            // Load configs
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
                // Create backup
                File backup = backupManager.createBackup(configFile);
                if (backup == null) {
                    plugin.getLogger().warning("Failed to create backup for " + configPath + ", skipping update");
                    return;
                }

                // Merge configs
                FileConfiguration mergedConfig = merger.merge(existingConfig, defaultConfig);

                // Save updated config
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

    /**
     * Gets the backup manager instance.
     *
     * @return The backup manager
     */
    public ConfigBackupManager getBackupManager() {
        return backupManager;
    }

    /**
     * Gets the total number of fields added during validation.
     *
     * @return Total fields added
     */
    public int getTotalFieldsAdded() {
        return totalFieldsAdded;
    }

    /**
     * Gets the total number of files updated during validation.
     *
     * @return Total files updated
     */
    public int getTotalFilesUpdated() {
        return totalFilesUpdated;
    }
}
