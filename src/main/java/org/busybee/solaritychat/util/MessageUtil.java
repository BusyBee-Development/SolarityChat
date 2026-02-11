package org.busybee.solaritychat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class MessageUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public static Component parse(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        String converted = legacyToMini(message);
        return MINI.deserialize(converted);
    }

    public static String colorize(String message) {
        if (message == null) return "";
        return legacyToMini(message);
    }

    public static String stripColor(String message) {
        if (message == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(parse(message));
    }

    public static String legacyToMini(String text) {
        if (text == null) return "";
        text = text.replace("§", "&");
        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length) {
                char code = chars[i + 1];
                if (code == 'x' && i + 13 < chars.length) {
                    StringBuilder hex = new StringBuilder("#");
                    boolean valid = true;
                    for (int j = 0; j < 6; j++) {
                        int idx = i + 2 + (j * 2);
                        if (idx + 1 < chars.length && chars[idx] == '&') {
                            hex.append(chars[idx + 1]);
                        } else {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        result.append("<color:").append(hex).append(">");
                        i += 13;
                        continue;
                    }
                }
                String mini = switch (code) {
                    case '0' -> "<black>";
                    case '1' -> "<dark_blue>";
                    case '2' -> "<dark_green>";
                    case '3' -> "<dark_aqua>";
                    case '4' -> "<dark_red>";
                    case '5' -> "<dark_purple>";
                    case '6' -> "<gold>";
                    case '7' -> "<gray>";
                    case '8' -> "<dark_gray>";
                    case '9' -> "<blue>";
                    case 'a', 'A' -> "<green>";
                    case 'b', 'B' -> "<aqua>";
                    case 'c', 'C' -> "<red>";
                    case 'd', 'D' -> "<light_purple>";
                    case 'e', 'E' -> "<yellow>";
                    case 'f', 'F' -> "<white>";
                    case 'k', 'K' -> "<obfuscated>";
                    case 'l', 'L' -> "<bold>";
                    case 'm', 'M' -> "<strikethrough>";
                    case 'n', 'N' -> "<underlined>";
                    case 'o', 'O' -> "<italic>";
                    case 'r', 'R' -> "<reset>";
                    default -> null;
                };
                if (mini != null) {
                    result.append(mini);
                    i++;
                    continue;
                }
            }
            result.append(chars[i]);
        }
        return result.toString();
    }
}