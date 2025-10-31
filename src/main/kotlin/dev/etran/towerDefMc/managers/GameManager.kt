package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.factories.CheckpointFactory
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID

class GameManager(
    val gameId: Int,
    val config: GameSaveConfig,
) {
    // -- External Managers (now game-specific, not global) --
    val startpointManager = StartpointManager()
    val waveManager = WaveManager(config, startpointManager, gameId)
    val checkpointManager = CheckpointManager()

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
        dev.etran.towerDefMc.managers.GameInstanceTracker.clearGame(gameId)

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

    // -- General Game Management --
    @Suppress("unused")
    fun addCheckpoint(event: PlayerInteractEvent) {
        CheckpointFactory.checkPointPlace(event, checkpointManager)
    }
}