package org.busybee.solaritychat.format;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.busybee.solaritychat.SolarityChat;
import org.busybee.solaritychat.util.MessageUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChatFormatter {

    private final SolarityChat plugin;
    private final MiniMessage miniMessage;

    public ChatFormatter(SolarityChat plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public Component format(Player player, FormatManager.ChatFormat format, Component message) {
        Component result = Component.empty();

        String tagDisplay = "";
        String equippedTag = plugin.getTagManager().getEquippedTag(player.getUniqueId());
        if (equippedTag != null) {
            String tag = plugin.getTagManager().getTagDisplay(equippedTag);
            if (tag != null) {
                tagDisplay = tag;
            }
        }

        if (format.getPrefix() != null) {
            result = result.append(createComponent(format.getPrefix(), player, tagDisplay, null));
        }

        if (format.getNameComponent() != null) {
            result = result.append(createComponent(format.getNameComponent(), player, tagDisplay, null));
        }

        if (format.getSuffix() != null) {
            result = result.append(createComponent(format.getSuffix(), player, tagDisplay, null));
        }

        if (format.getMessage() != null) {
            result = result.append(createComponent(format.getMessage(), player, tagDisplay, message));
        }

        return result;
    }

    private Component createComponent(FormatManager.FormatComponent component, Player player, String tag, Component message) {
        String text = component.getText();
        text = replacePlaceholders(text, player, tag);

        Component base;
        if (text.contains("%message%") && message != null) {
            String[] parts = text.split("%message%", 2);
            Component prefix = parse(parts[0]);
            Component suffix = parts.length > 1 ? parse(parts[1]) : Component.empty();
            base = prefix.append(message).append(suffix);
        } else {
            base = parse(text);
        }

        if (!component.getHover().isEmpty()) {
            List<Component> hoverLines = new ArrayList<>();
            for (String line : component.getHover()) {
                hoverLines.add(parse(replacePlaceholders(line, player, tag)));
            }
            base = base.hoverEvent(HoverEvent.showText(Component.join(Component.newline(), hoverLines)));
        }

        if (!component.getClick().isEmpty()) {
            String clickAction = replacePlaceholders(component.getClick(), player, tag);
            if (clickAction.startsWith("SUGGEST_COMMAND:")) {
                base = base.clickEvent(ClickEvent.suggestCommand(clickAction.substring(16)));
            } else if (clickAction.startsWith("RUN_COMMAND:")) {
                base = base.clickEvent(ClickEvent.runCommand(clickAction.substring(12)));
            } else if (clickAction.startsWith("OPEN_URL:")) {
                base = base.clickEvent(ClickEvent.openUrl(clickAction.substring(9)));
            }
        }

        return base;
    }

    private String replacePlaceholders(String text, Player player, String tag) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        String color = plugin.getColorManager().getPlayerColorCode(player.getUniqueId());
        text = text.replace("%player%", player.getName())
                   .replace("%player_name%", player.getName())
                   .replace("%displayname%", player.getDisplayName())
                   .replace("%tag%", tag)
                   .replace("%color%", color != null ? color : "");
        return text;
    }

    private Component parse(String text) {
        String converted = MessageUtil.legacyToMini(text);
        return miniMessage.deserialize(converted);
    }
}
