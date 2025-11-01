package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaveData
import dev.etran.towerDefMc.registries.GameRegistry
import java.util.UUID

class GameManager(
    val gameId: Int,
    val config: GameSaveConfig,
) {
    // -- External Managers (now game-specific, not global) --
    val waypointManager = WaypointManager()
    val pathManager = PathManager()
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
        if (isRunning) return

        // Validate that there are paths or start points configured
        val hasPaths = pathManager.getAllPaths().any { it.isVisible }
        val hasStartPoints = waypointManager.startpoints.values.isNotEmpty()

        if (!hasPaths && !hasStartPoints) {
            // Send message to all players trying to start the game
            initialPlayers.forEach { uuid ->
                plugin.server.getPlayer(uuid)?.sendMessage("§c§lCannot start game: No paths or start points configured!")
            }
            plugin.logger.warning("Game $gameId: Cannot start - no paths or start points configured!")
            return
        }

        // Reset game state for a fresh start
        health = config.maxHealth
        isRunning = true

        players.clear()
        players.addAll(initialPlayers)

        // Add this game to activeGames so enemies can find it
        GameRegistry.activeGames[gameId] = this

        plugin.logger.info("Game $gameId started: ${config.name}. Max Health: $health")

        // Spawn game stats display armor stands at all lecterns
        dev.etran.towerDefMc.factories.GameStatsDisplayFactory.spawnAllGameStatsDisplays(gameId)

        // Reset wave manager state
        waveManager.resetWaves()

        // Start the first wave sequence
        waveManager.startNextWave()
    }

    fun endGame(win: Boolean) {
        if (!isRunning) return
        isRunning = false

        plugin.logger.info("Game $gameId ended. Result: ${if (win) "Win" else "Loss"}")

        // Stop all wave activities (spawning, wave progression, etc.)
        waveManager.stopAllWaveActivities()

        // Remove game stats display armor stands
        dev.etran.towerDefMc.factories.GameStatsDisplayFactory.removeAllGameStatsDisplays(gameId)

        // Clean up all entities for this game
        GameInstanceTracker.clearGame(gameId)

        // Don't delete the game file, just stop the instance
        GameRegistry.activeGames.remove(gameId)
    }

    /**
     * Stop the game manually (cancellation)
     */
    fun stopGame() {
        if (!isRunning) return
        isRunning = false

        plugin.logger.info("Game $gameId stopped manually")

        // Stop all wave activities (spawning, wave progression, etc.)
        waveManager.stopAllWaveActivities()

        // Remove game stats display armor stands
        dev.etran.towerDefMc.factories.GameStatsDisplayFactory.removeAllGameStatsDisplays(gameId)

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

        // Initialize stats for new player
        if (isRunning) {
            PlayerStatsManager.initializePlayer(
                gameId,
                player,
                config.defaultCash
            )
        }
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
     * Save the game including paths
     */
    fun saveGame() {
        // Serialize paths before saving
        config.paths = pathManager.serializePaths()
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
        // Load paths from config
        if (config.paths.isNotEmpty()) {
            pathManager.loadPaths(config.paths)
        }
    }
}
