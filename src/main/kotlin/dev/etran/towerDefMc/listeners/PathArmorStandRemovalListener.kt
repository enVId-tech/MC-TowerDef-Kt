package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Handles removal of path armor stands outside of modification menu
 * Updates paths accordingly when checkpoints are removed
 */
class PathArmorStandRemovalListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity !is ArmorStand) return

        handlePathArmorStandRemoval(entity)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        // Check if armor stand is being killed
        if (event.isCancelled) return

        val entity = event.entity
        if (entity !is ArmorStand) return

        // Check if this will kill the armor stand
        if (entity.health - event.finalDamage <= 0) {
            handlePathArmorStandRemoval(entity)
        }
    }

    private fun handlePathArmorStandRemoval(stand: ArmorStand) {
        val elementType = stand.persistentDataContainer.get(
            TowerDefMC.ELEMENT_TYPES,
            PersistentDataType.STRING
        ) ?: return

        // Only handle path-related armor stands
        if (elementType !in listOf("PathStart", "PathEnd", "PathCheckpoint")) return

        // Find which game this path belongs to
        for ((gameId, gameManager) in GameRegistry.allGames) {
            val handled = gameManager.pathManager.handleArmorStandRemoval(stand)

            if (handled) {
                // Notify nearby players
                val location = stand.location
                val nearbyPlayers = location.world.getNearbyEntities(location, 50.0, 50.0, 50.0)
                    .filterIsInstance<Player>()

                when (elementType) {
                    "PathStart", "PathEnd" -> {
                        nearbyPlayers.forEach { player ->
                            player.sendMessage("§c§l[Path System] §7A path's ${if (elementType == "PathStart") "start" else "end"} point was removed. The entire path has been deleted.")
                        }
                    }
                    "PathCheckpoint" -> {
                        nearbyPlayers.forEach { player ->
                            player.sendMessage("§e§l[Path System] §7A checkpoint was removed from a path.")
                        }
                    }
                }

                break
            }
        }
    }
}

