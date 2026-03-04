package org.busybee.solaritychat.filter.filters;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.filter.ChatFilter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpamRepetitionFilter extends ChatFilter {

    private final int messageCount;
    private final Cache<UUID, List<MessageRecord>> messageHistory;

    public SpamRepetitionFilter(SolarityChat plugin, String name, ConfigurationSection config) {
        super(plugin, name, config);
        this.messageCount = config.getInt("message-count", 3);
        this.messageHistory = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean check(Player player, String message) {
        UUID uuid = player.getUniqueId();

        List<MessageRecord> history = messageHistory.get(uuid, k -> new ArrayList<>());

        long currentTime = System.currentTimeMillis();
        history.removeIf(record -> currentTime - record.timestamp > 10000);

        history.add(new MessageRecord(message, currentTime));

        if (history.size() >= messageCount) {
            String lastMessage = history.get(history.size() - 1).message;
            int identicalCount = 0;

            for (int i = history.size() - 1; i >= 0 && i >= history.size() - messageCount; i--) {
                if (history.get(i).message.equals(lastMessage)) {
                    identicalCount++;
                }
            }

            if (identicalCount >= messageCount) {
                executeActions(player, message);
                history.clear();
                return true;
            }
        }

        return false;
    }

    public void removePlayer(UUID uuid) {
        messageHistory.invalidate(uuid);
    }

    private static class MessageRecord {
        final String message;
        final long timestamp;

        MessageRecord(String message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
