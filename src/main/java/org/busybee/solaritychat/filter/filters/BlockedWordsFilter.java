package org.busybee.solaritychat.filter.filters;

import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.filter.ChatFilter;
import org.busybee.solaritychat.util.FuzzyMatcher;
import org.busybee.solaritychat.util.MessageNormalizer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class BlockedWordsFilter extends ChatFilter {

    private final List<String> blockedWords;
    private final List<String> normalizedBlockedWords;
    private final boolean useSmartFilter;
    private final boolean useFuzzyMatching;
    private final double fuzzyThreshold;

    public BlockedWordsFilter(SolarityChat plugin, String name, ConfigurationSection config) {
        super(plugin, name, config);
        this.blockedWords = config.getStringList("words");
        this.useSmartFilter = config.getBoolean("smart-filter", true);
        this.useFuzzyMatching = config.getBoolean("fuzzy-matching", true);
        this.fuzzyThreshold = config.getDouble("fuzzy-threshold", 0.85);
        
        this.normalizedBlockedWords = blockedWords.stream()
                .map(MessageNormalizer::normalize)
                .collect(Collectors.toList());
    }

    @Override
    public boolean check(Player player, String message) {
        String messageLower = message.toLowerCase();
        String normalizedMessage = MessageNormalizer.normalize(message);
        String[] words = messageLower.split("\\s+");
        String[] normalizedWords = normalizedMessage.split("(?<=\\G.{4})"); // Split into chunks for fuzzy check

        // 1. Check every blocked word
        for (int i = 0; i < blockedWords.size(); i++) {
            String blockedWord = blockedWords.get(i).toLowerCase();
            String normalizedBlocked = normalizedBlockedWords.get(i);

            // Exact or Contains check
            if (messageLower.contains(blockedWord) || (useSmartFilter && normalizedMessage.contains(normalizedBlocked))) {
                executeActions(player, message);
                return true;
            }

            // Fuzzy check (compares individual words in the message)
            if (useFuzzyMatching) {
                for (String word : words) {
                    if (FuzzyMatcher.isSimilar(word, blockedWord, fuzzyThreshold)) {
                        executeActions(player, message);
                        return true;
                    }
                }
                
                // Also check normalized chunks
                if (normalizedBlocked.length() >= 4) {
                    for (int j = 0; j <= normalizedMessage.length() - normalizedBlocked.length(); j++) {
                        String sub = normalizedMessage.substring(j, j + normalizedBlocked.length());
                        if (FuzzyMatcher.isSimilar(sub, normalizedBlocked, fuzzyThreshold)) {
                            executeActions(player, message);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
