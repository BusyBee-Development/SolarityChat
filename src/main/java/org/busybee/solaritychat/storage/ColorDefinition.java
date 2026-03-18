package org.busybee.solaritychat.storage;

import net.kyori.adventure.text.Component;
import org.busybee.solaritychat.util.MessageUtil;

public record ColorDefinition(
    String id,
    String displayName,
    String material,
    String code,
    String permission
) {
    public Component getParsedDisplayName() {
        return MessageUtil.parse(displayName);
    }
}
