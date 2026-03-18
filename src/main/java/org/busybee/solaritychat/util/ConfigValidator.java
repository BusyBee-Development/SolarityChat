package org.busybee.solaritychat.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

        // If config doesn't exist, let Bukkit create it from defaults
        if (!configFile.exists()) {
            plugin.getLogger().fine("Config not found, will be created: " + configPath);
            return;
        }

        // Load default config from resources
        InputStream defaultStream = plugin.getResource(configPath);
        if (defaultStream == null) {
            plugin.getLogger().warning("No default resource found for: " + configPath);
            return;
        }

        try {
            // Load configs
            FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream)
            );

            // Check if merge is needed
            int fieldsToAdd = countMissingFields(existingConfig, defaultConfig);

            if (fieldsToAdd > 0) {
                plugin.getLogger().info("Updating " + configPath + " (" + fieldsToAdd + " new field(s))");

                // Create backup
                File backup = backupManager.createBackup(configFile);
                if (backup == null) {
                    plugin.getLogger().warning("Failed to create backup for " + configPath + ", skipping update");
                    return;
                }

                // Merge configs
                int addedFields = ConfigMerger.mergeConfigs(existingConfig, defaultConfig);

                // Save updated config
                existingConfig.save(configFile);

                totalFieldsAdded += addedFields;
                totalFilesUpdated++;

                plugin.getLogger().info("Successfully updated " + configPath);
            } else {
                plugin.getLogger().fine(configPath + " is up to date");
            }

            defaultStream.close();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to validate config " + configPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Counts how many fields are missing in the existing config compared to defaults.
     *
     * @param existing The existing configuration
     * @param defaults The default configuration
     * @return Number of missing fields
     */
    private int countMissingFields(FileConfiguration existing, FileConfiguration defaults) {
        return countMissingFieldsRecursive(existing.getRoot(), defaults.getRoot());
    }

    /**
     * Recursively counts missing fields in a configuration section.
     */
    private int countMissingFieldsRecursive(org.bukkit.configuration.ConfigurationSection existing,
                                             org.bukkit.configuration.ConfigurationSection defaults) {
        int missing = 0;

        for (String key : defaults.getKeys(false)) {
            if (!existing.contains(key)) {
                missing++;
            } else if (defaults.get(key) instanceof org.bukkit.configuration.ConfigurationSection) {
                org.bukkit.configuration.ConfigurationSection existingSection = existing.getConfigurationSection(key);
                org.bukkit.configuration.ConfigurationSection defaultSection = defaults.getConfigurationSection(key);

                if (existingSection != null && defaultSection != null) {
                    missing += countMissingFieldsRecursive(existingSection, defaultSection);
                }
            }
        }

        return missing;
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
