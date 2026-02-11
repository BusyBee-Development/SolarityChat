package org.busybee.solaritychat.util;

import org.busybee.solaritychat.SolarityChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final SolarityChat plugin;
    private final Map<String, FileConfiguration> configs;

    public ConfigManager(SolarityChat plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        loadConfigs();
    }

    private void loadConfigs() {
        configs.put("messages", loadConfig("messages.yml"));
        configs.put("filters", loadConfig("chat" + File.separator + "filters.yml"));
        configs.put("format", loadConfig("chat" + File.separator + "format.yml"));
        configs.put("tags", loadConfig("chat" + File.separator + "tags.yml"));
        configs.put("channels", loadConfig("chat" + File.separator + "channels.yml"));
    }

    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        configs.clear();
        loadConfigs();
    }

    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }

    public String getFormattedMessage(String path, boolean includePrefix) {
        FileConfiguration messages = getConfig("messages");
        if (messages == null) return path;
        String message = messages.getString(path, "<red>Message not found: " + path);
        if (includePrefix) {
            String prefix = messages.getString("plugin-prefix", "<dark_gray>[<gold>SolarityChat</gold>]</dark_gray> ");
            message = prefix + message;
        }
        return message;
    }

    public String getMessage(String path) {
        FileConfiguration messages = getConfig("messages");
        if (messages == null) return path;
        String message = messages.getString(path, "<red>Message not found: " + path);
        String prefix = messages.getString("plugin-prefix", "<dark_gray>[<gold>SolarityChat</gold>]</dark_gray> ");
        return message.replace("%prefix%", prefix);
    }

    public String getMessageRaw(String path) {
        FileConfiguration messages = getConfig("messages");
        if (messages == null) return path;
        return messages.getString(path, "<red>Message not found: " + path);
    }
}