package org.busybee.solaritychat.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCommand implements CommandExecutor {

    private final SolarityChat plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, UUID> lastMessaged = new ConcurrentHashMap<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MessageCommand(SolarityChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (label.equalsIgnoreCase("reply") || label.equalsIgnoreCase("r")) {
            if (args.length < 1) {
                player.sendMessage(mm.deserialize("<red>Usage: /r <message>"));
                return true;
            }
            UUID targetUUID = lastMessaged.get(player.getUniqueId());
            if (targetUUID == null) {
                player.sendMessage(mm.deserialize("<red>You have no one to reply to."));
                return true;
            }
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null || !target.isOnline()) {
                player.sendMessage(mm.deserialize("<red>That player is no longer online."));
                return true;
            }
            sendMessage(player, target, String.join(" ", args));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<red>Usage: /msg <player> <message>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(mm.deserialize("<red>Player not found."));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(mm.deserialize("<red>You cannot message yourself."));
            return true;
        }

        StringBuilder msg = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            msg.append(args[i]).append(" ");
        }

        sendMessage(player, target, msg.toString().trim());
        return true;
    }

    private void sendMessage(Player sender, Player receiver, String message) {
        if (!sender.hasPermission("solaritychat.grammar.bypass")) {
            message = plugin.getGrammarManager().process(message);
        }

        if (!sender.hasPermission("solaritychat.bypass")) {
            var result = plugin.getFilterManager().checkMessage(sender, message);
            if (result.isTriggered()) {
                sender.sendMessage(mm.deserialize("<red>Your message was blocked by filters."));
                return;
            }
        }

        String sanitized = message.replace("<", "\\<").replace(">", "\\>");

        String time = LocalTime.now().format(timeFormatter);
        Component timeHover = mm.deserialize("<gray>Sent at: <yellow>" + time);

        Component senderMsg = mm.deserialize("<gray>[<yellow>me <gray>-> <yellow>" + receiver.getName() + "<gray>] ")
                .hoverEvent(HoverEvent.showText(timeHover))
                .append(mm.deserialize("<white>" + sanitized));

        Component receiverMsg = mm.deserialize("<gray>[<yellow>" + sender.getName() + " <gray>-> <yellow>me<gray>] ")
                .hoverEvent(HoverEvent.showText(timeHover))
                .append(mm.deserialize("<white>" + sanitized));

        sender.sendMessage(senderMsg);
        receiver.sendMessage(receiverMsg);

        lastMessaged.put(sender.getUniqueId(), receiver.getUniqueId());
        lastMessaged.put(receiver.getUniqueId(), sender.getUniqueId());

        Component spyMsg = mm.deserialize("<dark_gray>[Spy] <gray>" + sender.getName() + " -> " + receiver.getName() + ": <white>" + sanitized);
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("solaritychat.spy") && !staff.equals(sender) && !staff.equals(receiver)) {
                staff.sendMessage(spyMsg);
            }
        }
    }
}
