package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.factories.CheckpointFactory
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID

class GameManager(
    val gameId: Int,
    val config: GameSaveConfig,
) {
    // -- External Managers --
    private val startpointManager = StartpointManager()
    private val waveManager = WaveManager(config, startpointManager)
    private val checkpointManager = CheckpointManager()

    // -- Game State Properties --
    private var health: Int = config.maxHealth
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
    val currentWave: Int
        get() = waveManager.currentWave

    fun startGame(world: World, initialPlayers: List<UUID>) {
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

        GameRegistry.removeGame(this)
    }

    fun onHealthLost(amount: Int) {
        health -= amount

        if (health <= 0) {
            endGame(false)

        } else {
            println("Game $gameId health remaining: $health")
        }
    }

    // -- Player Management --
    fun addPlayer(player: UUID) {
        players.add(player)
    }

    fun removePlayer(player: UUID) {
        players.remove(player)
    }

    // -- General Game Management --
    fun addCheckpoint(event: PlayerInteractEvent) {
        CheckpointFactory.checkPointPlace(event, checkpointManager)
    }
}