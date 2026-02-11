package org.busybee.solaritychat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MiniMessageUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public static Component deserialize(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        return MINI.deserialize(MessageUtil.legacyToMini(message));
    }

    public static String serialize(Component component) {
        if (component == null) return "";
        return MINI.serialize(component);
    }
}