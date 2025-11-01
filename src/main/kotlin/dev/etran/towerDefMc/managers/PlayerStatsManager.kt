package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.data.PlayerStats
import dev.etran.towerDefMc.utils.DebugLogger
import java.util.UUID

/**
 * Manages player statistics for all active games
 */
object PlayerStatsManager {
    // Map of gameId -> Map of playerUUID -> PlayerStats
    private val gamePlayerStats: MutableMap<Int, MutableMap<UUID, PlayerStats>> = mutableMapOf()

    /**
     * Initialize a player's stats for a game
     */
    fun initializePlayer(gameId: Int, playerUUID: UUID, startingCash: Int = 500) {
        val gameStats = gamePlayerStats.getOrPut(gameId) { mutableMapOf() }
        gameStats[playerUUID] = PlayerStats(playerUUID, cash = startingCash)
    }

    /**
     * Get player stats for a specific game
     */
    fun getPlayerStats(gameId: Int, playerUUID: UUID): PlayerStats? {
        return gamePlayerStats[gameId]?.get(playerUUID)
    }

    /**
     * Get all player stats for a game
     */
    fun getAllPlayerStats(gameId: Int): Map<UUID, PlayerStats> {
        return gamePlayerStats[gameId] ?: emptyMap()
    }

    /**
     * Clear all stats for a game (when game ends)
     */
    fun clearGameStats(gameId: Int) {
        gamePlayerStats.remove(gameId)
    }

    /**
     * Remove a specific player from a game
     */
    fun removePlayer(gameId: Int, playerUUID: UUID) {
        gamePlayerStats[gameId]?.remove(playerUUID)
    }

    /**
     * Award cash to a player
     */
    fun awardCash(gameId: Int, playerUUID: UUID, amount: Int) {
        val stats = getPlayerStats(gameId, playerUUID)
        if (stats == null) {
            // Player stats don't exist - initialize them first
            DebugLogger.logStats("Warning: Player $playerUUID had no stats in game $gameId, initializing now")
            initializePlayer(gameId, playerUUID, 0)
            getPlayerStats(gameId, playerUUID)?.addCash(amount)
        } else {
            stats.addCash(amount)
        }
    }

    /**
     * Try to spend cash, returns true if successful
     * Players in creative mode always have infinite cash
     */
    fun spendCash(gameId: Int, playerUUID: UUID, amount: Int): Boolean {
        // Check if player is in creative mode - if so, they have infinite cash
        val player = org.bukkit.Bukkit.getPlayer(playerUUID)
        if (player != null && player.gameMode == org.bukkit.GameMode.CREATIVE) {
            return true
        }

        return getPlayerStats(gameId, playerUUID)?.spendCash(amount) ?: false
    }

    /**
     * Record a kill for a player
     */
    fun recordKill(gameId: Int, playerUUID: UUID) {
        val stats = getPlayerStats(gameId, playerUUID)
        if (stats == null) {
            DebugLogger.logStats("Warning: Player $playerUUID had no stats in game $gameId when recording kill")
            initializePlayer(gameId, playerUUID, 0)
            getPlayerStats(gameId, playerUUID)?.addKill()
        } else {
            stats.addKill()
        }
    }

    /**
     * Record damage dealt by a player
     */
    fun recordDamage(gameId: Int, playerUUID: UUID, damage: Double) {
        val stats = getPlayerStats(gameId, playerUUID)
        if (stats == null) {
            DebugLogger.logStats("Warning: Player $playerUUID had no stats in game $gameId when recording damage")
            initializePlayer(gameId, playerUUID, 0)
            getPlayerStats(gameId, playerUUID)?.addDamage(damage)
        } else {
            stats.addDamage(damage)
        }
    }

    /**
     * Record tower placement
     */
    fun recordTowerPlaced(gameId: Int, playerUUID: UUID) {
        val stats = getPlayerStats(gameId, playerUUID)
        if (stats == null) {
            DebugLogger.logStats("Warning: Player $playerUUID had no stats in game $gameId when recording tower placement")
            initializePlayer(gameId, playerUUID, 0)
            getPlayerStats(gameId, playerUUID)?.addTowerPlaced()
        } else {
            stats.addTowerPlaced()
        }
    }

    /**
     * Record tower upgrade
     */
    fun recordTowerUpgraded(gameId: Int, playerUUID: UUID) {
        val stats = getPlayerStats(gameId, playerUUID)
        if (stats == null) {
            DebugLogger.logStats("Warning: Player $playerUUID had no stats in game $gameId when recording tower upgrade")
            initializePlayer(gameId, playerUUID, 0)
            getPlayerStats(gameId, playerUUID)?.addTowerUpgraded()
        } else {
            stats.addTowerUpgraded()
        }
    }

    /**
     * Record wave completion for all players in game
     */
    fun recordWaveCompletion(gameId: Int) {
        gamePlayerStats[gameId]?.values?.forEach { it.completeWave() }
    }
}
