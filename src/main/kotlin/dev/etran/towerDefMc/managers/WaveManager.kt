package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.EnemySpawnCommand
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaitCommand
import dev.etran.towerDefMc.data.WaveData
import dev.etran.towerDefMc.factories.EnemyFactory
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class WaveManager(
    private val gameConfig: GameSaveConfig, private val waypointManager: WaypointManager, private val pathManager: PathManager, private val gameId: Int
) {
    private var currentWaveData: WaveData? = null
    private var commandIndex = -1
    var currentWave = 0
    private var enemiesRemaining = 0
    private var timeRemaining = 0.0


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
        enemiesRemaining = 0
        timeRemaining = 0.0
    }

    @Suppress("unused")
    fun checkWaveCompletion(): Boolean {
        return enemiesRemaining <= 0 || timeRemaining <= 0
    }

    fun startNextWave() {
        currentWave++
        val waveIndex = currentWave - 1

        if (waveIndex >= 0 && waveIndex < gameConfig.waves.size) {
            val waveDetails: WaveData = gameConfig.waves[waveIndex]

            currentWaveData = waveDetails

            timeRemaining = 0.0
            enemiesRemaining = waveDetails.sequence.sumOf {
                if (it is EnemySpawnCommand) it.enemies.values.sum() else 0
            }

            println("--- Starting Wave $currentWave: ${waveDetails.name} ---")
            println("Total Enemies to spawn: $enemiesRemaining")

            commandIndex = 0
            processNextCommand()
        } else {
            println("Game Over! All waves completed.")
        }
    }

    private fun processNextCommand() {
        if (commandIndex >= currentWaveData!!.sequence.size) {
            println("Wave completed!")
            // Check if this was the last wave
            if (currentWave >= gameConfig.waves.size) {
                println("All waves completed! Game won!")
                // Trigger game win
                val game = dev.etran.towerDefMc.registries.GameRegistry.activeGames[gameId]
                game?.endGame(true)
            }
            return // Wave is finished
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
            // Setup waypoint manager with this path's checkpoints
            pathManager.setupWaypointManagerForPath(randomPath, waypointManager)
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

                // Get a random path and use its start point
                if (randomPath == null) {
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
                    val startpointLoc: Location = randomPath.startPoint
                    
                    // Spawn the enemy and register it to this game
                    val entity = EnemyFactory.enemyPlace(currentEnemyType!!, startpointLoc)
                    if (entity != null) {
                        GameInstanceTracker.registerEntity(entity, gameId)
                    }
                }

                enemiesRemaining--
                currentQuantity--

                println("Spawned: $currentEnemyType for Game $gameId. Global remaining: $enemiesRemaining")
            }
        }.runTaskTimer(plugin, 0L, intervalTicks)
    }
}