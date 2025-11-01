package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameManager
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Factory for creating game stats display armor stands
 */
object GameStatsDisplayFactory {

    /**
     * Creates a game stats display item
     */
    fun createGameStatsItem(amount: Int = 1): ItemStack {
        val item = ItemStack(Material.LECTERN, amount)
        val meta = item.itemMeta ?: return item

        meta.displayName(Component.text("§b§lGame Stats Display"))
        meta.lore(
            listOf(
                Component.text("§7Place this to view game statistics"),
                Component.text("§7Shows: Health, Wave, Players, etc."),
                Component.text("§e"),
                Component.text("§eRight-click to place")
            )
        )

        meta.persistentDataContainer.set(
            TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Game_Stats_Display"
        )

        item.itemMeta = meta
        return item
    }

    /**
     * Spawns game stats display armor stands at all registered lectern locations for a specific game
     */
    fun spawnAllGameStatsDisplays(gameId: Int) {
        val game = GameRegistry.allGames[gameId] ?: return

        // Find all lectern blocks marked as game stats displays
        Bukkit.getWorlds().forEach { world ->
            world.loadedChunks.forEach { chunk ->
                val chunkData = chunk.persistentDataContainer
                val key = TowerDefMC.createKey("game_stats_lecterns")
                val locationsData = chunkData.get(key, PersistentDataType.STRING)

                if (!locationsData.isNullOrEmpty()) {
                    // Parse locations and spawn armor stands
                    locationsData.split(";").forEach { locationString ->
                        val coords = locationString.split(",")
                        if (coords.size == 3) {
                            try {
                                val x = coords[0].toInt()
                                val y = coords[1].toInt()
                                val z = coords[2].toInt()
                                val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

                                // Check if the lectern still exists at this location
                                if (location.block.type == Material.LECTERN) {
                                    // Spawn armor stand above the lectern
                                    val armorStandLocation = location.clone().add(0.5, 1.0, 0.5)
                                    placeGameStatsDisplay(armorStandLocation, gameId)
                                }
                            } catch (e: NumberFormatException) {
                                // Skip invalid location data
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes all game stats display armor stands for a specific game
     */
    fun removeAllGameStatsDisplays(gameId: Int) {
        Bukkit.getWorlds().forEach { world ->
            world.entities.filterIsInstance<ArmorStand>().forEach { stand ->
                val elementType = stand.persistentDataContainer.get(
                    TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
                )
                val standGameId = stand.persistentDataContainer.get(
                    TowerDefMC.GAME_ID_KEY, PersistentDataType.INTEGER
                )

                // Remove armor stands that belong to this game
                if (elementType == "GameStatsDisplay" && standGameId == gameId) {
                    // Cancel the update task
                    val taskId = stand.persistentDataContainer.get(
                        TowerDefMC.createKey("updateTaskId"), PersistentDataType.INTEGER
                    )
                    if (taskId != null) {
                        Bukkit.getScheduler().cancelTask(taskId)
                    }
                    stand.remove()
                }
            }
        }
    }

    /**
     * Places a game stats display armor stand at a location (without game context)
     * This is used when placing the lectern outside of a game
     */
    fun placeGameStatsDisplayStand(location: org.bukkit.Location): ArmorStand {
        val world = location.world
        val armorStand = world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand

        armorStand.isVisible = false
        armorStand.setGravity(false)
        armorStand.isInvulnerable = true
        armorStand.isCustomNameVisible = true
        armorStand.persistentDataContainer.set(
            TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "GameStatsDisplay"
        )

        // Start updating the stats display (will show "No active game" until a game starts)
        startGameStatsUpdateTaskNoGame(armorStand)

        return armorStand
    }

    /**
     * Places a game stats display armor stand at a location
     */
    fun placeGameStatsDisplay(location: Location, gameId: Int): List<ArmorStand> {
        val game = GameRegistry.allGames[gameId] ?: return emptyList()

        val world = location.world
        val armorStands = mutableListOf<ArmorStand>()

        // Get the display lines
        val displayLines = buildGameStatsDisplayLines(game)

        // Calculate starting height: place the BOTTOM line at the lectern's top level
        // Then stack upward. Each line is 0.3 blocks apart for better readability
        val totalHeight = displayLines.size * 0.3

        // Spawn an armor stand for each line, stacked vertically from bottom to top
        displayLines.forEachIndexed { index, line ->
            // Stack armor stands upward, starting from the base
            // Reverse index so the first line is at the top
            val lineFromBottom = displayLines.size - 1 - index
            val yOffset = lineFromBottom * 0.3
            val standLocation = location.clone().add(0.0, yOffset, 0.0)

            val armorStand = world.spawnEntity(standLocation, EntityType.ARMOR_STAND) as ArmorStand

            armorStand.isVisible = false
            armorStand.setGravity(false)
            armorStand.isInvulnerable = true
            armorStand.isCustomNameVisible = true
            armorStand.isMarker = true  // Makes it smaller and prevents collision
            armorStand.customName(Component.text(line))
            armorStand.persistentDataContainer.set(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "GameStatsDisplay"
            )
            armorStand.persistentDataContainer.set(
                TowerDefMC.GAME_ID_KEY, PersistentDataType.INTEGER, gameId
            )

            armorStands.add(armorStand)
        }

        // Start updating the stats display
        startGameStatsUpdateTask(armorStands, gameId)

        return armorStands
    }

    /**
     * Starts a task to continuously update the game stats display
     */
    private fun startGameStatsUpdateTask(armorStands: List<ArmorStand>, gameId: Int) {
        val taskId = Bukkit.getScheduler().runTaskTimer(TowerDefMC.instance, Runnable {
            if (armorStands.any { !it.isValid || it.isDead }) {
                return@Runnable
            }

            val game = GameRegistry.allGames[gameId]
            if (game != null) {
                val displayLines = buildGameStatsDisplayLines(game)
                armorStands.forEachIndexed { index, stand ->
                    if (index < displayLines.size) {
                        stand.customName(Component.text(displayLines[index]))
                    }
                }
            }
        }, 0L, 20L) // Update every second

        // Store task ID in the first armor stand to cancel later if needed
        if (armorStands.isNotEmpty()) {
            armorStands[0].persistentDataContainer.set(
                TowerDefMC.createKey("updateTaskId"), PersistentDataType.INTEGER, taskId.taskId
            )
        }
    }

    /**
     * Starts a task to continuously update the game stats display without game context
     */
    private fun startGameStatsUpdateTaskNoGame(armorStand: ArmorStand) {
        val taskId = Bukkit.getScheduler().runTaskTimer(TowerDefMC.instance, Runnable {
            if (!armorStand.isValid || armorStand.isDead) {
                return@Runnable
            }

            // Default message when no game is active
            armorStand.customName(Component.text("§cNo active game"))
        }, 0L, 20L) // Update every second

        // Store task ID to cancel later if needed
        armorStand.persistentDataContainer.set(
            TowerDefMC.createKey("updateTaskId"), PersistentDataType.INTEGER, taskId.taskId
        )
    }

    /**
     * Builds the display text lines for game stats (each line separate)
     */
    private fun buildGameStatsDisplayLines(game: GameManager): List<String> {
        val allPlayerStats = PlayerStatsManager.getAllPlayerStats(game.gameId)
        val playerCount = allPlayerStats.size

        // Calculate total stats
        val totalKills = allPlayerStats.values.sumOf { it.kills }
        val totalTowers = allPlayerStats.values.sumOf { it.towersPlaced }
        val totalDamage = allPlayerStats.values.sumOf { it.damageDealt }

        // Get active path count
        val activePaths = game.pathManager.getAllPaths().count { it.isVisible }

        return listOf(
            "§b§l╔════════════════════╗",
            "§b§l║  §6§lGAME STATISTICS  §b§l║",
            "§b§l╠════════════════════╣",
            "§b§l║ §e${game.config.name}",
            "§b§l║ §7Wave: §f${game.waveManager.currentWave}§7/§f${game.config.waves.size}",
            "§b§l║ §7Health: §c${game.currentHealth}§7/§f${game.config.maxHealth}",
            "§b§l║ §7Players: §a$playerCount",
            "§b§l║ §7Active Paths: §e$activePaths",
            "§b§l╠════════════════════╣",
            "§b§l║ §6§lTEAM STATS",
            "§b§l║ §7Total Kills: §f$totalKills",
            "§b§l║ §7Total Towers: §f$totalTowers",
            "§b§l║ §7Total Damage: §f${"%.0f".format(totalDamage)}",
            "§b§l╚════════════════════╝"
        )
    }

    /**
     * Builds the display text for game stats (legacy single-line version)
     */
    private fun buildGameStatsDisplay(game: GameManager): String {
        val allPlayerStats = PlayerStatsManager.getAllPlayerStats(game.gameId)
        val playerCount = allPlayerStats.size

        // Calculate total stats
        val totalKills = allPlayerStats.values.sumOf { it.kills }
        val totalTowers = allPlayerStats.values.sumOf { it.towersPlaced }
        val totalDamage = allPlayerStats.values.sumOf { it.damageDealt }

        // Get active path count
        val activePaths = game.pathManager.getAllPaths().count { it.isVisible }

        return buildString {
            append("§b§l╔════════════════════╗\n")
            append("§b§l║  §6§lGAME STATISTICS  §b§l║\n")
            append("§b§l╠════════════════════╣\n")
            append("§b§l║ §e${game.config.name}\n")
            append("§b§l║ §7Wave: §f${game.waveManager.currentWave}§7/§f${game.config.waves.size}\n")
            append("§b§l║ §7Health: §c${game.currentHealth}§7/§f${game.config.maxHealth}\n")
            append("§b§l║ §7Players: §a$playerCount\n")
            append("§b§l║ §7Active Paths: §e$activePaths\n")
            append("§b§l╠════════════════════╣\n")
            append("§b§l║ §6§lTEAM STATS\n")
            append("§b§l║ §7Total Kills: §f$totalKills\n")
            append("§b§l║ §7Total Towers: §f$totalTowers\n")
            append("§b§l║ §7Total Damage: §f${"%.0f".format(totalDamage)}\n")
            append("§b§l╚════════════════════╝")
        }
    }

    /**
     * Updates all game stats displays for a specific game
     */
    fun updateAllGameStatsDisplays(gameId: Int) {
        Bukkit.getWorlds().forEach { world ->
            world.entities.filterIsInstance<ArmorStand>().forEach { stand ->
                val elementType = stand.persistentDataContainer.get(
                    TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
                )
                val standGameId = stand.persistentDataContainer.get(
                    TowerDefMC.GAME_ID_KEY, PersistentDataType.INTEGER
                )

                if (elementType == "GameStatsDisplay" && standGameId == gameId) {
                    val game = GameRegistry.allGames[gameId]
                    if (game != null) {
                        val displayText = buildGameStatsDisplay(game)
                        stand.customName(Component.text(displayText))
                    }
                }
            }
        }
    }
}
