package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.CustomMenu
import dev.etran.towerDefMc.utils.getRenamableItemIntValue
import dev.etran.towerDefMc.utils.getRenamableItemValue
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.persistence.PersistentDataType
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

    fun createGame(menu: CustomMenu) {
        val file = File(plugin.dataFolder, "game-config.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        val maxHealth = getRenamableItemIntValue(menu, 10)
        val defaultCash = getRenamableItemIntValue(menu, 13)
        val gameName = getRenamableItemValue(menu, 16)

        if (maxHealth == null || defaultCash == null || gameName == null) {
            return
        }

        val gameConfigurationData = mapOf(
            "maxHealth" to maxHealth,
            "defaultCash" to defaultCash,
            "name" to gameName,
            "waves" to emptyMap<Int, Any>(), // TODO: Change any to a proper wave type once a wave system has been established
            "allowedTowers" to emptySet<String>()
        )

        config.set("gameConfigurationData", gameConfigurationData)
    }
}