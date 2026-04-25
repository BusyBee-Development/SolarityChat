package org.busybee.solaritychat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public static Component parse(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        String converted = legacyToMini(message);
        return MINI.deserialize(converted);
    }

    public static Component parseGui(String message) {
        return parse(message).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> parseGui(List<String> lines) {
        return lines.stream()
                .map(MessageUtil::parseGui)
                .collect(Collectors.toList());
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

                if (code == '#' && i + 7 < chars.length) {
                    String hex = text.substring(i + 2, i + 8);
                    if (hex.matches("[0-9a-fA-F]{6}")) {
                        result.append("<color:#").append(hex).append(">");
                        i += 7;
                        continue;
                    }
                }

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

                String mini = switch (Character.toLowerCase(code)) {
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
                    case 'a' -> "<green>";
                    case 'b' -> "<aqua>";
                    case 'c' -> "<red>";
                    case 'd' -> "<light_purple>";
                    case 'e' -> "<yellow>";
                    case 'f' -> "<white>";
                    case 'k' -> "<obfuscated>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underlined>";
                    case 'o' -> "<italic>";
                    case 'r' -> "<reset>";
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
