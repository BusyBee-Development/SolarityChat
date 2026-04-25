package org.busybee.solaritychat.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Intelligently merges configuration files, preserving user values while adding new fields
 * and maintaining comments using the native Paper/Spigot API.
 */
public class ConfigMerger {

    private final List<String> addedKeys = new ArrayList<>();

    public ConfigMerger() {
    }

    /**
     * Merges default configuration with existing configuration.
     *
     * @param userConfig   The current user configuration
     * @param defaultConfig The default configuration from resources
     * @return The merged configuration
     */
    public FileConfiguration merge(@NotNull FileConfiguration userConfig, @NotNull FileConfiguration defaultConfig) {
        addedKeys.clear();

        YamlConfiguration newConfig = new YamlConfiguration();
        
        // Use the default config as a base for structure and comments
        transferSection(userConfig, defaultConfig, newConfig, "");

        return newConfig;
    }

    private void transferSection(ConfigurationSection userConfig, ConfigurationSection defaultConfig, ConfigurationSection newConfig, String path) {
        for (String key : defaultConfig.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (defaultConfig.isConfigurationSection(key)) {
                ConfigurationSection newSubSection = newConfig.createSection(key);

                // Transfer comments from default
                newConfig.setComments(key, defaultConfig.getComments(key));
                newConfig.setInlineComments(key, defaultConfig.getInlineComments(key));

                ConfigurationSection userSubSection = userConfig != null ? userConfig.getConfigurationSection(key) : null;
                ConfigurationSection defaultSubSection = defaultConfig.getConfigurationSection(key);

                if (defaultSubSection != null) {
                    transferSection(userSubSection, defaultSubSection, newSubSection, fullPath);
                }
            } else {
                Object value;
                if (userConfig != null && userConfig.contains(key) && !userConfig.isConfigurationSection(key)) {
                    value = userConfig.get(key);
                } else {
                    value = defaultConfig.get(key);
                    addedKeys.add(fullPath);
                }

                newConfig.set(key, value);
                
                // Transfer comments from default
                newConfig.setComments(key, defaultConfig.getComments(key));
                newConfig.setInlineComments(key, defaultConfig.getInlineComments(key));
            }
        }

        // Preserve user keys not in default for this section (recursive preservation)
        preserveUserKeys(userConfig, defaultConfig, newConfig);
    }

    private void preserveUserKeys(ConfigurationSection userConfig, ConfigurationSection defaultConfig, ConfigurationSection newConfig) {
        if (userConfig == null) return;

        for (String key : userConfig.getKeys(false)) {
            if (!defaultConfig.contains(key)) {
                if (userConfig.isConfigurationSection(key)) {
                    ConfigurationSection userSubSection = userConfig.getConfigurationSection(key);
                    ConfigurationSection newSubSection = newConfig.createSection(key);
                    if (userSubSection != null) {
                        copyRecursive(userSubSection, newSubSection);
                    }
                } else {
                    newConfig.set(key, userConfig.get(key));
                }
                // Transfer comments if they exist in user config
                newConfig.setComments(key, userConfig.getComments(key));
                newConfig.setInlineComments(key, userConfig.getInlineComments(key));
            }
        }
    }

    private void copyRecursive(ConfigurationSection from, ConfigurationSection to) {
        for (String key : from.getKeys(false)) {
            if (from.isConfigurationSection(key)) {
                copyRecursive(from.getConfigurationSection(key), to.createSection(key));
            } else {
                to.set(key, from.get(key));
            }
            to.setComments(key, from.getComments(key));
            to.setInlineComments(key, from.getInlineComments(key));
        }
    }

    /**
     * Checks if migration is needed by comparing keys.
     */
    public boolean needsMigration(@NotNull FileConfiguration userConfig, @NotNull FileConfiguration defaultConfig) {
        Set<String> defaultKeys = getAllKeys(defaultConfig, "");
        Set<String> userKeys = getAllKeys(userConfig, "");

        for (String key : defaultKeys) {
            if (!userKeys.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getAllKeys(@NotNull ConfigurationSection section, @NotNull String prefix) {
        Set<String> keys = new HashSet<>();
        for (String key : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            keys.add(fullKey);

            if (section.isConfigurationSection(key)) {
                ConfigurationSection subSection = section.getConfigurationSection(key);
                if (subSection != null) {
                    keys.addAll(getAllKeys(subSection, fullKey));
                }
            }
        }
        return keys;
    }

    public List<String> getAddedKeys() {
        return new ArrayList<>(addedKeys);
    }
}
