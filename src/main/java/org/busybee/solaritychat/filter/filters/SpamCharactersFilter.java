package org.busybee.solaritychat.filter.filters;

import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.filter.ChatFilter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SpamCharactersFilter extends ChatFilter {

    private final int maxRepeated;

    public SpamCharactersFilter(SolarityChat plugin, String name, ConfigurationSection config) {
        super(plugin, name, config);
        this.maxRepeated = config.getInt("max-consecutive", config.getInt("max-repeated", 5));
    }

    @Override
    public boolean check(Player player, String message) {
        int count = 1;
        char lastChar = 0;

        for (char c : message.toCharArray()) {
            if (c == lastChar) {
                count++;
                if (count > maxRepeated) {
                    executeActions(player, message);
                    return true;
                }
            } else {
                count = 1;
                lastChar = c;
            }
        }

        return false;
    }
}
