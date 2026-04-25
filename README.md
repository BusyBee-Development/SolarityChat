# SolarityChat

Advanced chat management plugin for Minecraft servers with comprehensive filtering, formatting, and moderation features.

## Features

### Chat Formatting
- **Custom Chat Formats**: Fully customizable chat format with prefix, name, suffix, and message components
- **Interactive Hover Text**: Display player statistics, balance, and playtime on hover
- **Click Actions**: Click player names to message or execute commands
- **Color & Formatting Support**: RGB hex colors, legacy color codes, and native MiniMessage support (gradients, rainbows)
- **Chat Colors GUI**: Interactive paginated menu for selecting chat colors and gradients
- **Permission-Based Formatting**: Control who can use colors and formatting codes
- **PlaceholderAPI Integration**: Full support for PAPI placeholders in chat formats, tags, and colors
- **LuckPerms Integration**: Display ranks, prefixes, and suffixes from LuckPerms
- **Vault Integration**: Show economy balance in hover text
- **Tag System**: Custom player tags with GUI selection
- **Chat Channels**: Multi-channel support (Global, Staff, Local) with easy switching

### Chat Filtering
- **Multiple Filter Types**:
  - Blocked Words: Block specific words and phrases
  - Regex Patterns: Advanced pattern matching for advertising detection
  - Spam Detection: Prevent message repetition
  - Character Spam: Limit repeated characters
  - Caps Lock: Prevent excessive uppercase messages
  
- **Pre-configured Filters**:
  - Profanity filter
  - Slurs and hate speech (auto-mute via LiteBans)
  - Self-harm encouragement (auto-mute via LiteBans)
  - Sexual content (auto-mute via LiteBans)
  - Drug references
  - Advertising (auto-ban via LiteBans)
  - Spam and character spam
  - Caps lock abuse
  - Doxxing attempts
  - Threats and DDoS mentions

- **Filter Actions**:
  - Cancel message
  - Notify staff members
  - Send message to player
  - Discord webhook notifications
  - Execute custom commands (LiteBans integration)
  - Warning system with tracking

### Moderation Features
- **Warning System**: Track player violations with persistent storage
- **Staff Alerts**: Real-time notifications for staff when filters are triggered
- **Discord Integration**: Send violation reports to Discord via webhooks with customizable embeds
- **LiteBans Integration**: Automatic mutes and bans for severe violations
- **Alert Toggle**: Staff can enable/disable filter alerts per-session

### Player Features
- **Mentions**: Tag players in chat with customizable mention format and sound
- **Chat Cooldown**: Configurable cooldown between messages
- **Tag Selection GUI**: Interactive menu to select and manage custom tags
- **Chat MOTD**: Welcome messages displayed on join

### Announcements
- **Automated Announcements**: Scheduled messages with customizable intervals
- **Multiple Announcement Types**: Discord, Store, Teams, Auction House, Buy/Sell, Unlockables, Events
- **Random or Sequential Order**: Choose announcement display order
- **Optional Sound Effects**: Play sounds with announcements
- **Rich Formatting**: Full color and formatting support in announcements

## Commands

| Command                             | Aliases       | Description                   | Permission                     |
|-------------------------------------|---------------|-------------------------------|--------------------------------|
| `/solaritychat`                     | `/schat`      | Main plugin command           | -                              |
| `/solaritychat reload`              | -             | Reload configuration files    | `solaritychat.reload`          |
| `/solaritychat test <msg>`          | -             | Test message against filters  | `solaritychat.test`            |
| `/solaritychat toggle-alerts`       | -             | Toggle staff filter alerts    | `solaritychat.toggle-alerts`   |
| `/solaritychat warnings <p>`        | -             | View player warnings          | `solaritychat.warnings.view`   |
| `/solaritychat setwarnings <p> <n>` | -             | Set player warnings           | `solaritychat.warnings.manage` |
| `/solaritychat clearwarnings <p>`   | -             | Clear player warnings         | `solaritychat.warnings.manage` |
| `/tags`                             | `/sctags`     | Open tag selection GUI        | `solaritychat.tags`            |
| `/colors`                           | `/chatcolors` | Open chat color selection GUI | `solaritychat.colors`          |
| `/channel <name>`                   | `/ch`         | Switch chat channels          | `solaritychat.channel.<name>`  |
| `/msg <p> <msg>`                    | `/whisper`    | Send a private message        | -                              |
| `/reply <msg>`                      | `/r`          | Reply to last private message | -                              |
| `/clearchat`                        | `/cc`         | Clear chat for all players    | `solaritychat.clearchat`       |

## Permissions

### Admin Permissions
- `solaritychat.reload` - Reload plugin configuration
- `solaritychat.test` - Test filter system
- `solaritychat.toggle-alerts` - Toggle filter alerts
- `solaritychat.warnings.view` - View player warnings
- `solaritychat.warnings.manage` - Manage player warnings
- `solaritychat.clearchat` - Clear chat for all players

### Formatting Permissions
- `solaritychat.format.color` - Use legacy color codes
- `solaritychat.format.hex` - Use RGB hex colors
- `solaritychat.format.bold` - Use bold formatting
- `solaritychat.format.italic` - Use italic formatting
- `solaritychat.format.underline` - Use underline formatting
- `solaritychat.format.strikethrough` - Use strikethrough formatting
- `solaritychat.format.magic` - Use magic/obfuscated formatting
- `solaritychat.format.minimessage` - Use raw MiniMessage tags in chat

### Player Permissions
- `solaritychat.tags` - Access tag commands
- `solaritychat.tags.use` - Use the tag system
- `solaritychat.colors` - Access the /colors command
- `solaritychat.color.<id>` - Access to a specific color definition
- `solaritychat.color.*` - Access to all colors
- `solaritychat.channel.<name>` - Access to join a specific channel

## PlaceholderAPI Integration

| Placeholder                    | Description                          |
|--------------------------------|--------------------------------------|
| `%solaritychat_tag%`           | Player's equipped tag display name   |
| `%solaritychat_warnings%`      | Player's total warnings              |
| `%solaritychat_color%`         | Player's selected color code         |
| `%solaritychat_color_id%`      | Player's selected color ID           |
| `%solaritychat_color_display%` | Player's selected color display name |
| `%solaritychat_vault_balance%` | Player's economy balance             |
| `%solaritychat_vault_prefix%`  | Player's Vault/LuckPerms prefix      |
| `%solaritychat_vault_suffix%`  | Player's Vault/LuckPerms suffix      |

## Configuration

### Main Config (`config.yml`)
```yaml
chat:
  cooldown: 0
  mention:
    enabled: true
    char: ""
    replacement: "<yellow>@%player_name%</yellow>"
    sound:
      name: "block.note_block.pling"
      volume: 1.0
      pitch: 1.0

discord:
  webhook-url: "your-webhook-url"
  embed:
    title: "%action_type%"
    color: "#3498db"
    description: "%player% has been %action_verb% because they said `%message%`"
    footer: "If this is false, un%action_verb% them."
    timestamp-format: "M/d/yy, h:mm a"
```

### Chat Format (`chat/format.yml`)
Define custom chat formats with hover text and click actions. Supports multiple formats with priority and permission-based selection.

### Filters (`chat/filters.yml`)
Configure all chat filters including blocked words, regex patterns, spam detection, and actions to take when triggered.

### Chat Colors (`chat/colors.yml`)
Configure the colors GUI layout, item templates, and define unlimited custom colors, gradients, and rainbows using MiniMessage format.

### Chat Channels (`chat/channels.yml`)
Define chat channels with custom formats, ranges (local chat), and join/leave permissions.

### Messages (`messages.yml`)
Customize all plugin messages and notifications.

## Dependencies

### Required
- **Spigot/Paper**: 1.20.1 or higher
- **Java**: 17 or higher

### Optional (Soft Dependencies)
- **PlaceholderAPI**: For placeholder support in chat formats
- **LuckPerms**: For rank prefixes and suffixes
- **Vault**: For economy integration
- **LiteBans**: For automatic punishment integration

## Discord Webhook Setup

1. Create a webhook in your Discord server
2. Copy the webhook URL
3. Paste it in `config.yml` under `discord.webhook-url`
4. Customize the embed format as desired
5. Reload the plugin

## Filter Configuration

Each filter supports the following actions:
- `CANCEL_MESSAGE` - Prevent the message from being sent
- `NOTIFY_STAFF` - Alert online staff members
- `TELL_PLAYER` - Send a message to the player
- `DISCORD_WEBHOOK` - Send notification to Discord
- `RUN_COMMANDS: [command1, command2]` - Execute commands (use %player% placeholder)

Example:
```yaml
slurs:
  enabled: true
  type: BLOCKED_WORDS
  words:
    - "badword"
  actions:
    - "CANCEL_MESSAGE"
    - "NOTIFY_STAFF"
    - "DISCORD_WEBHOOK"
    - "RUN_COMMANDS: [litebans:mute %player% 1d Hate Speech]"
```

## Tag System

Players can select custom tags that appear in chat after their name. Tags are configured per-player and persist across restarts. Use `/tags` to open the selection GUI.

## Chat Color System

The plugin features a robust, ID-based chat color system. Admins can define colors in `colors.yml` using modern MiniMessage tags.

- **Live Previews**: Players see a preview of how their text will look directly in the GUI.
- **State-Aware GUI**: The menu automatically shows which colors are locked, available, or currently selected.
- **Admin-Friendly**: If you change a color's code in the config, it updates for all players who have it selected without them needing to re-open the menu.
- **Pagination**: Supports infinite colors with automatic pagination.

## Chat Channels

SolarityChat supports multiple chat channels. By default, it includes:
- **Global**: Default channel for all players.
- **Staff**: Private channel for staff members (`solaritychat.channel.staff`).
- **Local**: Range-based chat for players near each other.

Use `/channel <name>` to switch your active channel.

## License

Copyright (c) 2026 Busy Bee Development. All Rights Reserved.     
