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
            "Hello World!"
        }

        server.pluginManager.registerEvents(PlayerPlaceListener(), this)

        getCommand("givettower")?.setExecutor(GiveTower())
        getCommand("givetcheckpoint")?.setExecutor(GiveCheckpoint())

        startTowerCheckTask()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info {
            "Goodbye World!"
        }
    }

    private fun startTowerCheckTask() {
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
