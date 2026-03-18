package org.busybee.solaritychat.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpyManager {
    private final Set<UUID> spyEnabled = new HashSet<>();

    public boolean isSpyEnabled(UUID playerUUID) {
        return spyEnabled.contains(playerUUID);
    }

    public void toggleSpy(UUID playerUUID) {
        if (spyEnabled.contains(playerUUID)) {
            spyEnabled.remove(playerUUID);
        } else {
            spyEnabled.add(playerUUID);
        }
    }

    public boolean enableSpy(UUID playerUUID) {
        return spyEnabled.add(playerUUID);
    }

    public boolean disableSpy(UUID playerUUID) {
        return spyEnabled.remove(playerUUID);
    }
}
