package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameInstanceTracker
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.cleanUpEnemyHealthBar
import dev.etran.towerDefMc.utils.DebugLogger
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.persistence.PersistentDataType

object EntityDeathListener : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val gameElement = event.entity

        val gameElementId = gameElement.persistentDataContainer.get(
            TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
        )

        event.drops.clear()

        if (gameElementId != null) {
            cleanUpEnemyHealthBar(gameElement)
            when (gameElementId) {
                "Checkpoint", "EndPoint" -> if (gameElement is ArmorStand) {
                    // Find which game this checkpoint belongs to and remove it
                    val gameId = GameInstanceTracker.getGameId(gameElement)
                    if (gameId != null) {
                        DebugLogger.logPath("Checkpoint removed from game $gameId")
                        GameRegistry.activeGames[gameId]?.waypointManager?.remove(gameElement)
                    }
                }
                "Enemy" -> {
                    // Get the game ID before unregistering
                    val gameId = GameInstanceTracker.getGameId(gameElement)

                    DebugLogger.logEnemy("Enemy ${gameElement.uniqueId} killed in game $gameId")

                    // Unregister the enemy from the game instance tracker
                    // This is critical for wave completion detection
                    GameInstanceTracker.unregisterEntity(gameElement)

                    if (gameId != null) {
                        // Track enemy kills for all players in the game
                        PlayerStatsManager.getAllPlayerStats(gameId).keys.forEach { playerUUID ->
                            PlayerStatsManager.recordKill(gameId, playerUUID)
                        }

                        // Don't award extra cash on kill - cash is already awarded for damage dealt
                        // This prevents double-dipping (damage cash + kill bonus)
                    }
                }
            }
        }
    }
}