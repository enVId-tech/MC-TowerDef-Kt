package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.CustomMenu
import dev.etran.towerDefMc.utils.getRenamableItemIntValue
import dev.etran.towerDefMc.utils.getRenamableItemValue
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class GameManager() {
    val games = mutableMapOf<String, GameManager>()

    companion object {
        lateinit var plugin: Plugin
    }

    constructor(plugin: TowerDefMC) : this() {
        CheckpointManager.plugin = plugin
    }

    fun add(gameManager: GameManager) {

    }

    fun remove(gameManager: GameManager) {

    }

    fun saveGame(menu: CustomMenu) {
        val file = File(plugin.dataFolder, "game-config.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        val maxHealth = getRenamableItemIntValue(menu, 10)
        val defaultCash = getRenamableItemIntValue(menu, 13)
        val gameName = getRenamableItemValue(menu, 16)

        if (maxHealth == null || defaultCash == null || gameName == null) {
            return
        }

        val gameConfigurationData = games.entries.map{ game ->

            mapOf(
                "maxHealth" to maxHealth,
                "defaultCash" to defaultCash,
                "name" to gameName,
                "waves" to emptyMap<Int, Any>(), // TODO: Change any to a proper wave type once a wave system has been established
                "allowedTowers" to emptySet<String>()
            )
        }

        config.set ("gameConfigurationData", gameConfigurationData)
    }
}