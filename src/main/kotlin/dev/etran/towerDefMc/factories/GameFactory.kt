package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.utils.getRenamableItemIntValue
import dev.etran.towerDefMc.utils.getRenamableItemValue
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object GameFactory {
    lateinit var plugin: TowerDefMC

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
    }

    private fun getNextAvailableGameId(): Int {
        val gamesDir = File(plugin.dataFolder, "games")

        // If the directory doesn't exist, start at 1
        if (!gamesDir.exists() || !gamesDir.isDirectory) {
            return 1
        }

        val gameFiles = gamesDir.listFiles { _, name ->
            name.startsWith("game_") && name.endsWith(".yml")
        } ?: return 1

        var maxId = 0
        val regex = Regex("game_(\\d+)\\.yml")

        for (file in gameFiles) {
            val match = regex.find(file.name)
            val idString = match?.groups?.get(1)?.value

            val id = idString?.toIntOrNull() ?: 0
            if (id > maxId) {
                maxId = id
            }
        }

        return maxId + 1
    }

    fun createGame(menu: CustomMenu): GameManager {
        val gameId = getNextAvailableGameId()
        val gameFileName = "game_${gameId}.yml"
        val gameFile = File(plugin.dataFolder, "games/$gameFileName")

        val maxHealth = getRenamableItemIntValue(menu, 10)
        val defaultCash = getRenamableItemIntValue(menu, 13)
        val gameName = getRenamableItemValue(menu, 16)

        val gameConfigurationData = GameSaveConfig(
            maxHealth = maxHealth ?: 100,
            defaultCash = defaultCash ?: 500,
            name = gameName ?: "Default Game Name",
            waves = emptyList(), // Start with empty waves list - can be added via Waves menu
            allowedTowers = emptyList()
        )

        val config = YamlConfiguration()

        val savableMap = mapOf(
            "maxHealth" to gameConfigurationData.maxHealth,
            "defaultCash" to gameConfigurationData.defaultCash,
            "name" to gameConfigurationData.name,
            "waves" to gameConfigurationData.waves,
            "allowedTowers" to gameConfigurationData.allowedTowers
        )

        config.set("game-data", savableMap)

        gameFile.parentFile.mkdirs()

        try {
            config.save(gameFile)
            plugin.logger.info("Successfully created and saved new game file: ${gameFile.name}")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save new game configuration for ID $gameId: ${e.message}")
        }

        val newGame = GameManager(gameId, gameConfigurationData)
        GameRegistry.addGame(newGame)
        return newGame
    }

    /**
     * Modify an existing game's configuration and save it
     * @param gameId The ID of the game to modify
     * @param maxHealth New max health value (null to keep current)
     * @param defaultCash New default cash value (null to keep current)
     * @param name New game name (null to keep current)
     * @param waves New waves list (null to keep current)
     * @param allowedTowers New allowed towers list (null to keep current)
     * @return true if successful, false if game not found
     */
    fun modifyGame(
        gameId: Int,
        maxHealth: Int? = null,
        defaultCash: Int? = null,
        name: String? = null,
        waves: List<dev.etran.towerDefMc.data.WaveData>? = null,
        allowedTowers: List<String>? = null
    ): Boolean {
        val game = GameRegistry.allGames[gameId] ?: return false

        maxHealth?.let { game.updateMaxHealth(it) }
        defaultCash?.let { game.updateDefaultCash(it) }
        name?.let { game.updateGameName(it) }
        waves?.let { game.updateWaves(it) }
        allowedTowers?.let { game.updateAllowedTowers(it) }

        plugin.logger.info("Modified and saved game $gameId (${game.config.name})")
        return true
    }

    /**
     * Create a game directly from configuration data
     * Useful for programmatic game creation
     */
    fun createGameFromConfig(config: GameSaveConfig): GameManager {
        val gameId = getNextAvailableGameId()
        val newGame = GameManager(gameId, config)
        GameRegistry.addGame(newGame)
        newGame.saveToFile()
        plugin.logger.info("Created game $gameId (${config.name}) from configuration")
        return newGame
    }
}