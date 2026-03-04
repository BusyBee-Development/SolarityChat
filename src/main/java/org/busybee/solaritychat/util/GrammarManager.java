package org.busybee.solaritychat.util;

import org.busybee.solaritychat.SolarityChat;

import java.util.HashMap;
import java.util.Map;

public class GrammarManager {

    private final SolarityChat plugin;
    private final Map<String, String> autoCorrections;
    private boolean autoCapitalize;
    private boolean autoPunctuate;
    private int minLength;

    public GrammarManager(SolarityChat plugin) {
        this.plugin = plugin;
        this.autoCorrections = new HashMap<>();
        loadConfig();
    }

    public void loadConfig() {
        autoCorrections.clear();
        var config = plugin.getConfig();
        autoCapitalize = config.getBoolean("grammar.auto-capitalize", true);
        autoPunctuate = config.getBoolean("grammar.auto-punctuate", true);
        minLength = config.getInt("grammar.min-length", 5);

        autoCorrections.put("dont", "don't");
        autoCorrections.put("cant", "can't");
        autoCorrections.put("im", "I'm");
        autoCorrections.put("idk", "I don't know");
        autoCorrections.put("u", "you");
        autoCorrections.put("r", "are");
    }

    public String process(String message) {
        if (message == null || message.isEmpty() || message.length() < minLength) return message;

        String[] words = message.split("\\s+");
        StringBuilder corrected = new StringBuilder();
        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z']", "");
            if (autoCorrections.containsKey(cleanWord)) {
                String replacement = autoCorrections.get(cleanWord);
                if (!word.isEmpty() && Character.isUpperCase(word.charAt(0))) {
                    replacement = Character.toUpperCase(replacement.charAt(0)) + replacement.substring(1);
                }
                corrected.append(replacement);
            } else {
                corrected.append(word);
            }
            corrected.append(" ");
        }
        message = corrected.toString().trim();

        if (message.isEmpty()) return message;

        if (autoCapitalize && Character.isLowerCase(message.charAt(0))) {
            message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
        }

        if (autoPunctuate) {
            char lastChar = message.charAt(message.length() - 1);
            if (Character.isLetterOrDigit(lastChar)) {
                message += ".";
            }
        }

        return message;
    }
}
