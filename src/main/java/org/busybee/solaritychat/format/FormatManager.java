package org.busybee.solaritychat.format;

import org.busybee.solaritychat.SolarityChat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FormatManager {

    private final SolarityChat plugin;
    private final List<ChatFormat> formats;
    private String defaultFormatName;

    public FormatManager(SolarityChat plugin) {
        this.plugin = plugin;
        this.formats = new ArrayList<>();
        loadFormats();
    }

    public void loadFormats() {
        formats.clear();
        
        var config = plugin.getConfigManager().getConfig("format");
        if (config == null) {
            plugin.getLogger().warning("format.yml not found!");
            return;
        }

        defaultFormatName = config.getString("default-format", "default");

        ConfigurationSection formatsSection = config.getConfigurationSection("formats");
        if (formatsSection == null) {
            plugin.getLogger().warning("No formats section found in format.yml!");
            return;
        }

        for (String key : formatsSection.getKeys(false)) {
            ConfigurationSection formatSection = formatsSection.getConfigurationSection(key);
            if (formatSection == null) {
                continue;
            }

            int priority = formatSection.getInt("priority", 0);
            String permission = formatSection.getString("permission", "");

            FormatComponent prefix = loadComponent(formatSection.getConfigurationSection("prefix"));
            FormatComponent name = loadComponent(formatSection.getConfigurationSection("name"));
            FormatComponent suffix = loadComponent(formatSection.getConfigurationSection("suffix"));
            FormatComponent message = loadComponent(formatSection.getConfigurationSection("message"));

            ChatFormat chatFormat = new ChatFormat(key, priority, permission, prefix, name, suffix, message);
            formats.add(chatFormat);
        }

        formats.sort(Comparator.comparingInt(ChatFormat::getPriority).reversed());

        plugin.getLogger().info("Loaded " + formats.size() + " chat formats.");
    }

    private FormatComponent loadComponent(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        String text = section.getString("text", "");
        List<String> hover = section.getStringList("hover");
        String click = section.getString("click", "");

        return new FormatComponent(text, hover, click);
    }

    public ChatFormat getFormat(Player player) {
        for (ChatFormat format : formats) {
            if (format.getPermission().isEmpty() || player.hasPermission(format.getPermission())) {
                return format;
            }
        }

        for (ChatFormat format : formats) {
            if (format.getName().equalsIgnoreCase(defaultFormatName)) {
                return format;
            }
        }

        return formats.isEmpty() ? null : formats.get(formats.size() - 1);
    }

    public void reload() {
        loadFormats();
    }

    public static class ChatFormat {
        private final String name;
        private final int priority;
        private final String permission;
        private final FormatComponent prefix;
        private final FormatComponent nameComponent;
        private final FormatComponent suffix;
        private final FormatComponent message;

        public ChatFormat(String name, int priority, String permission, 
                         FormatComponent prefix, FormatComponent nameComponent, 
                         FormatComponent suffix, FormatComponent message) {
            this.name = name;
            this.priority = priority;
            this.permission = permission;
            this.prefix = prefix;
            this.nameComponent = nameComponent;
            this.suffix = suffix;
            this.message = message;
        }

        public String getName() {
            return name;
        }
        public int getPriority() {
            return priority;
        }
        public String getPermission() {
            return permission;
        }
        public FormatComponent getPrefix() {
            return prefix;
        }
        public FormatComponent getNameComponent() {
            return nameComponent;
        }
        public FormatComponent getSuffix() {
            return suffix;
        }
        public FormatComponent getMessage() {
            return message;
        }
    }

    public static class FormatComponent {
        private final String text;
        private final List<String> hover;
        private final String click;

        public FormatComponent(String text, List<String> hover, String click) {
            this.text = text;
            this.hover = hover != null ? hover : new ArrayList<>();
            this.click = click != null ? click : "";
        }

        public String getText() {
            return text;
        }
        public List<String> getHover() {
            return hover;
        }
        public String getClick() {
            return click;
        }
    }
}
