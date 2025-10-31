package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaveData
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID

class GameManager(
    val gameId: Int,
    val config: GameSaveConfig,
) {
    // -- External Managers (now game-specific, not global) --
    val waypointManager = WaypointManager()
    val waveManager = WaveManager(config, waypointManager, gameId)

    // -- Game State Properties --
    private var health: Int = config.maxHealth

    @Suppress("unused")
    private var cash: Int = config.defaultCash
    private var isRunning: Boolean = false
    private var players: MutableSet<UUID> = mutableSetOf()

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
        isRunning = true

        players.addAll(initialPlayers)

        plugin.logger.info("Game $gameId started: ${config.name}. Max Health: $health")

        // Start the first wave sequence
        waveManager.startNextWave()
    }

    fun endGame(win: Boolean) {
        if (!isRunning) return
        isRunning = false

        plugin.logger.info("Game $gameId ended. Result: ${if (win) "Win" else "Loss"}")

        // Clean up all entities for this game
        GameInstanceTracker.clearGame(gameId)

        GameRegistry.removeGame(this)
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
    }

    @Suppress("unused")
    fun removePlayer(player: UUID) {
        players.remove(player)
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
}
