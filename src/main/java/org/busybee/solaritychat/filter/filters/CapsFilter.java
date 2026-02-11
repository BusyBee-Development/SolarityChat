package org.busybee.solaritychat.filter.filters;

import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.filter.ChatFilter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class CapsFilter extends ChatFilter {

    private final int percentage;
    private final int minLength;

    public CapsFilter(SolarityChat plugin, String name, ConfigurationSection config) {
        super(plugin, name, config);
        this.percentage = (int) (config.getDouble("threshold", 0.7) * 100);
        this.minLength = config.getInt("min-length", 8);
    }

    @Override
    public boolean check(Player player, String message) {
        if (message.length() < minLength) {
            return false;
        }

        int upperCount = 0;
        int letterCount = 0;

        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                letterCount++;
                if (Character.isUpperCase(c)) {
                    upperCount++;
                }
            }
        }

        if (letterCount == 0) {
            return false;
        }

        double capsPercentage = (upperCount * 100.0) / letterCount;

        if (capsPercentage >= percentage) {
            executeActions(player, message);
            return true;
        }

        return false;
    }
}