package org.busybee.solaritychat.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.busybee.solaritychat.SolarityChat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SolarityChatExpansion extends PlaceholderExpansion {

    private final SolarityChat plugin;

    public SolarityChatExpansion(SolarityChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "solaritychat";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("tag")) {
            String tag = plugin.getTagManager().getEquippedTag(player.getUniqueId());
            if (tag != null) {
                return plugin.getTagManager().getTagDisplay(tag);
            }
            return "";
        }

        if (params.equalsIgnoreCase("warnings")) {
            return String.valueOf(plugin.getWarningManager().getWarnings(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("color") || params.equalsIgnoreCase("color_code")) {
            String color = plugin.getColorManager().getPlayerColorCode(player.getUniqueId());
            return color != null ? color : "";
        }

        if (params.equalsIgnoreCase("color_id")) {
            String id = plugin.getColorManager().getPlayerColorId(player.getUniqueId());
            return id != null ? id : "";
        }

        if (params.equalsIgnoreCase("color_display")) {
            String id = plugin.getColorManager().getPlayerColorId(player.getUniqueId());
            if (id != null) {
                var def = plugin.getColorManager().getColorDefinition(id);
                return def != null ? def.displayName() : "";
            }
            return "";
        }

        if (params.startsWith("vault_")) {
            return handleVaultPlaceholder(player, params.substring(6));
        }

        return null;
    }

    private String handleVaultPlaceholder(Player player, String params) {
        if (!plugin.getVaultIntegration().isEnabled()) {
            return "";
        }

        switch (params.toLowerCase()) {
            case "eco_balance":
            case "balance":
                if (plugin.getVaultIntegration().hasEconomy()) {
                    double balance = plugin.getVaultIntegration().getBalance(player);
                    return String.valueOf((int) balance);
                }
                return "0";

            case "eco_balance_formatted":
            case "balance_formatted":
                if (plugin.getVaultIntegration().hasEconomy()) {
                    return plugin.getVaultIntegration().getFormattedBalance(player);
                }
                return "0";

            case "eco_balance_fixed":
                if (plugin.getVaultIntegration().hasEconomy()) {
                    return String.format("%.2f", plugin.getVaultIntegration().getBalance(player));
                }
                return "0.00";

            case "eco_balance_commas":
                if (plugin.getVaultIntegration().hasEconomy()) {
                    return String.format("%,.2f", plugin.getVaultIntegration().getBalance(player));
                }
                return "0.00";

            case "prefix":
                return plugin.getVaultIntegration().getPrefix(player);

            case "suffix":
                return plugin.getVaultIntegration().getSuffix(player);

            case "group":
            case "rank":
                return plugin.getVaultIntegration().getGroup(player);

            default:
                return "";
        }
    }
}
