# TowerDef-Kt

A fully-featured Tower Defense game plugin for Minecraft Paper servers, written in Kotlin.

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21+-blue.svg)](https://papermc.io/)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21--RC2-purple.svg)](https://kotlinlang.org/)

## Description

TowerDef-Kt brings the classic tower defense gameplay experience to Minecraft. Players work together to defend against waves of enemies by strategically placing towers, managing resources, and upgrading their defenses. The plugin features a complete game management system with multiple games, custom paths, wave sequences, player statistics, and an in-game shop system.

## Features

### Core Gameplay
- **Multiple Game Instances** - Run multiple independent tower defense games simultaneously
- **Wave System** - Customizable wave sequences with different enemy types and spawn patterns
- **Tower Placement** - Strategic tower placement with range indicators and upgrade system
- **Path System** - Visual path creator with multiple paths per game and checkpoint-based navigation
- **Player Statistics** - Track kills, damage dealt, towers placed, and cash per player
- **Economy System** - Earn cash from damage dealt and wave completion to buy towers and upgrades
- **Shop System** - In-game villager shops for purchasing towers and upgrades

### Advanced Systems
- **Custom Health System** - Enemies use custom health values independent of Minecraft's vanilla system
- **Health Bar Display** - Floating health bars above enemies showing current HP
- **Tower Upgrades** - Multi-level tower upgrade system with increasing damage and fire rate
- **Game Stats Display** - Real-time game statistics displayed at lecterns
- **Player HUD** - Actionbar display showing current cash and game statistics
- **Debug System** - Comprehensive debug logging with category-based toggles

### Game Management
- **Save/Load System** - Games are automatically saved and loaded from YAML files
- **Path Serialization** - Paths are saved with games and restored on server restart
- **Visual Path Editor** - In-game path creation and modification with armor stand markers
- **Game Configuration** - Customizable health, starting cash, waves, and allowed towers per game
- **Tower Sell System** - Configurable refund percentage when selling towers

### Technical Features
- **Instance-Based Architecture** - Each game runs as an independent instance
- **Entity Tracking System** - Robust tracking of which entities belong to which game
- **Wave Command System** - Flexible wave sequences using wait and spawn commands
- **Enemy Registry** - Centralized enemy configuration with health and speed values
- **Tower Registry** - Extensible tower system for easy addition of new tower types

## Gameplay

### Starting a Game
1. Create a game using the menu system (`/tdmenu`)
2. Set up paths with start points, checkpoints, and end points
3. Configure wave sequences and allowed towers
4. Join the game and start when ready
5. Place towers to defend against waves of enemies
6. Earn cash from dealing damage and completing waves
7. Upgrade towers to increase effectiveness
8. Survive all waves to win!

### Tower Mechanics
- **Placement**: Right-click with a tower item to place
- **Upgrades**: Sneak + Right-click on a tower to upgrade (costs cash)
- **Range**: Towers automatically target the closest enemy within range
- **Damage**: Earn cash equal to the damage you deal
- **Selling**: Shift + Left-click to sell towers (configurable refund %)

### Enemy Mechanics
- **Path Following**: Enemies follow the defined path checkpoints
- **Custom Health**: Enemies have custom health pools (e.g., 100+ HP)
- **Speed Variants**: Different enemy types move at different speeds
- **End Point**: Enemies deal damage when reaching the end point
- **Visual Feedback**: Health bars display current HP

## Commands

### Player Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/tdmenu` | Open the game management menu | `towerdef.menu` |
| `/tdgenerator <type>` | Access various generator menus | `towerdef.generator` |
| `/giveStatsTracker` | Get a stats tracker item | `towerdef.stats` |

### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/giveTDtower [player]` | Give a tower item | `towerdef.admin` |
| `/giveTDenemy [player]` | Give an enemy spawn item | `towerdef.admin` |
| `/stopgame <gameId>` | Stop a running game | `towerdef.admin` |
| `/nextwave <gameId>` | Skip to the next wave | `towerdef.admin` |
| `/spawnenemy <gameId> <enemyType>` | Manually spawn an enemy | `towerdef.admin` |
| `/givecash <gameId> <player> <amount>` | Give cash to a player | `towerdef.admin` |
| `/addplayer <gameId> <player>` | Add a player to a game | `towerdef.admin` |
| `/giveShopVillager` | Get a shop villager spawn egg | `towerdef.admin` |
| `/clearTDalltowers` | Remove all towers | `towerdef.admin` |
| `/clearTDallenemies` | Remove all enemies | `towerdef.admin` |
| `/clearTDallwaypoints` | Remove all waypoints | `towerdef.admin` |
| `/toggleStandVisibility` | Toggle armor stand visibility | `towerdef.admin` |

### Debug Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/tddebug toggle <category>` | Toggle debug logging | `towerdef.debug` |
| `/tddebug status` | View current debug settings | `towerdef.debug` |

**Debug Categories:**
- `master` - Enable all debug output
- `waves` - Wave spawning and progression
- `enemies` - Enemy lifecycle and movement
- `towers` - Tower placement and targeting
- `game` - Game state changes
- `stats` - Player statistics and rewards
- `paths` - Path creation and navigation

## 🛠️ Configuration

### Game Configuration (per game)
```yaml
game-data:
  name: "My Tower Defense Game"
  maxHealth: 100
  defaultCash: 500
  towerSellRefundPercentage: 80
  allowedTowers:
    - "Basic_Tower_1"
  waves:
    - name: "Wave 1"
      cashGiven: 100
      sequence:
        - type: "ENEMY_SPAWN"
          enemies:
            "Basic_Enemy_1": 10
          intervalSeconds: 2.0
        - type: "WAIT"
          waitSeconds: 5.0
  paths:
    - id: 1
      name: "Main Path"
      startPoint: { world: "world", x: 100, y: 64, z: 100 }
      checkpoints: []
      endPoint: { world: "world", x: 200, y: 64, z: 100 }
      isVisible: true
```

### Plugin Configuration (config.yml)
```yaml
debug:
  master: false
  waves: false
  enemies: false
  towers: false
  game: false
  stats: false
  paths: false
```

### Enemy Registry Configuration
Enemies are defined in the code with configurable properties:
- Health (custom health pool)
- Speed (movement speed multiplier)
- Display name and appearance

### Tower Registry Configuration
Towers have configurable properties:
- Range (detection radius)
- Damage (per hit)
- Attack speed (seconds between attacks)
- Upgrade paths and costs

## File Structure

```
plugins/TowerDef-Kt/
├── config.yml                 # Debug settings
└── games/                     # Saved games
    ├── game_1.yml
    ├── game_2.yml
    └── ...
```

## Installation

### Server Requirements
- **Minecraft Server**: Paper 1.21 or higher
- **Java**: Version 21 or higher
- **RAM**: Minimum 2GB (4GB+ recommended for multiple games)

### Installation Steps
1. Download the latest release JAR from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. The plugin will generate default configuration files
5. Configure games using the in-game menu system

## Building from Source

### Prerequisites
- JDK 21 or higher
- Git

### Build Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/TowerDef-Kt.git
   cd TowerDef-Kt
   ```

2. **Build the plugin:**
   ```bash
   ./gradlew build
   ```
   On Windows:
   ```cmd
   gradlew.bat build
   ```

3. **Locate the JAR:**
   The compiled JAR will be in `build/libs/towerdef-1.0-SNAPSHOT-all.jar`

### Development Build
For development with auto-reload:
```bash
./gradlew shadowJar
```

## Usage Examples

### Creating a New Game
1. Run `/tdmenu` to open the game management menu
2. Click "Create New Game"
3. Configure game settings (name, health, cash, etc.)
4. Save the game

### Setting Up Paths
1. Open the path editor in the game menu
2. Click "Create New Path"
3. Right-click blocks to set start point
4. Right-click to add checkpoints
5. Right-click to set end point
6. Save the path

### Configuring Waves
1. Open the wave editor in the game menu
2. Click "Add Wave"
3. Add spawn commands with enemy types and quantities
4. Add wait commands for delays between spawns
5. Set wave rewards (cash given on completion)
6. Save the wave configuration

### Starting a Game
1. Players join using the game menu
2. Admin or game creator starts the game
3. Players place towers using tower items
4. Waves spawn automatically
5. Defend until all waves are complete or base health reaches zero

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `towerdef.menu` | Access game menus | op |
| `towerdef.play` | Join and play games | all |
| `towerdef.admin` | Admin commands | op |
| `towerdef.debug` | Debug commands | op |
| `towerdef.generator` | Access generators | op |
| `towerdef.stats` | View statistics | all |

## Debug System

The plugin includes a comprehensive debug system for troubleshooting:

### Enabling Debug Output
```
/tddebug toggle master  # Enable all debug output
```

### Category-Specific Debugging
```
/tddebug toggle waves    # Debug wave system
/tddebug toggle enemies  # Debug enemy spawning/movement
/tddebug toggle towers   # Debug tower placement/targeting
/tddebug toggle game     # Debug game lifecycle
/tddebug toggle stats    # Debug player statistics
/tddebug toggle paths    # Debug path system
```

### What Gets Logged
- Game start/stop events
- Wave progression and completion
- Enemy spawning with configuration details
- Tower placement and upgrade operations
- Entity cleanup and orphan detection
- Cash rewards and player statistics
- Path creation and modification
- Error conditions and warnings

## Player Statistics

The plugin tracks comprehensive player statistics per game:
- **Total Cash Earned** - Cumulative cash from all sources
- **Current Cash** - Available cash for purchases
- **Kills** - Number of enemies eliminated
- **Towers Placed** - Total towers placed in the game
- **Damage Dealt** - Total damage dealt to enemies
- **Waves Completed** - Number of waves survived

Statistics are displayed:
- In the player's HUD (actionbar)
- At stats tracker lecterns
- In game-end summaries

## Wave System

### Wave Configuration
Waves consist of a sequence of commands:
- **ENEMY_SPAWN**: Spawn enemies with intervals
- **WAIT**: Pause before next command

### Example Wave Sequence
```yaml
sequence:
  - type: "ENEMY_SPAWN"
    enemies:
      "Basic_Enemy_1": 5
      "Fast_Enemy_1": 3
    intervalSeconds: 1.5
  - type: "WAIT"
    waitSeconds: 3.0
  - type: "ENEMY_SPAWN"
    enemies:
      "Tank_Enemy_1": 2
    intervalSeconds: 2.0
```

### Wave Completion
- Wave completes when all enemies are spawned AND eliminated
- Players receive cash rewards upon completion
- Short delay before next wave begins
- Game won when all waves completed

## Tower System

### Tower Types
Currently implemented:
- **Basic Tower**: Standard damage, medium range

### Tower Properties
- **Range**: Detection radius for enemies
- **Damage**: Amount of damage per attack
- **Attack Speed**: Seconds between attacks
- **Upgrade Levels**: Multiple levels with increasing power

### Tower Upgrades
- Sneak + Right-click on tower to upgrade
- Each level increases damage and fire rate
- Costs cash based on upgrade level
- Visual feedback on upgrade success/failure

## Enemy System

### Enemy Types
Enemies are defined with:
- **Health**: Custom health pool (e.g., 20-200 HP)
- **Speed**: Movement speed multiplier
- **Type ID**: Unique identifier for registry

### Enemy Behavior
- Follow defined path checkpoints
- Use custom health system
- Display floating health bars
- Deal damage when reaching end point
- Drop no items on death

## Path System

### Path Features
- Multiple paths per game
- Visual editor with armor stand markers
- Checkpoint-based navigation
- Save/load with game configuration
- Toggle visibility per path
- Random path selection for variety

### Path Components
- **Start Point**: Where enemies spawn (green marker)
- **Checkpoints**: Waypoints enemies follow (yellow markers)
- **End Point**: Where enemies cause damage (red marker)

## Economy System

### Earning Cash
- Deal damage to enemies (1 cash per damage point)
- Complete waves (configurable reward per wave)
- Starting cash on game join

### Spending Cash
- Place towers (item cost)
- Upgrade towers (increasing costs per level)
- Purchase from shops (future feature)

### Cash Management
- Tracked per player per game
- Displayed in HUD
- Persists during game session
- Reset on game end

## Contributing

Contributions are welcome! Here's how you can help:

1. **Report Bugs**: Open an issue with detailed reproduction steps
2. **Suggest Features**: Describe new features or improvements
3. **Submit Pull Requests**: Fork, create a branch, and submit PR
4. **Improve Documentation**: Help clarify or expand documentation
5. **Test**: Test new features and report issues

### Development Setup
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Test thoroughly
5. Commit with clear messages (`git commit -m 'Add amazing feature'`)
6. Push to branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## License

This project is licensed under the terms specified in the LICENSE file.

## Roadmap

### Known Issues
- None currently reported

## API Information

Developers can extend TowerDef-Kt by:
- Creating custom tower types in `TowerRegistry`
- Adding enemy types in `EnemyRegistry`
- Implementing custom wave commands
- Extending the menu system
- Adding custom game modifiers

## Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/TowerDef-Kt/issues)
- **Wiki**: [GitHub Wiki](https://github.com/yourusername/TowerDef-Kt/wiki)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/TowerDef-Kt/discussions)

## Acknowledgments

- Built with [Paper API](https://papermc.io/)
- Powered by [Kotlin](https://kotlinlang.org/)
- Uses [Adventure](https://docs.advntr.dev/) for text components