package dev.etran.towerDefMc

import dev.etran.towerDefMc.commands.TestCommand
import dev.etran.towerDefMc.listeners.PlayerJoinListener
import dev.etran.towerDefMc.listeners.PlayerPlaceListener
import org.bukkit.plugin.java.JavaPlugin

class TowerDefMC : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        logger.info {
            "Hello World!"
        }

        server.pluginManager.registerEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(PlayerPlaceListener(), this)

        getCommand("gettower")?.setExecutor(TestCommand())
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info {
            "Goodbye World!"
        }
    }
}
