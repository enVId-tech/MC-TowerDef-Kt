package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaveData
import dev.etran.towerDefMc.factories.GameStatsDisplayFactory
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.DebugLogger
import java.util.UUID

class GameManager(
    val gameId: Int,
    val config: GameSaveConfig,
) {
    // -- External Managers (now game-specific, not global) --
    val waypointManager = WaypointManager()
    val pathManager = PathManager()
    val spawnableSurfaceManager = SpawnableSurfaceManager()
    val waveManager = WaveManager(config, waypointManager, pathManager, gameId)

    // -- Game State Properties --
    private var health: Int = config.maxHealth

    @Suppress("unused")
    private var cash: Int = config.defaultCash
    private var isRunning: Boolean = false
    private var players: MutableSet<UUID> = mutableSetOf()

    // Public getters for game state
    val currentHealth: Int
        get() = health

    val isGameRunning: Boolean
        get() = isRunning

    val playerCount: Int
        get() = players.size

    val activePlayers: Set<UUID>
        get() = players.toSet()

    companion object {
        lateinit var plugin: TowerDefMC

        fun initialize(plugin: TowerDefMC) {
            this.plugin = plugin
        }
    }

    // -- Wave Management --
    @Suppress("unused")
    val currentWave: Int
        get() = waveManager.currentWave

    @Suppress("unused")
    fun startGame(initialPlayers: List<UUID>) {
        if (isRunning) {
            DebugLogger.logGame("Game $gameId: Cannot start - already running")
            return
        }

        DebugLogger.logGame("Game $gameId: Attempting to start game '${config.name}'")

        // Validate that there are paths or start points configured
        // Check for ANY paths (visible or not) or start points
        val hasPaths = pathManager.getAllPaths().isNotEmpty()
        val hasStartPoints = waypointManager.startpoints.values.isNotEmpty()

        if (!hasPaths && !hasStartPoints) {
            DebugLogger.logGame("Game $gameId: Cannot start - no paths or start points configured!")
            // Send message to all players trying to start the game
            initialPlayers.forEach { uuid ->
                plugin.server.getPlayer(uuid)
                    ?.sendMessage("§c§lCannot start game: No paths or start points configured!")
            }
            plugin.logger.warning("Game $gameId: Cannot start - no paths or start points configured!")
            return
        }

        // Reset game state for a fresh start
        health = config.maxHealth
        isRunning = true

        players.clear()
        players.addAll(initialPlayers)

        DebugLogger.logGame("Game $gameId: Initializing ${initialPlayers.size} players with starting cash ${config.defaultCash}")

        // Initialize player stats with starting cash for all players
        initialPlayers.forEach { playerUUID ->
            PlayerStatsManager.initializePlayer(gameId, playerUUID, config.defaultCash)
            plugin.server.getPlayer(playerUUID)?.sendMessage("§a§lStarting cash: §e${config.defaultCash}")
        }

        // Add this game to activeGames so enemies can find it
        GameRegistry.activeGames[gameId] = this

        plugin.logger.info("Game $gameId started: ${config.name}. Max Health: $health")
        DebugLogger.logGame("Game $gameId: Successfully started with ${config.waves.size} waves")

        // Spawn game stats display armor stands at all lecterns
        GameStatsDisplayFactory.spawnAllGameStatsDisplays(gameId)

        // Reset wave manager state
        waveManager.resetWaves()

        // Start the first wave sequence
        waveManager.startNextWave()
    }

    fun endGame(win: Boolean) {
        if (!isRunning) {
            DebugLogger.logGame("Game $gameId: Cannot end - not running")
            return
        }
        isRunning = false

        plugin.logger.info("Game $gameId ended. Result: ${if (win) "Win" else "Loss"}")
        DebugLogger.logGame("Game $gameId: Ending game - Result: ${if (win) "WIN" else "LOSS"}, Wave: ${waveManager.currentWave}, Health: $health")

        // Stop tower placement previews for all players in this game
        players.forEach { playerId ->
            dev.etran.towerDefMc.listeners.PlayerHoldListener.stopPlayerTask(playerId)
        }

        // Clear game items from all players' inventories
        clearPlayerInventories()

        // Display ending sequence before cleanup
        if (win) {
            dev.etran.towerDefMc.utils.GameEndingSequence.displayVictorySequence(
                gameId, config.name, players.toSet(), waveManager.currentWave
            )
        } else {
            dev.etran.towerDefMc.utils.GameEndingSequence.displayDefeatSequence(
                gameId, config.name, players.toSet(), waveManager.currentWave, health
            )
        }

        // Delay cleanup to allow players to see the ending sequence
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            DebugLogger.logGame("Game $gameId: Starting cleanup")
            // Stop all wave activities (spawning, wave progression, etc.)
            waveManager.stopAllWaveActivities()

            // Remove game stats display armor stands
            GameStatsDisplayFactory.removeAllGameStatsDisplays(gameId)

            // Clean up all entities for this game
            GameInstanceTracker.clearGame(gameId)

            // Clear player stats after displaying them
            PlayerStatsManager.clearGameStats(gameId)

            // Don't delete the game file, just stop the instance
            GameRegistry.activeGames.remove(gameId)
            DebugLogger.logGame("Game $gameId: Cleanup complete")
        }, 200L) // 10 second delay to allow players to read stats
    }

    /**
     * Stop the game manually (cancellation)
     */
    fun stopGame() {
        if (!isRunning) {
            DebugLogger.logGame("Game $gameId: Cannot stop - not running")
            return
        }
        isRunning = false

        plugin.logger.info("Game $gameId stopped manually")
        DebugLogger.logGame("Game $gameId: Manually stopped by admin")

        // Stop tower placement previews for all players in this game
        players.forEach { playerId ->
            dev.etran.towerDefMc.listeners.PlayerHoldListener.stopPlayerTask(playerId)
        }

        // Clear game items from all players' inventories
        clearPlayerInventories()

        // Stop all wave activities (spawning, wave progression, etc.)
        waveManager.stopAllWaveActivities()

        // Remove game stats display armor stands
        GameStatsDisplayFactory.removeAllGameStatsDisplays(gameId)

        // Clean up all entities for this game
        GameInstanceTracker.clearGame(gameId)

        // Don't remove from registry, just stop it so it can be restarted
        GameRegistry.activeGames.remove(gameId)
    }

    fun onHealthLost(amount: Int) {
        health -= amount

        if (health <= 0) {
            endGame(false)
        } else {
            plugin.logger.info("Game $gameId health remaining: $health")
        }
    }

    // -- Player Management --
    @Suppress("unused")
    fun addPlayer(player: UUID) {
        players.add(player)

        // Initialize stats for new player (always initialize, not just when running)
        PlayerStatsManager.initializePlayer(
            gameId, player, config.defaultCash
        )
    }

    @Suppress("unused")
    fun removePlayer(player: UUID) {
        players.remove(player)

        // Remove player stats
        PlayerStatsManager.removePlayer(gameId, player)
    }

    fun hasPlayer(player: UUID): Boolean {
        return players.contains(player)
    }

    // -- Configuration Management & Saving --
    /**
     * Update the max health configuration and save to file
     */
    fun updateMaxHealth(newMaxHealth: Int) {
        config.maxHealth = newMaxHealth
        saveToFile()
    }

    /**
     * Update the default cash configuration and save to file
     */
    fun updateDefaultCash(newDefaultCash: Int) {
        config.defaultCash = newDefaultCash
        saveToFile()
    }

    /**
     * Update the game name and save to file
     */
    fun updateGameName(newName: String) {
        config.name = newName
        saveToFile()
    }

    /**
     * Update the tower sell refund percentage and save to file
     */
    fun updateTowerSellRefundPercentage(newPercentage: Int) {
        config.towerSellRefundPercentage = newPercentage
        saveToFile()
    }

    /**
     * Update the waves list and save to file
     */
    fun updateWaves(newWaves: List<WaveData>) {
        config.waves = newWaves
        saveToFile()
    }

    /**
     * Update the allowed towers list and save to file
     */
    fun updateAllowedTowers(newTowers: List<String>) {
        config.allowedTowers = newTowers
        saveToFile()
    }

    /**
     * Save this game's configuration to file
     */
    fun saveToFile() {
        GameRegistry.saveGame(this)
    }

    /**
     * Save the game including paths and spawnable surfaces
     */
    fun saveGame() {
        // Serialize paths and spawnable surfaces before saving
        config.paths = pathManager.serializePaths()
        config.spawnableSurfaces = spawnableSurfaceManager.serializeSurfaces()
        saveToFile()
    }

    // -- Spawn Point Management --
    /**
     * Set the start point location
     */
    fun setStartPoint(location: org.bukkit.Location) {
        waypointManager.setStartPoint(location)
        saveToFile()
    }

    /**
     * Set the end point location
     */
    fun setEndPoint(location: org.bukkit.Location) {
        waypointManager.setEndPoint(location)
        saveToFile()
    }

    /**
     * Add a checkpoint location
     */
    fun addCheckpoint(location: org.bukkit.Location) {
        waypointManager.addCheckpoint(location)
        saveToFile()
    }

    // -- General Game Management --

    init {
        // Clean up any orphaned path armor stands from previous sessions BEFORE loading paths
        cleanupOrphanedPathStands()

        // Load paths from config
        if (config.paths.isNotEmpty()) {
            pathManager.loadPaths(config.paths)
        }
        // Load spawnable surfaces from config
        if (config.spawnableSurfaces.isNotEmpty()) {
            spawnableSurfaceManager.loadSurfaces(config.spawnableSurfaces)
        }
    }

    /**
     * Clean up orphaned path armor stands from the world
     * This ensures no duplicate armor stands remain from previous server sessions
     */
    private fun cleanupOrphanedPathStands() {
        var removedCount = 0
        org.bukkit.Bukkit.getWorlds().forEach { world ->
            world.entities.filterIsInstance<org.bukkit.entity.ArmorStand>().forEach { stand ->
                val elementType = stand.persistentDataContainer.get(
                    TowerDefMC.ELEMENT_TYPES,
                    org.bukkit.persistence.PersistentDataType.STRING
                )
                // Remove any armor stands that are path-related
                if (elementType in listOf("PathStart", "PathCheckpoint", "PathEnd")) {
                    stand.remove()
                    removedCount++
                }
            }
        }

        if (removedCount > 0) {
            DebugLogger.logGame("Game $gameId: Cleaned up $removedCount orphaned path armor stands")
        }
    }

    /**
     * Clear all game-related items from players' inventories
     */
    private fun clearPlayerInventories() {
        players.forEach { playerId ->
            val player = plugin.server.getPlayer(playerId) ?: return@forEach

            // Remove all tower items
            player.inventory.contents.forEachIndexed { index, item ->
                if (item != null) {
                    val meta = item.itemMeta
                    if (meta != null) {
                        // Check if it's a tower item
                        val isTowerItem = meta.persistentDataContainer.has(
                            TowerDefMC.TOWER_RANGE,
                            org.bukkit.persistence.PersistentDataType.DOUBLE
                        )

                        // Check if it's any game item
                        val isGameItem = meta.persistentDataContainer.has(
                            TowerDefMC.GAME_ITEMS,
                            org.bukkit.persistence.PersistentDataType.STRING
                        )

                        if (isTowerItem || isGameItem) {
                            player.inventory.setItem(index, null)
                        }
                    }
                }
            }

            player.sendMessage("§7Game items cleared from inventory")
            DebugLogger.logGame("Cleared game items from player ${player.name}'s inventory")
        }
    }
}
