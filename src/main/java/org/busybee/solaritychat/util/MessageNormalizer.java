package org.busybee.solaritychat.util;

import java.util.HashMap;
import java.util.Map;

public class MessageNormalizer {

    private static final Map<Character, Character> LEET_MAP = new HashMap<>();

    static {
        LEET_MAP.put('4', 'a');
        LEET_MAP.put('@', 'a');
        LEET_MAP.put('3', 'e');
        LEET_MAP.put('1', 'i');
        LEET_MAP.put('!', 'i');
        LEET_MAP.put('0', 'o');
        LEET_MAP.put('5', 's');
        LEET_MAP.put('$', 's');
        LEET_MAP.put('7', 't');
        LEET_MAP.put('+', 't');
        LEET_MAP.put('8', 'b');
        LEET_MAP.put('9', 'g');
        LEET_MAP.put('v', 'u');
    }

    public static String normalize(String message) {
        if (message == null) return "";

        StringBuilder normalized = new StringBuilder();
        String lower = message.toLowerCase();

        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);

            c = LEET_MAP.getOrDefault(c, c);

            if (Character.isLetterOrDigit(c)) {
                if (normalized.length() >= 2 && 
                    normalized.charAt(normalized.length() - 1) == c && 
                    normalized.charAt(normalized.length() - 2) == c) {
                    continue;
                }
                normalized.append(c);
            }
        }

        return normalized.toString();
    }
}
