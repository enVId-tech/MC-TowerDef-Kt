package dev.etran.towerDefMc

import dev.etran.towerDefMc.commands.ClearCheckpoints
import dev.etran.towerDefMc.commands.GiveCheckpoint
import dev.etran.towerDefMc.commands.GiveEndPoint
import dev.etran.towerDefMc.commands.GiveEnemy
import dev.etran.towerDefMc.commands.GiveStartPoint
import dev.etran.towerDefMc.commands.GiveTower
import dev.etran.towerDefMc.commands.ToggleStandVisibility
import dev.etran.towerDefMc.listeners.EnemyHealthListener
import dev.etran.towerDefMc.listeners.EntityDeathListener
import dev.etran.towerDefMc.listeners.FireproofListener
import dev.etran.towerDefMc.listeners.PlayerHoldListener
import dev.etran.towerDefMc.listeners.PlayerPlaceListener
import dev.etran.towerDefMc.schedulers.EnemyScheduler
import dev.etran.towerDefMc.schedulers.TowerScheduler
import dev.etran.towerDefMc.utils.TaskUtility
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.java.JavaPlugin

class TowerDefMC : JavaPlugin() {
    val CHECK_INTERVAL_TICKS: Long = 4L

    companion object {
        // Set by onEnable, clear in onDisable
        lateinit var instance: TowerDefMC
            private set

        // Access the key via the current instance
        // Major game objects
        val ELEMENT_TYPES: NamespacedKey
            get() = NamespacedKey(instance, "elementTypes")
        val TOWER_TYPES: NamespacedKey
            get() = NamespacedKey(instance, "towerKey")
        val ENEMY_TYPES: NamespacedKey
            get() = NamespacedKey(instance, "enemyKey")
        val GAME_ITEMS: NamespacedKey
            get() = NamespacedKey(instance, "towerItems")

        // Per-object properties
        // -- Checkpoints --
        val CHECKPOINT_ID: NamespacedKey
            get() = NamespacedKey(instance, "checkpointId")
        val TARGET_CHECKPOINT_ID: NamespacedKey
            get() = NamespacedKey(instance, "targetCheckpointId")
        val STARTPOINT_ID: NamespacedKey
            get() = NamespacedKey(instance, "startpointId")

        // -- Towers --
        // Attack wait time is used in seconds, converted to ms
        val TOWER_RANGE: NamespacedKey
            get() = NamespacedKey(instance, "towerRange")
        val READY_TIME: NamespacedKey
            get() = NamespacedKey(instance, "towerReadyTime")
        val ATTACK_WAIT_TIME: NamespacedKey
            get() = NamespacedKey(instance, "attackWaitTime")
        val TOWER_DMG: NamespacedKey
            get() = NamespacedKey(instance, "towerDMG")

        // -- Enemies --
        val HEALTH_OWNER_UUID: NamespacedKey
            get() = NamespacedKey(instance, "owner_uuid")
    }

    override fun onEnable() {
        instance = this

        // Plugin startup logic
        logger.info {
            "Tower Defense Plugin - Starting Initialization"
        }

        // Register utils
        TaskUtility.initialize(this)

        logger.info {
            "Tower Defense Plugin - Utility Functions Registered"
        }

        // Register continuous events
        server.pluginManager.registerEvents(PlayerPlaceListener, this)
        server.pluginManager.registerEvents(EntityDeathListener, this)
        server.pluginManager.registerEvents(FireproofListener, this)
        server.pluginManager.registerEvents(PlayerHoldListener, this)
        server.pluginManager.registerEvents(EnemyHealthListener, this)

        logger.info {
            "Tower Defense Plugin - Continuous Listeners Registered"
        }

        // Set commands and behaviors
        getCommand("giveTDtower")?.setExecutor(GiveTower)
        getCommand("giveTDcheckpoint")?.setExecutor(GiveCheckpoint)
        getCommand("giveTDenemy")?.setExecutor(GiveEnemy)
        getCommand("giveTDstartpoint")?.setExecutor(GiveStartPoint)
        getCommand("giveTDendpoint")?.setExecutor(GiveEndPoint)
        getCommand("clearTDallcheckpoints")?.setExecutor(ClearCheckpoints)
        getCommand("toggleStandVisibility")?.setExecutor(ToggleStandVisibility)

        logger.info {
            "Tower Defense Plugin - Game Commands Verified"
        }

        // Scheduler tasks
        startTowerCheckTask()
        startEnemyCheckTask()

        logger.info {
            "Tower Defense Plugin - Scheduler Tasks Started"
        }

        logger.info {
            "Tower Defense Plugin - Initialization Process Complete!"
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info {
            "Tower Defense Plugin - Shut down all tasks"
        }
    }

    fun startTowerCheckTask() {
        // Makes a task every 5 game ticks to update object behavior based on new data
        Bukkit.getScheduler().runTaskTimer(
            this, Runnable {
                for (world in Bukkit.getWorlds()) {
                    TowerScheduler.checkAndHandleTowers(world)
                }
            },
            0L,
            CHECK_INTERVAL_TICKS
        )
    }

    fun startEnemyCheckTask() {
        // Makes a task every 5 game ticks to update object behavior based on new data
        Bukkit.getScheduler().runTaskTimer(
            this, Runnable {
                for (world in Bukkit.getWorlds()) {
                    EnemyScheduler.checkAndHandleEnemies(world)
                }
            },
            0L,
            CHECK_INTERVAL_TICKS
        )
    }
}
