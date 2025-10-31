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
    var allGames: MutableMap<Int, GameManager> = mutableMapOf()
    lateinit var plugin: TowerDefMC

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
        allGames = mutableMapOf()
    }

    fun saveAllActiveGames() {
        activeGames.values.forEach { game ->
            saveGame(game)
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
                config = gameConfigurationData
            )

            allGames[gameId] = newGameManager
            plugin.logger.info("Loaded Game $gameId (${newGameManager.config.name})")
        }
    }

    fun getNames(game: GameManager): String {
        return game.config.name
    }

    fun saveGame(game: GameManager) {
        saveGameConfig(game.gameId, game.config)
    }

    fun saveGameConfig(gameId: Int, config: GameSaveConfig) {
        val gamesDir = File(plugin.dataFolder, "games")
        if (!gamesDir.exists()) {
            gamesDir.mkdirs()
        }

        val configFile = File(gamesDir, "game_$gameId.yml")
        val yamlConfig = YamlConfiguration()

        yamlConfig.set("game-data.maxHealth", config.maxHealth)
        yamlConfig.set("game-data.defaultCash", config.defaultCash)
        yamlConfig.set("game-data.name", config.name)
        yamlConfig.set("game-data.waves", config.waves)
        yamlConfig.set("game-data.allowedTowers", config.allowedTowers)

        yamlConfig.save(configFile)
        plugin.logger.info("Saved Game $gameId (${config.name})")
    }

    fun addGame(game: GameManager) {
        // Use the game's existing ID instead of generating a new one
        allGames[game.gameId] = game
        activeGames[game.gameId] = game
        saveGame(game)
    }

    fun removeGame(game: GameManager) {
        val gameId = game.gameId
        allGames.remove(gameId)
        activeGames.remove(gameId)

        // Delete the file
        val gamesDir = File(plugin.dataFolder, "games")
        val configFile = File(gamesDir, "game_${gameId}.yml")
        if (configFile.exists()) {
            configFile.delete()
            plugin.logger.info("Deleted Game $gameId (${game.config.name})")
        }
    }

    /**
     * Find the active game that a player is currently in
     * @param playerUUID The UUID of the player
     * @return The GameManager instance if the player is in a game, null otherwise
     */
    fun getGameByPlayer(playerUUID: java.util.UUID): GameManager? {
        return activeGames.values.firstOrNull { game ->
            game.hasPlayer(playerUUID)
        }
    }
}