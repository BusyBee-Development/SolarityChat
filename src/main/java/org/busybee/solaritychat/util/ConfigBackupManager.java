package org.busybee.solaritychat.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class ConfigBackupManager {

    private final Plugin plugin;
    private final File backupDir;
    private static final int MAX_BACKUPS = 5;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    public ConfigBackupManager(Plugin plugin) {
        this.plugin = plugin;
        this.backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }

    public File createBackup(File configFile) {
        return createBackup(configFile, false);
    }

    public File createBackup(File configFile, boolean isCorrupted) {
        if (!configFile.exists()) {
            return null;
        }

        try {
            String timestamp = DATE_FORMAT.format(new Date());
            String fileName = configFile.getName().replace(".yml", "");
            String suffix = isCorrupted ? ".corrupted-" : "-";
            String backupFileName = fileName + suffix + timestamp + ".yml";
            File backupFile = new File(backupDir, backupFileName);

            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Created " + (isCorrupted ? "corrupted file backup" : "backup") + ": backups/" + backupFileName);

            if (!isCorrupted) {
                cleanupOldBackups(fileName);
            }

            return backupFile;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create backup for " + configFile.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private void cleanupOldBackups(String configBaseName) {
        File[] backups = backupDir.listFiles((dir, name) ->
            name.startsWith(configBaseName + "-") && name.endsWith(".yml"));

        if (backups == null || backups.length <= MAX_BACKUPS) {
            return;
        }

        Arrays.sort(backups, Comparator.comparingLong(File::lastModified));

        int toDelete = backups.length - MAX_BACKUPS;
        for (int i = 0; i < toDelete; i++) {
            if (backups[i].delete()) {
                plugin.getLogger().fine("Deleted old backup: " + backups[i].getName());
            }
        }
    }

    public File getBackupDir() {
        return backupDir;
    }
}
