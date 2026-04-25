package org.busybee.solaritychat;

import org.bstats.bukkit.Metrics;
import org.busybee.solaritychat.announcements.AnnouncementManager;
import org.busybee.solaritychat.channels.ChannelManager;
import org.busybee.solaritychat.commands.*;
import org.busybee.solaritychat.filter.FilterManager;
import org.busybee.solaritychat.format.ChatFormatter;
import org.busybee.solaritychat.format.FormatManager;
import org.busybee.solaritychat.integration.SolarityChatExpansion;
import org.busybee.solaritychat.integration.VaultIntegration;
import org.busybee.solaritychat.listener.ChatListener;
import org.busybee.solaritychat.listeners.ColorGUIListener;
import org.busybee.solaritychat.listeners.PlayerJoinListener;
import org.busybee.solaritychat.managers.SpyManager;
import org.busybee.solaritychat.storage.ColorManager;
import org.busybee.solaritychat.storage.DatabaseManager;
import org.busybee.solaritychat.storage.WarningManager;
import org.busybee.solaritychat.tags.TagGUI;
import org.busybee.solaritychat.tags.TagManager;
import org.busybee.solaritychat.util.ConfigManager;
import org.busybee.solaritychat.util.ConfigValidator;
import org.busybee.solaritychat.util.GrammarManager;
import org.busybee.solaritychat.util.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SolarityChat extends JavaPlugin {

    private DatabaseManager databaseManager;
    private WarningManager warningManager;
    private ColorManager colorManager;
    private FilterManager filterManager;
    private ConfigManager configManager;
    private FormatManager formatManager;
    private ChatFormatter chatFormatter;
    private TagManager tagManager;
    private TagGUI tagGUI;
    private VaultIntegration vaultIntegration;
    private AnnouncementManager announcementManager;
    private GrammarManager grammarManager;
    private ChannelManager channelManager;
    private ColorCommand colorCommand;
    private SpyManager spyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfNotPresent("messages.yml");
        saveResourceIfNotPresent("chat" + File.separator + "filters.yml");
        saveResourceIfNotPresent("chat" + File.separator + "format.yml");
        saveResourceIfNotPresent("chat" + File.separator + "tags.yml");
        saveResourceIfNotPresent("chat" + File.separator + "announcements.yml");
        saveResourceIfNotPresent("chat" + File.separator + "channels.yml");
        saveResourceIfNotPresent("chat" + File.separator + "colors.yml");

        // Validate and update configs (merges new fields while preserving user values)
        try {
            ConfigValidator configValidator = new ConfigValidator(this);
            configValidator.validateAllConfigs();
        } catch (Exception e) {
            getLogger().warning("Failed to run config validator: " + e.getMessage());
            e.printStackTrace();
        }

        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        warningManager = new WarningManager(this, databaseManager);
        colorManager = new ColorManager(this, databaseManager);
        filterManager = new FilterManager(this);
        formatManager = new FormatManager(this);
        chatFormatter = new ChatFormatter(this);
        tagManager = new TagManager(this, databaseManager);
        tagGUI = new TagGUI(this, tagManager, configManager);
        announcementManager = new AnnouncementManager(this);
        grammarManager = new GrammarManager(this);
        channelManager = new ChannelManager(this);
        spyManager = new SpyManager();

        vaultIntegration = new VaultIntegration(this);
        if (vaultIntegration.setup()) {
            getLogger().info("VaultUnlocked integration enabled!");
        }

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ColorGUIListener(this), this);
        getServer().getPluginManager().registerEvents(tagGUI, this);
        getServer().getPluginManager().registerEvents(new UpdateChecker(this), this);

        getCommand("solaritychat").setExecutor(new SolarityChatCommand(this));
        getCommand("tags").setExecutor(new TagCommand(this, tagGUI));
        getCommand("clearchat").setExecutor(new ClearChatCommand());
        colorCommand = new ColorCommand(this);
        getCommand("colors").setExecutor(colorCommand);
        getCommand("channel").setExecutor(new ChannelCommand(this));

        MessageCommand msgCommand = new MessageCommand(this);
        getCommand("msg").setExecutor(msgCommand);
        getCommand("reply").setExecutor(msgCommand);
        getCommand("spy").setExecutor(new SpyCommand(this));

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SolarityChatExpansion(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        new Metrics(this, 29470);

        getLogger().info("SolarityChat has been enabled!");
    }

    @Override
    public void onDisable() {
        if (announcementManager != null) {
            announcementManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (tagManager != null) {
            tagManager.saveData();
        }
        getLogger().info("SolarityChat has been disabled!");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        filterManager.reload();
        formatManager.reload();
        tagManager.reload();
        announcementManager.reload();
        grammarManager.loadConfig();
        channelManager.loadChannels();
        if (colorManager != null) {
            colorManager.loadDefinitions();
        }
    }

    private void saveResourceIfNotPresent(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public WarningManager getWarningManager() { return warningManager; }
    public ColorManager getColorManager() { return colorManager; }
    public FilterManager getFilterManager() { return filterManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public FormatManager getFormatManager() { return formatManager; }
    public ChatFormatter getChatFormatter() { return chatFormatter; }
    public TagManager getTagManager() { return tagManager; }
    public VaultIntegration getVaultIntegration() { return vaultIntegration; }
    public AnnouncementManager getAnnouncementManager() { return announcementManager; }
    public GrammarManager getGrammarManager() { return grammarManager; }
    public ChannelManager getChannelManager() { return channelManager; }
    public SpyManager getSpyManager() { return spyManager; }
}
