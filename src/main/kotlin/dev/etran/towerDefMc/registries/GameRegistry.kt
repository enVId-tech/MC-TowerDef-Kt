package dev.etran.towerDefMc.registries

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.factories.GameFactory
import dev.etran.towerDefMc.managers.GameManager
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaveData
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object GameRegistry {
    val activeGames: MutableMap<Int, GameManager> = mutableMapOf()
    lateinit var allGames: MutableMap<Int, GameManager>
    lateinit var plugin: TowerDefMC

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
    }

    fun saveAllActiveGames() {
        activeGames.values.forEach { game ->

        }
    }

    fun getAllGameFiles(): List<File> {
        val gamesDir = File(plugin.dataFolder, "games")

        if (!gamesDir.exists() || !gamesDir.isDirectory) {
            plugin.logger.warning("Games directory not found at: ${gamesDir.absolutePath}")
            return emptyList()
        }

        val configFiles = gamesDir.listFiles { _, name ->
            name.endsWith(".yml")
        }

        return configFiles?.toList() ?: emptyList()
    }

    fun loadAllSavedGames() {
        val gameFiles = getAllGameFiles()

        gameFiles.forEach { file ->
            val idRegex = Regex("game_(\\d+)\\.yml")
            val match = idRegex.find(file.name)
            val gameId = match?.groups?.get(1)?.value?.toIntOrNull()

            if (gameId == null) {
                plugin.logger.warning("Skipping file ${file.name}: Invalid ID format.")
                return@forEach
            }

            val config = YamlConfiguration.loadConfiguration(file)

            val gameConfigurationData = GameSaveConfig(
                maxHealth = config.getInt("game-data.maxHealth"),
                defaultCash = config.getInt("game-data.defaultCash"),
                name = config.getString("game-data.name") ?: "",
                waves = (config.getList("game-data.waves") ?: emptyList()) as List<WaveData>,
                allowedTowers = (config.getList("game-data.allowedTowers") ?: emptyList()) as List<String>
            )

            val newGameManager = GameManager(
                gameId = gameId,
                gameConfig = gameConfigurationData
            )

            allGames[gameId] = newGameManager
            plugin.logger.info("Loaded Game $gameId (${newGameManager.gameConfig.name})")
        }
    }

    fun getNames(game: GameManager) {

    }

    fun saveGame(game: GameManager) {

    }

    fun addGame(game: GameManager) {
        activeGames[activeGames.size] = game
    }
}