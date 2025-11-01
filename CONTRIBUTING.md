# Contributing to TowerDef-Kt

Thank you for your interest in contributing to TowerDef-Kt! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Bug Reports](#bug-reports)
- [Feature Requests](#feature-requests)
- [Testing](#testing)
- [Documentation](#documentation)

## Code of Conduct

### Pledge

Don't be a dick, and neither will be.

### Unacceptable Behavior

Basically: Don't be dumb, have some common sense.

- Harassment, discrimination, or offensive comments
- Trolling, insulting, or derogatory remarks
- Publishing others' private information without permission
- Any conduct that would be inappropriate in a professional setting

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **JDK 21 or higher** - [Download](https://adoptium.net/)
- **Git** - [Download](https://git-scm.com/downloads)
- **IntelliJ IDEA** (recommended) or another Kotlin-compatible IDE
- **Paper 1.21+ Test Server** (for testing)

### Fork and Clone

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/TowerDef-Kt.git
   cd TowerDef-Kt
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/TowerDef-Kt.git
   ```

## Development Setup

### Building the Project

```bash
# Build the plugin
./gradlew build

# Build without tests (faster)
./gradlew build -x test

# Create shadow JAR only
./gradlew shadowJar
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "ClassName"
```

### Setting Up a Test Server

1. Create a test server directory
2. Download Paper 1.21+ JAR
3. Run the server once to generate files
4. Copy your built plugin JAR to the `plugins` folder
5. Restart the server

### IDE Setup (IntelliJ IDEA)

1. Open IntelliJ IDEA
2. Select "Open" and choose the project directory
3. Wait for Gradle to sync
4. Enable Kotlin plugin if not already enabled
5. Set project SDK to Java 21

## How to Contribute

### Types of Contributions

- **Bug Fixes** - Fix issues in existing code
- **New Features** - Add new functionality
- **Documentation** - Improve or add documentation
- **Code Quality** - Refactor or optimize existing code
- **Tests** - Add or improve test coverage
- **Translations** - Add language support (future)

### Contribution Workflow

1. **Create an issue** (if one doesn't exist) describing the bug or feature
2. **Discuss** your approach in the issue before starting work on large changes
3. **Create a branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```
4. **Make your changes** following our coding standards
5. **Test your changes** thoroughly
6. **Commit** with clear, descriptive messages
7. **Push** to your fork
8. **Open a Pull Request** with a detailed description

## Coding Standards

### Kotlin Style Guide

This repo follows the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with some project-specific additions:

#### Naming Conventions

```kotlin
// Classes: PascalCase
class GameManager { }

// Functions: camelCase
fun startGame() { }

// Constants: UPPER_SNAKE_CASE
const val MAX_PLAYERS = 10

// Properties: camelCase
val playerCount: Int

// Private properties: camelCase with underscore prefix (optional)
private val _internalState: Int
```

#### Code Organization

```kotlin
class Example {
    // 1. Companion object
    companion object {
        // Constants and static members
    }
    
    // 2. Properties
    private val privateProperty: String
    val publicProperty: Int
    
    // 3. Init blocks
    init {
        // Initialization code
    }
    
    // 4. Public functions
    fun publicFunction() { }
    
    // 5. Private functions
    private fun privateFunction() { }
}
```

#### Documentation

Use KDoc for public APIs:

```kotlin
/**
 * Starts a new game instance with the given players
 *
 * @param players List of player UUIDs to join the game
 * @param gameId The ID of the game to start
 * @return true if the game started successfully, false otherwise
 * @throws IllegalStateException if the game is already running
 */
fun startGame(players: List<UUID>, gameId: Int): Boolean {
    // Implementation
}
```

#### Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Max 120 characters (soft limit)
- **Blank Lines**: Use to separate logical blocks
- **Imports**: Organize and remove unused imports
- **Trailing Whitespace**: Remove all trailing whitespace

### File Structure

Organize files by feature:

```
src/main/kotlin/dev/etran/towerDefMc/
├── commands/          # Command executors
├── data/             # Data classes and models
├── factories/        # Entity factories
├── listeners/        # Event listeners
├── managers/         # Game logic managers
├── menus/            # GUI menus
├── registries/       # Registry systems
├── schedulers/       # Scheduled tasks
└── utils/            # Utility functions
```

### Error Handling

```kotlin
// Use Result type for operations that can fail
fun loadGame(gameId: Int): Result<GameManager> {
    return try {
        val game = // load game
        Result.success(game)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Log errors appropriately
try {
    // risky operation
} catch (e: Exception) {
    plugin.logger.severe("Failed to perform operation: ${e.message}")
    DebugLogger.logGame("Error details: ${e.stackTraceToString()}")
}
```

### Debug Logging

Always add appropriate debug logging for new features:

```kotlin
DebugLogger.logGame("Starting game $gameId with ${players.size} players")
DebugLogger.logWave("Wave $waveNumber spawning ${enemies.size} enemies")
DebugLogger.logTower("Tower placed at ${location.blockX}, ${location.blockY}, ${location.blockZ}")
```

## Commit Guidelines

### Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes
- `ci`: CI/CD changes

#### Examples

```
feat(towers): add sniper tower with long range

Implement new tower type with extended range but slower fire rate.
Includes upgrade path and registry entry.

Closes #123
```

```
fix(waves): prevent enemy duplication on path switch

Fixed bug where enemies could duplicate when switching between
checkpoints under certain timing conditions.

Fixes #456
```

```
docs(readme): update installation instructions

Added detailed steps for Windows users and troubleshooting section.
```

### Commit Best Practices

- **One logical change per commit**
- **Write clear, descriptive messages**
- **Reference issues** when applicable (`Fixes #123`, `Closes #456`)
- **Keep commits focused** and atomic
- **Test before committing**

## Pull Request Process

### Before Submitting

- [ ] Code follows our style guidelines
- [ ] All tests pass locally
- [ ] Added tests for new functionality
- [ ] Updated documentation (README, KDoc, etc.)
- [ ] Debug logging added where appropriate
- [ ] No merge conflicts with `main` branch
- [ ] Self-review completed

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issues
Fixes #(issue number)

## Changes Made
- Change 1
- Change 2
- Change 3

## Testing
Describe how you tested the changes

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-reviewed
- [ ] Commented complex code
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests added/updated
- [ ] Tests pass locally
```

### Review Process

1. **Automated Checks**: CI/CD will run tests and checks
2. **Code Review**: Maintainers will review your code
3. **Discussion**: Address feedback and questions
4. **Approval**: Once approved, your PR will be merged

### After Merge

- Your contribution will be included in the next release
- You'll be added to the contributors list
- Delete your feature branch (optional)

## Bug Reports

### Before Reporting

1. **Search existing issues** to avoid duplicates
2. **Test with latest version** to ensure bug still exists
3. **Gather information** about the bug

### Bug Report Template

```markdown
**Description**
Clear and concise description of the bug

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '...'
3. See error

**Expected Behavior**
What you expected to happen

**Actual Behavior**
What actually happened

**Environment**
- Server: Paper 1.21.x
- Plugin Version: 1.0-SNAPSHOT
- Java Version: 21
- Other Plugins: List relevant plugins

**Logs/Screenshots**
Attach relevant logs or screenshots

**Additional Context**
Any other information about the problem
```

## Feature Requests

### Before Requesting

1. **Check existing feature requests**
2. **Consider scope** - Does it fit the plugin's goals?
3. **Think about implementation** - Is it technically feasible?

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
Clear description of the problem

**Describe the solution you'd like**
Clear description of what you want to happen

**Describe alternatives you've considered**
Any alternative solutions or features

**Additional context**
Any other context, mockups, or examples

**Implementation Ideas**
Thoughts on how this could be implemented (optional)
```

## Testing

### Writing Tests

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GameManagerTest {
    @Test
    fun `should start game with valid players`() {
        // Arrange
        val gameManager = GameManager(1, mockConfig)
        val players = listOf(UUID.randomUUID())
        
        // Act
        gameManager.startGame(players)
        
        // Assert
        assertTrue(gameManager.isGameRunning)
        assertEquals(1, gameManager.playerCount)
    }
}
```

### Test Coverage Goals

- **New Features**: 80%+ coverage
- **Bug Fixes**: Add test that would have caught the bug
- **Critical Systems**: 90%+ coverage

### Manual Testing

For features that require in-game testing:

1. Build the plugin
2. Install on test server
3. Test all related functionality
4. Test edge cases
5. Document test results in PR

## Documentation

### What to Document

- **Public APIs**: All public classes, methods, and properties
- **Configuration**: New config options
- **Commands**: New commands and usage
- **Features**: How to use new features
- **Changes**: Update CHANGELOG.md

### Documentation Style

```kotlin
/**
 * Manages the lifecycle of a tower defense game
 *
 * This class handles game state, player management, wave progression,
 * and cleanup operations for a single game instance.
 *
 * @property gameId Unique identifier for this game
 * @property config Game configuration including health, cash, and waves
 *
 * @constructor Creates a new game manager with the specified configuration
 */
class GameManager(
    val gameId: Int,
    val config: GameSaveConfig
) {
    /**
     * Starts the game with the given players
     *
     * Initializes player statistics, spawns game entities, and begins
     * the first wave. The game must not already be running.
     *
     * @param initialPlayers List of player UUIDs to join at game start
     * @throws IllegalStateException if game is already running
     */
    fun startGame(initialPlayers: List<UUID>) {
        // Implementation
    }
}
```

## Questions?

- **General Questions**: Open a [Discussion](https://github.com/ORIGINAL_OWNER/TowerDef-Kt/discussions)
- **Bug Reports**: Open an [Issue](https://github.com/ORIGINAL_OWNER/TowerDef-Kt/issues)
- **Feature Ideas**: Open a [Feature Request](https://github.com/ORIGINAL_OWNER/TowerDef-Kt/issues)

## Recognition

Contributors will be:
- Listed in the project contributors
- Mentioned in release notes for significant contributions
- Credited in the plugin credits (if applicable)

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (see LICENSE file).
