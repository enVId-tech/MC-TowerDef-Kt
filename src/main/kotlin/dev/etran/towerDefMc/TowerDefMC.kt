package dev.etran.towerDefMc

import dev.etran.towerDefMc.commands.GiveCheckpoint
import dev.etran.towerDefMc.commands.GiveTower
import dev.etran.towerDefMc.listeners.PlayerPlaceListener
import dev.etran.towerDefMc.schedulers.TowerScheduler
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class TowerDefMC : JavaPlugin() {
    private val CHECK_INTERVAL_TICKS: Long = 5L
    companion object {
        // Set by onEnable, clear in onDisable
        lateinit var instance: TowerDefMC
            private set

        // Access the key via the current instance
        val TOWER_KEY: NamespacedKey
            get() = NamespacedKey(instance, "towerKey")
    }
    override fun onEnable() {
        instance = this

        // Plugin startup logic
        logger.info {
            "Tower Defense Plugin Enabled!"
        }

        // Register continuous events
        server.pluginManager.registerEvents(PlayerPlaceListener(), this)

        // Set commands and behaviors
        getCommand("givettower")?.setExecutor(GiveTower())
        getCommand("givetcheckpoint")?.setExecutor(GiveCheckpoint())

        // Scheduler tasks
        startTowerCheckTask()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info {
            "Tower Defense Plugin Successfully Disabled"
        }
    }

    private fun startTowerCheckTask() {
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
}
