package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaveData
import dev.etran.towerDefMc.factories.CheckpointFactory
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Location
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
    fun setStartPoint(location: Location) {
        // Remove old start point if exists
        startpointManager.startpoints.values.firstOrNull()?.remove()
        startpointManager.startpoints.clear()

        // Create new armor stand at location
        val world = location.world ?: return
        val armorStand = world.spawn(location, org.bukkit.entity.ArmorStand::class.java) { stand ->
            stand.isVisible = true
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.customName(net.kyori.adventure.text.Component.text("§a§lSTART POINT"))
            stand.isCustomNameVisible = true
            stand.persistentDataContainer.set(
                TowerDefMC.GAME_ID_KEY,
                org.bukkit.persistence.PersistentDataType.INTEGER,
                gameId
            )
        }

        startpointManager.add(armorStand)
        saveToFile()
    }

    /**
     * Set the end point location
     */
    fun setEndPoint(location: Location) {
        // Store as special checkpoint with ID 999999 (end point marker)
        checkpointManager.checkpoints[999999]?.remove()

        val world = location.world ?: return
        val armorStand = world.spawn(location, org.bukkit.entity.ArmorStand::class.java) { stand ->
            stand.isVisible = true
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.customName(net.kyori.adventure.text.Component.text("§c§lEND POINT"))
            stand.isCustomNameVisible = true
            stand.persistentDataContainer.set(
                TowerDefMC.GAME_ID_KEY,
                org.bukkit.persistence.PersistentDataType.INTEGER,
                gameId
            )
            stand.persistentDataContainer.set(
                TowerDefMC.CHECKPOINT_ID,
                org.bukkit.persistence.PersistentDataType.INTEGER,
                999999
            )
        }

        checkpointManager.checkpoints[999999] = armorStand
        saveToFile()
    }

    /**
     * Add a checkpoint location
     */
    fun addCheckpoint(location: Location) {
        val world = location.world ?: return
        val nextId = (checkpointManager.checkpoints.keys.filter { it < 999999 }.maxOrNull() ?: 0) + 1

        val armorStand = world.spawn(location, org.bukkit.entity.ArmorStand::class.java) { stand ->
            stand.isVisible = true
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.customName(net.kyori.adventure.text.Component.text("§e§lCHECKPOINT #$nextId"))
            stand.isCustomNameVisible = true
            stand.persistentDataContainer.set(
                TowerDefMC.GAME_ID_KEY,
                org.bukkit.persistence.PersistentDataType.INTEGER,
                gameId
            )
            stand.persistentDataContainer.set(
                TowerDefMC.CHECKPOINT_ID,
                org.bukkit.persistence.PersistentDataType.INTEGER,
                nextId
            )
        }

        checkpointManager.checkpoints[nextId] = armorStand
        saveToFile()
    }

    // -- General Game Management --
    @Suppress("unused")
    fun addCheckpoint(event: PlayerInteractEvent) {
        CheckpointFactory.checkPointPlace(event, checkpointManager)
    }
}

