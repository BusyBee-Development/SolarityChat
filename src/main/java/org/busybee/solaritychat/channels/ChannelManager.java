package org.busybee.solaritychat.channels;

import org.busybee.solaritychat.SolarityChat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {

    private final SolarityChat plugin;
    private final Map<String, ChatChannel> channels;
    private final Map<UUID, String> playerChannels;
    private String defaultChannel;

    public ChannelManager(SolarityChat plugin) {
        this.plugin = plugin;
        this.channels = new HashMap<>();
        this.playerChannels = new ConcurrentHashMap<>();
        loadChannels();
    }

    public void loadChannels() {
        channels.clear();
        var config = plugin.getConfigManager().getConfig("channels");
        if (config == null) {
            createDefaultChannels();
            return;
        }

        defaultChannel = config.getString("default-channel", "global");
        ConfigurationSection section = config.getConfigurationSection("channels");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection chanSection = section.getConfigurationSection(key);
                if (chanSection == null) continue;

                ChatChannel channel = new ChatChannel(
                        key,
                        chanSection.getString("name", key),
                        chanSection.getString("prefix", "[" + key + "]"),
                        chanSection.getString("permission", ""),
                        chanSection.getInt("radius", -1),
                        chanSection.getBoolean("auto-join", true)
                );
                channels.put(key.toLowerCase(), channel);
            }
        }
    }

    private void createDefaultChannels() {
        channels.put("global", new ChatChannel("global", "Global", "<gray>[G]</gray>", "", -1, true));
        channels.put("local", new ChatChannel("local", "Local", "<yellow>[L]</yellow>", "", 100, true));
        channels.put("staff", new ChatChannel("staff", "Staff", "<red>[Staff]</red>", "solaritychat.channel.staff", -1, false));
        defaultChannel = "global";
    }

    public ChatChannel getChannel(String name) {
        return channels.get(name.toLowerCase());
    }

    public ChatChannel getPlayerChannel(Player player) {
        String channelName = playerChannels.getOrDefault(player.getUniqueId(), defaultChannel);
        ChatChannel channel = getChannel(channelName);
        return channel != null ? channel : getChannel(defaultChannel);
    }

    public void setPlayerChannel(Player player, String channelName) {
        if (channels.containsKey(channelName.toLowerCase())) {
            playerChannels.put(player.getUniqueId(), channelName.toLowerCase());
        }
    }

    public void removePlayer(UUID uuid) {
        playerChannels.remove(uuid);
    }

    public Collection<ChatChannel> getAvailableChannels() {
        return channels.values();
    }

    public static class ChatChannel {
        private final String id;
        private final String name;
        private final String prefix;
        private final String permission;
        private final int radius;
        private final boolean autoJoin;

        public ChatChannel(String id, String name, String prefix, String permission, int radius, boolean autoJoin) {
            this.id = id;
            this.name = name;
            this.prefix = prefix;
            this.permission = permission;
            this.radius = radius;
            this.autoJoin = autoJoin;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getPrefix() { return prefix; }
        public String getPermission() { return permission; }
        public int getRadius() { return radius; }
        public boolean isAutoJoin() { return autoJoin; }
    }
}