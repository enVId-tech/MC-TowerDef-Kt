package dev.etran.towerDefMc.registries

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameManager
import org.bukkit.configuration.file.YamlConfiguration

object GameRegistry {
    val activeGames: MutableMap<Int, GameManager> = mutableMapOf()

    lateinit var plugin: TowerDefMC

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
    }

    fun saveAllActiveGames() {
        activeGames.values.forEach { game ->

        }
    }

    fun loadGame(game: GameManager) {

    }

    fun saveGame(game: GameManager) {

    }

    fun addGame(game: GameManager) {
        activeGames[activeGames.size] = game
    }
}