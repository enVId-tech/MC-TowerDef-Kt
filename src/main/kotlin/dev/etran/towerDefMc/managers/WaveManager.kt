package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.EnemySpawnCommand
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaitCommand
import dev.etran.towerDefMc.data.WaveData
import dev.etran.towerDefMc.factories.EnemyFactory
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class WaveManager(
    private val gameConfig: GameSaveConfig, private val waypointManager: WaypointManager, private val pathManager: PathManager, private val gameId: Int
) {
    private var currentWaveData: WaveData? = null
    private var commandIndex = -1
    var currentWave = 0
    private var spawnedEnemies = 0 // Track how many we spawned
    private var timeRemaining = 0.0
    private var waveCheckTaskId: Int? = null
    private var isSpawningComplete = false


    companion object {
        private lateinit var plugin: TowerDefMC

        fun initialize(plugin: TowerDefMC) {
            this.plugin = plugin
        }
    }

    /**
     * Reset wave manager state for a fresh game start
     */
    fun resetWaves() {
        currentWave = 0
        commandIndex = -1
        currentWaveData = null
        spawnedEnemies = 0
        timeRemaining = 0.0
        isSpawningComplete = false
        cancelWaveCheckTask()
    }

    private fun cancelWaveCheckTask() {
        waveCheckTaskId?.let {
            plugin.server.scheduler.cancelTask(it)
            waveCheckTaskId = null
        }
    }

    fun checkWaveCompletion(): Boolean {
        // Wave is complete when spawning is done AND no enemies remain alive
        if (!isSpawningComplete) return false

        val aliveEnemies = GameInstanceTracker.getLivingEntitiesInGame(gameId).size
        return aliveEnemies <= 0
    }

    fun startNextWave() {
        currentWave++
        val waveIndex = currentWave - 1

        if (waveIndex >= 0 && waveIndex < gameConfig.waves.size) {
            val waveDetails: WaveData = gameConfig.waves[waveIndex]

            currentWaveData = waveDetails

            timeRemaining = 0.0
            spawnedEnemies = 0
            isSpawningComplete = false

            println("--- Starting Wave $currentWave: ${waveDetails.name} ---")

            commandIndex = 0
            processNextCommand()

            // Start checking for wave completion
            startWaveCompletionCheck()
        } else {
            println("Game Over! All waves completed.")
        }
    }

    private fun startWaveCompletionCheck() {
        cancelWaveCheckTask()

        // Check every second (20 ticks) if the wave is complete
        waveCheckTaskId = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            if (checkWaveCompletion()) {
                println("Wave $currentWave completed!")
                cancelWaveCheckTask()

                // Check if this was the last wave
                if (currentWave >= gameConfig.waves.size) {
                    println("All waves completed! Game won!")
                    val game = GameRegistry.activeGames[gameId]
                    game?.endGame(true)
                } else {
                    // Move to next wave after a short delay
                    plugin.server.scheduler.runTaskLater(plugin, Runnable {
                        startNextWave()
                    }, 60L) // 3 second delay between waves
                }
            }
        }, 20L, 20L) // Check every second
    }

    private fun processNextCommand() {
        if (commandIndex >= currentWaveData!!.sequence.size) {
            println("All spawning complete for wave $currentWave!")
            isSpawningComplete = true
            // Check if this was the last wave
            if (currentWave >= gameConfig.waves.size) {
                // Don't end yet - wait for enemies to be killed
                println("Final wave spawning complete, waiting for enemies to be eliminated...")
            }
            return // Spawning is finished, but wave continues until enemies are dead
        }

        val command = currentWaveData!!.sequence[commandIndex]

        when (command) {
            is WaitCommand -> handleWaitCommand(command)
            is EnemySpawnCommand -> handleEnemySpawnCommand(command)
        }
    }

    private fun handleWaitCommand(command: WaitCommand) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            commandIndex++
            processNextCommand()
        }, (command.waitSeconds * 20).toLong())
    }

    private fun handleEnemySpawnCommand(command: EnemySpawnCommand) {
        println("Command $commandIndex: Starting spawn sequence (Interval: ${command.intervalSeconds}s).")
        commandIndex++

        val intervalTicks = (command.intervalSeconds * 20).toLong()

        val spawnQueue = command.enemies.toMutableMap()

        // Get a random path once for this spawn command and set up waypoint manager
        val randomPath = pathManager.getRandomPath()
        if (randomPath != null) {
            // Setup waypoint manager with this path's checkpoints ONLY ONCE per game
            // Check if waypoints are already set up (don't duplicate)
            if (waypointManager.checkpoints.isEmpty()) {
                pathManager.setupWaypointManagerForPath(randomPath, waypointManager)
            }
        }

        object : BukkitRunnable() {
            private var currentEnemyType: String? = null
            private var currentQuantity = 0

            override fun run() {
                if (currentQuantity <= 0) {
                    currentEnemyType = spawnQueue.keys.firstOrNull()
                    if (currentEnemyType != null) {
                        currentQuantity = spawnQueue.remove(currentEnemyType)!!
                      }
                }

                // If no more enemies in this command's queue, cancel the task
                if (currentEnemyType == null) {
                    this.cancel()
                    println("Spawn sequence finished for command.")
                    processNextCommand()
                    return
                }

                // Get a random path and use its start point (or pick new one each time)
                val spawnPath = randomPath ?: pathManager.getRandomPath()

                if (spawnPath == null) {
                    // Fallback to old waypoint system if no paths exist
                    if (waypointManager.startpoints.values.isEmpty()) {
                        plugin.logger.warning("Game $gameId: No paths or start points configured! Cannot spawn enemies.")
                        this.cancel()
                        return
                    }
                    val startpointLoc: Location = waypointManager.startpoints.values.random().location
                    
                    // Spawn the enemy and register it to this game
                    val entity = EnemyFactory.enemyPlace(currentEnemyType!!, startpointLoc)
                    if (entity != null) {
                        GameInstanceTracker.registerEntity(entity, gameId)
                    }
                } else {
                    // Use the path's start point
                    val startpointLoc: Location = spawnPath.startPoint

                    // Spawn the enemy and register it to this game
                    val entity = EnemyFactory.enemyPlace(currentEnemyType!!, startpointLoc)
                    if (entity != null) {
                        GameInstanceTracker.registerEntity(entity, gameId)
                    }
                }

                spawnedEnemies++
                currentQuantity--

                println("Spawned: $currentEnemyType for Game $gameId. Total spawned: $spawnedEnemies")
            }
        }.runTaskTimer(plugin, 0L, intervalTicks)
    }
}