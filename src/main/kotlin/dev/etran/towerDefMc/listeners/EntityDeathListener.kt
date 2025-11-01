package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameInstanceTracker
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.cleanUpEnemyHealthBar
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
                        GameRegistry.activeGames[gameId]?.waypointManager?.remove(gameElement)
                    }
                }
                "Enemy" -> {
                    // Track enemy kills for all players in the game
                    val gameId = GameInstanceTracker.getGameId(gameElement)
                    if (gameId != null) {
                        PlayerStatsManager.getAllPlayerStats(gameId).keys.forEach { playerUUID ->
                            PlayerStatsManager.recordKill(gameId, playerUUID)
                        }

                        // Award cash for killing enemy (50 cash per kill)
                        PlayerStatsManager.getAllPlayerStats(gameId).keys.forEach { playerUUID ->
                            PlayerStatsManager.awardCash(gameId, playerUUID, 50)
                        }
                    }
                }
            }
        }
    }
}