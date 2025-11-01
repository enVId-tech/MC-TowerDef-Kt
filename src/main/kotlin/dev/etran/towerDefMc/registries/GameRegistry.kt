package dev.etran.towerDefMc.registries

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.*
import dev.etran.towerDefMc.managers.GameManager
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

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

            try {
                val config = YamlConfiguration.loadConfiguration(file)

                // Deserialize waves from maps
                val wavesData = config.getMapList("game-data.waves").map { waveMap ->
                    deserializeWave(waveMap)
                }

                val pathsData = config.getMapList("game-data.paths").mapNotNull { pathMap ->
                    deserializePath(pathMap)
                }

                val gameConfigurationData = GameSaveConfig(
                    maxHealth = config.getInt("game-data.maxHealth"),
                    defaultCash = config.getInt("game-data.defaultCash"),
                    name = config.getString("game-data.name") ?: "",
                    waves = wavesData,
                    allowedTowers = config.getStringList("game-data.allowedTowers"),
                    paths = pathsData
                )

                val newGameManager = GameManager(
                    gameId = gameId,
                    config = gameConfigurationData
                )

                allGames[gameId] = newGameManager
                plugin.logger.info("Loaded Game $gameId (${newGameManager.config.name})")
            } catch (e: Exception) {
                plugin.logger.severe("Failed to load game $gameId: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun deserializeWave(waveMap: Map<*, *>): WaveData {
        val name = waveMap["name"] as? String ?: "Unknown Wave"
        val minTime = (waveMap["minTime"] as? Number)?.toDouble() ?: 0.0
        val maxTime = (waveMap["maxTime"] as? Number)?.toDouble() ?: 60.0
        val waveHealth = (waveMap["waveHealth"] as? Number)?.toDouble()
        val cashGiven = (waveMap["cashGiven"] as? Number)?.toInt() ?: 0

        val sequenceList = waveMap["sequence"] as? List<*> ?: emptyList<Any>()
        val sequence = sequenceList.mapNotNull { commandMap ->
            if (commandMap is Map<*, *>) {
                deserializeWaveCommand(commandMap)
            } else null
        }

        return WaveData(
            name = name,
            sequence = sequence,
            minTime = minTime,
            maxTime = maxTime,
            waveHealth = waveHealth,
            cashGiven = cashGiven
        )
    }

    private fun deserializeWaveCommand(commandMap: Map<*, *>): WaveCommand? {
        val type = commandMap["type"] as? String ?: return null

        return when (type) {
            "WAIT" -> {
                val waitSeconds = (commandMap["waitSeconds"] as? Number)?.toDouble() ?: 0.0
                WaitCommand(waitSeconds)
            }
            "ENEMY_SPAWN" -> {
                val intervalSeconds = (commandMap["intervalSeconds"] as? Number)?.toDouble() ?: 1.0
                val enemiesMap = commandMap["enemies"] as? Map<*, *> ?: emptyMap<Any, Any>()
                val enemies = enemiesMap.mapNotNull { (key, value) ->
                    val enemyId = key as? String
                    val count = (value as? Number)?.toInt()
                    if (enemyId != null && count != null) {
                        enemyId to count
                    } else null
                }.toMap()
                EnemySpawnCommand(enemies, intervalSeconds)
            }
            else -> null
        }
    }

    fun getNames(game: GameManager): String {
        return game.config.name
    }

    fun saveGame(game: GameManager) {
        saveGameConfig(game.gameId, game.config)
    }


    private fun serializeWave(wave: WaveData): Map<String, Any?> {
        return mapOf(
            "name" to wave.name,
            "minTime" to wave.minTime,
            "maxTime" to wave.maxTime,
            "waveHealth" to wave.waveHealth,
            "cashGiven" to wave.cashGiven,
            "sequence" to wave.sequence.map { command ->
                serializeWaveCommand(command)
            }
        )
    }

    private fun serializeWaveCommand(command: WaveCommand): Map<String, Any> {
        return when (command) {
            is WaitCommand -> mapOf(
                "type" to "WAIT",
                "waitSeconds" to command.waitSeconds
            )
            is EnemySpawnCommand -> mapOf(
                "type" to "ENEMY_SPAWN",
                "intervalSeconds" to command.intervalSeconds,
                "enemies" to command.enemies
            )
            else -> mapOf("type" to "UNKNOWN")
        }
    }

    private fun serializePath(path: SerializablePathData): Map<String, Any?> {
        return mapOf(
            "id" to path.id,
            "name" to path.name,
            "startPoint" to serializeLocation(path.startPoint),
            "checkpoints" to path.checkpoints.map { serializeLocation(it) },
            "endPoint" to serializeLocation(path.endPoint),
            "isVisible" to path.isVisible
        )
    }

    private fun serializeLocation(location: SerializableLocation): Map<String, Any> {
        return mapOf(
            "world" to location.world,
            "x" to location.x,
            "y" to location.y,
            "z" to location.z,
            "yaw" to location.yaw,
            "pitch" to location.pitch
        )
    }

    private fun deserializePath(pathMap: Map<*, *>): SerializablePathData? {
        try {
            val id = (pathMap["id"] as? Number)?.toInt() ?: return null
            val name = pathMap["name"] as? String ?: "Unnamed Path"
            val startPointMap = pathMap["startPoint"] as? Map<*, *> ?: return null
            val checkpointsList = pathMap["checkpoints"] as? List<*> ?: emptyList<Any>()
            val endPointMap = pathMap["endPoint"] as? Map<*, *> ?: return null
            val isVisible = pathMap["isVisible"] as? Boolean ?: true

            val startPoint = deserializeLocation(startPointMap) ?: return null
            val checkpoints = checkpointsList.mapNotNull { checkpoint ->
                if (checkpoint is Map<*, *>) {
                    deserializeLocation(checkpoint)
                } else null
            }
            val endPoint = deserializeLocation(endPointMap) ?: return null

            return SerializablePathData(
                id = id,
                name = name,
                startPoint = startPoint,
                checkpoints = checkpoints,
                endPoint = endPoint,
                isVisible = isVisible
            )
        } catch (e: Exception) {
            plugin.logger.warning("Failed to deserialize path: ${e.message}")
            return null
        }
    }

    private fun deserializeLocation(locationMap: Map<*, *>): SerializableLocation? {
        try {
            val world = locationMap["world"] as? String ?: return null
            val x = (locationMap["x"] as? Number)?.toDouble() ?: return null
            val y = (locationMap["y"] as? Number)?.toDouble() ?: return null
            val z = (locationMap["z"] as? Number)?.toDouble() ?: return null
            val yaw = (locationMap["yaw"] as? Number)?.toFloat() ?: 0f
            val pitch = (locationMap["pitch"] as? Number)?.toFloat() ?: 0f

            return SerializableLocation(
                world = world,
                x = x,
                y = y,
                z = z,
                yaw = yaw,
                pitch = pitch
            )
        } catch (e: Exception) {
            plugin.logger.warning("Failed to deserialize location: ${e.message}")
            return null
        }
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
    fun getGameByPlayer(playerUUID: UUID): GameManager? {
        return activeGames.values.firstOrNull { game ->
            game.hasPlayer(playerUUID)
        }
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

        // Serialize waves to maps
        val wavesData = config.waves.map { wave ->
            serializeWave(wave)
        }
        yamlConfig.set("game-data.waves", wavesData)

        yamlConfig.set("game-data.allowedTowers", config.allowedTowers)

        // Serialize paths to maps
        val pathsData = config.paths.map { path ->
            serializePath(path)
        }
        yamlConfig.set("game-data.paths", pathsData)

        yamlConfig.save(configFile)
        plugin.logger.info("Saved Game $gameId (${config.name}) with ${config.paths.size} paths")
    }
}