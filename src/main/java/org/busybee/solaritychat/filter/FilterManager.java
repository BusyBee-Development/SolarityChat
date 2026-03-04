package org.busybee.solaritychat.filter;

import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.filter.filters.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FilterManager {

    private final SolarityChat plugin;
    private final List<ChatFilter> filters;

    public FilterManager(SolarityChat plugin) {
        this.plugin = plugin;
        this.filters = new ArrayList<>();
        loadFilters();
    }

    public void loadFilters() {
        filters.clear();

        var config = plugin.getConfigManager().getConfig("filters");
        if (config == null || !config.contains("filters")) {
            return;
        }

        ConfigurationSection filtersSection = config.getConfigurationSection("filters");
        if (filtersSection == null) {
            return;
        }

        for (String key : filtersSection.getKeys(false)) {
            ConfigurationSection filterSection = filtersSection.getConfigurationSection(key);
            if (filterSection == null) {
                continue;
            }

            boolean enabled = filterSection.getBoolean("enabled", true);
            if (!enabled) {
                continue;
            }

            String type = filterSection.getString("type", "");
            ChatFilter filter = createFilter(key, type, filterSection);

            if (filter != null) {
                filters.add(filter);
            }
        }

        plugin.getLogger().info("Loaded " + filters.size() + " chat filters.");
    }

    private ChatFilter createFilter(String name, String type, ConfigurationSection config) {
        return switch (type.toUpperCase()) {
            case "BLOCKED_WORDS" -> new BlockedWordsFilter(plugin, name, config);
            case "REGEX" -> new RegexFilter(plugin, name, config);
            case "SPAM_REPETITION" -> new SpamRepetitionFilter(plugin, name, config);
            case "SPAM_CHARACTERS" -> new SpamCharactersFilter(plugin, name, config);
            case "CAPS" -> new CapsFilter(plugin, name, config);
            default -> {
                plugin.getLogger().warning("Unknown filter type: " + type);
                yield null;
            }
        };
    }

    public FilterResult checkMessage(Player player, String message) {
        for (ChatFilter filter : filters) {
            if (filter.check(player, message)) {
                return new FilterResult(true, filter.getName(), filter.getActions(), message);
            }
        }
        return new FilterResult(false, null, null, message);
    }

    public void reload() {
        loadFilters();
    }

    public static class FilterResult {
        private final boolean triggered;
        private final String filterName;
        private final List<String> actions;
        private final String message;

        public FilterResult(boolean triggered, String filterName, List<String> actions, String message) {
            this.triggered = triggered;
            this.filterName = filterName;
            this.actions = actions;
            this.message = message;
        }

        public boolean isTriggered() {
            return triggered;
        }
        public String getFilterName() {
            return filterName;
        }
        public List<String> getActions() {
            return actions;
        }
        public String getMessage() {
            return message;
        }

        public boolean hasAction(String action) {
            if (actions == null) {
                return false;
            }
            return actions.stream().anyMatch(a -> a.toUpperCase().startsWith(action.toUpperCase()));
        }
    }
}
