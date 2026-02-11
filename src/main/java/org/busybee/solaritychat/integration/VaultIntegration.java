package org.busybee.solaritychat.integration;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultIntegration {

    private final JavaPlugin plugin;
    private Chat chat;
    private Economy economy;
    private Permission permission;
    private boolean enabled;

    public VaultIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null
                && plugin.getServer().getPluginManager().getPlugin("VaultUnlocked") == null) {
            return false;
        }

        RegisteredServiceProvider<Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        enabled = (chat != null || economy != null || permission != null);
        return enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public double getBalance(Player player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    public String getFormattedBalance(Player player) {
        if (economy != null) {
            return economy.format(economy.getBalance(player));
        }
        return "0";
    }

    public String getPrefix(Player player) {
        if (chat != null) {
            String prefix = chat.getPlayerPrefix(player);
            return prefix != null ? prefix : "";
        }
        return "";
    }

    public String getSuffix(Player player) {
        if (chat != null) {
            String suffix = chat.getPlayerSuffix(player);
            return suffix != null ? suffix : "";
        }
        return "";
    }

    public String getGroup(Player player) {
        if (permission != null) {
            String group = permission.getPrimaryGroup(player);
            return group != null ? group : "";
        }
        return "";
    }
}