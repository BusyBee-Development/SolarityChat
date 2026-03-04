package org.busybee.solaritychat.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.filter.FilterManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolarityChatCommand implements CommandExecutor, TabCompleter {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SolarityChatCommand(SolarityChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "test":
                return handleTest(sender, args);
            case "toggle-alerts":
                return handleToggleAlerts(sender);
            case "warnings":
                return handleWarnings(sender, args);
            case "setwarnings":
                return handleSetWarnings(sender, args);
            case "clearwarnings":
                return handleClearWarnings(sender, args);
            default:
                sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("solaritychat.reload")) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        plugin.reload();
        sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("reload-success")));
        return true;
    }

    private boolean handleTest(CommandSender sender, String[] args) {
        if (!sender.hasPermission("solaritychat.test")) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length < 2 || !(sender instanceof Player testPlayer)) {
            sender.sendMessage(mm.deserialize("<red>Usage: /schat test <message>"));
            return true;
        }

        String testMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        FilterManager.FilterResult result = plugin.getFilterManager().checkMessage(testPlayer, testMessage);

        if (result.isTriggered()) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("test-triggered")
                    .replace("%filter_name%", result.getFilterName())));
        } else {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("test-passed")));
        }

        return true;
    }

    private boolean handleToggleAlerts(CommandSender sender) {
        if (!sender.hasPermission("solaritychat.toggle-alerts")) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>Only players can use this command."));
            return true;
        }

        player.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("alerts-enabled")));
        return true;
    }

    private boolean handleWarnings(CommandSender sender, String[] args) {
        if (!sender.hasPermission("solaritychat.warnings.view")) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(mm.deserialize("<red>Usage: /schat warnings <player>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("player-not-found")));
            return true;
        }

        int warnings = plugin.getWarningManager().getWarnings(target.getUniqueId());

        sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("warnings-check")
                .replace("%player%", target.getName())
                .replace("%warnings%", String.valueOf(warnings))));

        return true;
    }

    private boolean handleSetWarnings(CommandSender sender, String[] args) {
        if (!sender.hasPermission("solaritychat.warnings.manage")) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(mm.deserialize("<red>Usage: /schat setwarnings <player> <amount>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("player-not-found")));
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            if (amount < 0) {
                sender.sendMessage(mm.deserialize("<red>Amount must be positive."));
                return true;
            }
            plugin.getWarningManager().setWarnings(target.getUniqueId(), target.getName(), amount);

            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("warnings-set")
                    .replace("%player%", target.getName())
                    .replace("%warnings%", String.valueOf(amount))));
        } catch (NumberFormatException e) {
            sender.sendMessage(mm.deserialize("<red>Invalid number."));
        }

        return true;
    }

    private boolean handleClearWarnings(CommandSender sender, String[] args) {
        if (!sender.hasPermission("solaritychat.warnings.manage")) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(mm.deserialize("<red>Usage: /schat clearwarnings <player>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("player-not-found")));
            return true;
        }

        plugin.getWarningManager().clearWarnings(target.getUniqueId(), target.getName());

        sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessage("warnings-cleared")
                .replace("%player%", target.getName())));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("solaritychat.reload")) subs.add("reload");
            if (sender.hasPermission("solaritychat.test")) subs.add("test");
            if (sender.hasPermission("solaritychat.toggle-alerts")) subs.add("toggle-alerts");
            if (sender.hasPermission("solaritychat.warnings.view")) subs.add("warnings");
            if (sender.hasPermission("solaritychat.warnings.manage")) {
                subs.add("setwarnings");
                subs.add("clearwarnings");
            }
            String input = args[0].toLowerCase();
            for (String sub : subs) {
                if (sub.startsWith(input)) completions.add(sub);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("warnings") || sub.equals("setwarnings") || sub.equals("clearwarnings")) {
                String input = args[1].toLowerCase();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(input)) {
                        completions.add(p.getName());
                    }
                }
            }
        }

        return completions;
    }
}
