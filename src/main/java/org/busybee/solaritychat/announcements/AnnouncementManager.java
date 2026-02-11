package org.busybee.solaritychat.announcements;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AnnouncementManager {

    private final SolarityChat plugin;
    private FileConfiguration announcementsConfig;
    private BukkitTask announcementTask;

    private boolean enabled;
    private boolean random;
    private int interval;
    private List<String> order;
    private boolean soundEnabled;
    private String soundName;

    private int currentIndex = 0;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AnnouncementManager(SolarityChat plugin) {
        this.plugin = plugin;
        loadAnnouncements();
    }

    public void loadAnnouncements() {
        File announcementsFile = new File(plugin.getDataFolder(), "chat/announcements.yml");
        if (!announcementsFile.exists()) {
            plugin.saveResource("chat/announcements.yml", false);
        }
        announcementsConfig = YamlConfiguration.loadConfiguration(announcementsFile);

        enabled = announcementsConfig.getBoolean("enabled", true);
        random = announcementsConfig.getBoolean("random", false);
        interval = announcementsConfig.getInt("interval", 360);
        order = announcementsConfig.getStringList("order");
        soundEnabled = announcementsConfig.getBoolean("sound.enabled", false);
        soundName = announcementsConfig.getString("sound.sound", "ENTITY_EXPERIENCE_ORB_PICKUP").toUpperCase();

        if (announcementTask != null) {
            announcementTask.cancel();
            announcementTask = null;
        }

        if (enabled && !order.isEmpty()) {
            scheduleAnnouncements();
        }
    }

    private void scheduleAnnouncements() {
        announcementTask = new BukkitRunnable() {
            @Override
            public void run() {
                broadcastNextAnnouncement();
            }
        }.runTaskTimer(plugin, 20L * interval, 20L * interval);
    }

    private void broadcastNextAnnouncement() {
        if (order.isEmpty()) {
            return;
        }

        String announcementKey;
        if (random) {
            announcementKey = order.get(ThreadLocalRandom.current().nextInt(order.size()));
        } else {
            if (currentIndex >= order.size()) {
                currentIndex = 0;
            }
            announcementKey = order.get(currentIndex);
            currentIndex++;
        }

        List<String> messages = announcementsConfig.getStringList("announcements." + announcementKey);
        if (messages.isEmpty()) {
            return;
        }

        org.bukkit.Sound resolvedSound = null;
        if (soundEnabled) {
            try {
                resolvedSound = org.bukkit.Sound.valueOf(soundName);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid announcement sound: " + soundName);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String message : messages) {
                player.sendMessage(MessageUtil.parse(message));
            }
            if (resolvedSound != null) {
                player.playSound(player.getLocation(), resolvedSound, 1.0f, 1.0f);
            }
        }
    }

    public void shutdown() {
        if (announcementTask != null) {
            announcementTask.cancel();
            announcementTask = null;
        }
    }

    public void reload() {
        loadAnnouncements();
    }
}