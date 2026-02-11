# SolarityChat

Advanced chat management plugin for Minecraft servers with comprehensive filtering, formatting, and moderation features.

## Features

### Chat Formatting
- **Custom Chat Formats**: Fully customizable chat format with prefix, name, suffix, and message components
- **Interactive Hover Text**: Display player statistics, balance, and playtime on hover
- **Click Actions**: Click player names to view stats or execute commands
- **Color & Formatting Support**: RGB hex colors, legacy color codes, and text formatting (bold, italic, underline, strikethrough, magic)
- **Permission-Based Formatting**: Control who can use colors and formatting codes
- **PlaceholderAPI Integration**: Full support for PAPI placeholders in chat formats
- **LuckPerms Integration**: Display ranks, prefixes, and suffixes from LuckPerms
- **Vault Integration**: Show economy balance in hover text
- **Tag System**: Custom player tags with GUI selection

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

| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/solaritychat` | `/schat` | Main plugin command | - |
| `/solaritychat reload` | - | Reload configuration files | `solaritychat.reload` |
| `/solaritychat test <message>` | - | Test message against filters | `solaritychat.test` |
| `/solaritychat toggle-alerts` | - | Toggle staff filter alerts | `solaritychat.toggle-alerts` |
| `/solaritychat warnings <player>` | - | View player warnings | `solaritychat.warnings.view` |
| `/solaritychat setwarnings <player> <amount>` | - | Set player warnings | `solaritychat.warnings.manage` |
| `/solaritychat clearwarnings <player>` | - | Clear player warnings | `solaritychat.warnings.manage` |
| `/tags` | `/sctags` | Open tag selection GUI | `solaritychat.tags` |
| `/clearchat` | - | Clear chat for all players | `solaritychat.clearchat` |

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

### Player Permissions
- `solaritychat.tags` - Access tag commands
- `solaritychat.tags.use` - Use the tag system

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

### Announcements (`chat/announcements.yml`)
Set up automated announcements with customizable intervals, order, and formatting.

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

## Installation

1. Download the latest release
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/SolarityChat/`
5. Run `/solaritychat reload` to apply changes

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

## Support

For issues, feature requests, or questions:
- GitHub: https://github.com/BusyBee-Development
- Discord: Join via `/discord` command in-game

## License

Copyright (c) BusyBee Development. All rights reserved.

## Credits

**Author**: BusyBee  
**Version**: 1.0  
**API Version**: 1.20.1
