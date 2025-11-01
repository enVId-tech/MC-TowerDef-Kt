package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.PathCreationSession
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Handles interactive path creation where players place waypoints with items
 */
class PathCreationListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (!PathCreationSession.isInSession(player)) return

        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) return

        val meta = item.itemMeta ?: return
        val isPathItem = meta.persistentDataContainer.has(
            TowerDefMC.createKey("pathCreationItem"), PersistentDataType.STRING
        )

        if (!isPathItem) return

        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true

            val location = if (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock != null) {
                event.clickedBlock!!.location.add(0.5, 1.0, 0.5)
            } else {
                player.location.clone()
            }

            // Determine which item was clicked
            when (item.type) {
                Material.LIME_WOOL -> {
                    // Start point
                    PathCreationSession.handlePlacement(player, location)
                }

                Material.YELLOW_WOOL -> {
                    // Checkpoint
                    PathCreationSession.handlePlacement(player, location)
                }

                Material.RED_WOOL -> {
                    // End point or switch to end point phase
                    val phase = PathCreationSession.getSessionPhase(player)
                    if (phase == PathCreationSession.PlacementPhase.CHECKPOINTS) {
                        // Switch to end point phase
                        PathCreationSession.switchToEndPointPhase(player)
                    } else if (phase == PathCreationSession.PlacementPhase.END_POINT) {
                        // Place end point
                        PathCreationSession.handlePlacement(player, location)
                    }
                }

                Material.BARRIER -> {
                    // Cancel path creation
                    PathCreationSession.cancelSession(player)
                    player.sendMessage("§cPath creation cancelled!")
                }

                else -> {}
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Clean up if player leaves while creating a path
        if (PathCreationSession.isInSession(event.player)) {
            PathCreationSession.cancelSession(event.player)
        }
    }
}
