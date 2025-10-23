package dev.etran.towerDefMc

import dev.etran.towerDefMc.commands.TestCommand
import dev.etran.towerDefMc.listeners.PlayerJoinListener
import dev.etran.towerDefMc.listeners.PlayerPlaceListener
import dev.etran.towerDefMc.schedulers.TowerScheduler
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class TowerDefMC : JavaPlugin() {

    private val CHECK_INTERVAL_TICKS: Long = 5L

    companion object {
        lateinit var instance: TowerDefMC
        lateinit var TOWER_KEY: NamespacedKey
    }

    override fun onEnable() {
        instance = this
        TOWER_KEY = NamespacedKey(this, "TOWER_ID")
        // Plugin startup logic
        logger.info {
            "Hello World!"
        }

        server.pluginManager.registerEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(PlayerPlaceListener(), this)

        getCommand("gettower")?.setExecutor(TestCommand())

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
