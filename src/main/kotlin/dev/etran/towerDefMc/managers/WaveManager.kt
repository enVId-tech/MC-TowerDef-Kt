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
import org.bukkit.persistence.PersistentDataType

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
    private val activeSpawnTasks = mutableListOf<Int>() // Track active spawn task IDs
    private var waitTaskId: Int? = null // Track wait command task


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

        // Clear waypoint manager checkpoints to allow new game to set them up fresh
        waypointManager.checkpoints.clear()
    }

    /**
     * Stop all wave activities (spawning, wave progression, etc.)
     * Call this when the game ends
     */
    fun stopAllWaveActivities() {
        // Cancel wave completion check
        cancelWaveCheckTask()

        // Cancel all active spawn tasks
        activeSpawnTasks.forEach { taskId ->
            plugin.server.scheduler.cancelTask(taskId)
        }
        activeSpawnTasks.clear()

        // Cancel wait command task if active
        waitTaskId?.let {
            plugin.server.scheduler.cancelTask(it)
            waitTaskId = null
        }

        println("All wave activities stopped for Game $gameId")
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

        val aliveEnemies = GameInstanceTracker.getLivingEntitiesInGame(gameId)

        // Remove any enemies with null custom health (they're dead/removed but still tracked)
        val actuallyAlive = aliveEnemies.filter { enemy ->
            val customHealth = enemy.persistentDataContainer.get(
                TowerDefMC.createKey("custom_health"),
                PersistentDataType.DOUBLE
            )

            if (customHealth == null) {
                // This enemy is already dead/removed, unregister it
                println("Removing ghost enemy ${enemy.uniqueId} from tracker (customHealth is null)")
                GameInstanceTracker.unregisterEntity(enemy)
                false
            } else {
                true
            }
        }

        val aliveCount = actuallyAlive.size

        // Debug logging to help track wave completion issues
        if (aliveCount > 0) {
            println("Game $gameId - Wave $currentWave: $aliveCount enemies still alive")
            // Log the actual entities to help debug ghost enemies
            actuallyAlive.forEach { enemy ->
                val customHealth = enemy.persistentDataContainer.get(
                    TowerDefMC.createKey("custom_health"),
                    PersistentDataType.DOUBLE
                )
                println("  - Enemy ${enemy.uniqueId}: isDead=${enemy.isDead}, health=${enemy.health}, customHealth=$customHealth")
            }
        }

        return aliveCount <= 0
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

            // Get all players in this game
            val game = GameRegistry.activeGames[gameId]
            val playerUUIDs = game?.activePlayers ?: emptySet()

            // Announce wave start with on-screen display and chat details
            dev.etran.towerDefMc.utils.WaveAnnouncement.announceWaveStart(
                gameId,
                currentWave,
                waveDetails,
                gameConfig.waves.size,
                playerUUIDs
            )

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

                // Get all players in this game
                val game = GameRegistry.activeGames[gameId]
                val playerUUIDs = game?.activePlayers ?: emptySet()

                // Award cash to all players for completing the wave
                val cashReward = currentWaveData?.cashGiven ?: 0
                if (cashReward > 0) {
                    PlayerStatsManager.getAllPlayerStats(gameId).keys.forEach { playerUUID ->
                        PlayerStatsManager.awardCash(gameId, playerUUID, cashReward)
                    }
                    println("Awarded $cashReward cash to all players for completing wave $currentWave")
                }

                // Display wave complete announcement with title and rewards
                dev.etran.towerDefMc.utils.WaveAnnouncement.announceWaveComplete(
                    gameId,
                    currentWave,
                    cashReward,
                    playerUUIDs
                )

                // Record wave completion in stats
                PlayerStatsManager.recordWaveCompletion(gameId)

                cancelWaveCheckTask()

                // Check if this was the last wave
                if (currentWave >= gameConfig.waves.size) {
                    println("All waves completed! Game won!")
                    game?.endGame(true)
                } else {
                    // Show preparation message
                    plugin.server.scheduler.runTaskLater(plugin, Runnable {
                        dev.etran.towerDefMc.utils.WaveAnnouncement.announcePreparation(playerUUIDs, 3)
                    }, 40L) // After 2 seconds

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
        waitTaskId = plugin.server.scheduler.runTaskLater(plugin, Runnable {
            commandIndex++
            processNextCommand()
        }, (command.waitSeconds * 20).toLong()).taskId
    }

    private fun handleEnemySpawnCommand(command: EnemySpawnCommand) {
        println("Command $commandIndex: Starting spawn sequence (Interval: ${command.intervalSeconds}s).")
        commandIndex++

        val intervalTicks = (command.intervalSeconds * 20).toLong()

        val spawnQueue = command.enemies.toMutableMap()

        // Get a random path once for this spawn command and set up waypoint manager
        // Use getRandomPathForSpawning() to get paths regardless of visibility
        val randomPath = pathManager.getRandomPathForSpawning()
        if (randomPath != null) {
            // Clear all existing waypoints first to prevent duplication
            waypointManager.clearAllWaypoints()

            // Now setup waypoint manager with this path's checkpoints
            pathManager.setupWaypointManagerForPath(randomPath, waypointManager)
        }

        val spawnTask = object : BukkitRunnable() {
            private var currentEnemyType: String? = null
            private var currentQuantity = 0

            override fun run() {
                // Check if game is still running before spawning
                val game = GameRegistry.activeGames[gameId]
                if (game == null || !game.isGameRunning) {
                    this.cancel()
                    activeSpawnTasks.remove(this.taskId)
                    println("Spawn task cancelled - game is no longer running")
                    return
                }

                if (currentQuantity <= 0) {
                    currentEnemyType = spawnQueue.keys.firstOrNull()
                    if (currentEnemyType != null) {
                        currentQuantity = spawnQueue.remove(currentEnemyType)!!
                      }
                }

                // If no more enemies in this command's queue, cancel the task
                if (currentEnemyType == null) {
                    this.cancel()
                    activeSpawnTasks.remove(this.taskId)
                    println("Spawn sequence finished for command.")
                    processNextCommand()
                    return
                }

                // Get a random path and use its start point (or pick new one each time)
                // Use getRandomPathForSpawning() instead of getRandomPath()
                val spawnPath = randomPath ?: pathManager.getRandomPathForSpawning()

                if (spawnPath == null) {
                    // Fallback to old waypoint system if no paths exist
                    if (waypointManager.startpoints.values.isEmpty()) {
                        plugin.logger.warning("Game $gameId: No paths or start points configured! Cannot spawn enemies.")
                        this.cancel()
                        activeSpawnTasks.remove(this.taskId)
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
        }

        val taskId = spawnTask.runTaskTimer(plugin, 0L, intervalTicks).taskId
        activeSpawnTasks.add(taskId)
    }
}