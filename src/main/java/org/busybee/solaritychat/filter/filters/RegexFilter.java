package org.busybee.solaritychat.filter.filters;

import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.filter.ChatFilter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegexFilter extends ChatFilter {

    private final List<Pattern> patterns;
    private final List<String> whitelist;

    public RegexFilter(SolarityChat plugin, String name, ConfigurationSection config) {
        super(plugin, name, config);
        this.patterns = new ArrayList<>();
        this.whitelist = config.getStringList("whitelist");

        List<String> patternStrings = config.getStringList("patterns");
        if (patternStrings.isEmpty()) {
            String single = config.getString("pattern", "");
            if (!single.isEmpty()) {
                patternStrings = List.of(single);
            }
        }

        for (String regex : patternStrings) {
            try {
                patterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid regex in filter " + name + ": " + regex);
            }
        }
    }

    @Override
    public boolean check(Player player, String message) {
        if (!whitelist.isEmpty()) {
            String messageLower = message.toLowerCase();
            for (String allowed : whitelist) {
                if (messageLower.contains(allowed.toLowerCase())) {
                    return false;
                }
            }
        }

        for (Pattern pattern : patterns) {
            if (pattern.matcher(message).find()) {
                executeActions(player, message);
                return true;
            }
        }
        return false;
    }
}
