package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

object GameFactory {
    private lateinit var plugin: Plugin

    /**
     * Sets plugin reference
     */
    fun initialize(pluginInstance: TowerDefMC) {
        plugin = pluginInstance
    }

    fun createGame(maxHealth: Double, defaultCash: Double) {
        val file = File(plugin.dataFolder, "game-config.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        val gameConfigurationData = mapOf(
            "maxHealth" to maxHealth,
            "defaultCash" to defaultCash,
            "waves" to emptyMap<Int, Any>(), // TODO: Change any to a proper wave type once a wave system has been established
            "allowedTowers" to emptySet<String>()
        )


    }
}