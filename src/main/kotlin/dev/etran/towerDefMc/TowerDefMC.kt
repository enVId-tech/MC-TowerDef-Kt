package dev.etran.towerDefMc

import org.bukkit.plugin.java.JavaPlugin

class TowerDefMC : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        logger.info {
            "Hello World!"
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info {
            "Goodbye World!"
        }
    }
}
