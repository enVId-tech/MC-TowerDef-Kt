package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC

/**
 * Utility for managing debug logging throughout the plugin
 */
object DebugLogger {
    private lateinit var plugin: TowerDefMC

    // Debug flags for different systems
    var waveDebug: Boolean = false
    var enemyDebug: Boolean = false
    var towerDebug: Boolean = false
    var gameDebug: Boolean = false
    var statsDebug: Boolean = false
    var pathDebug: Boolean = false

    // Master debug switch
    var masterDebug: Boolean = false

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
        loadConfig()
    }

    /**
     * Load debug settings from config
     */
    private fun loadConfig() {
        val config = plugin.config

        masterDebug = config.getBoolean("debug.master", false)
        waveDebug = config.getBoolean("debug.waves", false)
        enemyDebug = config.getBoolean("debug.enemies", false)
        towerDebug = config.getBoolean("debug.towers", false)
        gameDebug = config.getBoolean("debug.game", false)
        statsDebug = config.getBoolean("debug.stats", false)
        pathDebug = config.getBoolean("debug.paths", false)
    }

    /**
     * Save debug settings to config
     */
    fun saveConfig() {
        val config = plugin.config

        config.set("debug.master", masterDebug)
        config.set("debug.waves", waveDebug)
        config.set("debug.enemies", enemyDebug)
        config.set("debug.towers", towerDebug)
        config.set("debug.game", gameDebug)
        config.set("debug.stats", statsDebug)
        config.set("debug.paths", pathDebug)

        plugin.saveConfig()
    }

    /**
     * Log wave-related debug messages
     */
    fun logWave(message: String) {
        if (masterDebug || waveDebug) {
            plugin.logger.info("[Wave] $message")
        }
    }

    /**
     * Log enemy-related debug messages
     */
    fun logEnemy(message: String) {
        if (masterDebug || enemyDebug) {
            plugin.logger.info("[Enemy] $message")
        }
    }

    /**
     * Log tower-related debug messages
     */
    fun logTower(message: String) {
        if (masterDebug || towerDebug) {
            plugin.logger.info("[Tower] $message")
        }
    }

    /**
     * Log game-related debug messages
     */
    fun logGame(message: String) {
        if (masterDebug || gameDebug) {
            plugin.logger.info("[Game] $message")
        }
    }

    /**
     * Log stats-related debug messages
     */
    fun logStats(message: String) {
        if (masterDebug || statsDebug) {
            plugin.logger.info("[Stats] $message")
        }
    }

    /**
     * Log path-related debug messages
     */
    fun logPath(message: String) {
        if (masterDebug || pathDebug) {
            plugin.logger.info("[Path] $message")
        }
    }

    /**
     * Always log important messages (warnings, errors, etc.)
     */
    fun logImportant(message: String) {
        plugin.logger.info(message)
    }

    /**
     * Toggle a specific debug flag
     */
    fun toggle(category: String): Boolean {
        return when (category.lowercase()) {
            "master" -> {
                masterDebug = !masterDebug
                saveConfig()
                masterDebug
            }

            "wave", "waves" -> {
                waveDebug = !waveDebug
                saveConfig()
                waveDebug
            }

            "enemy", "enemies" -> {
                enemyDebug = !enemyDebug
                saveConfig()
                enemyDebug
            }

            "tower", "towers" -> {
                towerDebug = !towerDebug
                saveConfig()
                towerDebug
            }

            "game" -> {
                gameDebug = !gameDebug
                saveConfig()
                gameDebug
            }

            "stats" -> {
                statsDebug = !statsDebug
                saveConfig()
                statsDebug
            }

            "path", "paths" -> {
                pathDebug = !pathDebug
                saveConfig()
                pathDebug
            }

            else -> false
        }
    }

    /**
     * Get status of all debug flags
     */
    fun getStatus(): Map<String, Boolean> {
        return mapOf(
            "master" to masterDebug,
            "waves" to waveDebug,
            "enemies" to enemyDebug,
            "towers" to towerDebug,
            "game" to gameDebug,
            "stats" to statsDebug,
            "paths" to pathDebug
        )
    }
}

