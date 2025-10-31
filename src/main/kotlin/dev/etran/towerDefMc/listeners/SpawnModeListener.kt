package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.managers.SpawnModeManager
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Handles spawn mode interactions
 */
class SpawnModeListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (!SpawnModeManager.isInSpawnMode(player)) return

        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true

            val location = if (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock != null) {
                event.clickedBlock!!.location.add(0.5, 1.0, 0.5)
            } else {
                player.location
            }

            SpawnModeManager.placeSpawn(player, location)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player

        if (SpawnModeManager.isInSpawnMode(player)) {
            event.isCancelled = true

            // Exit spawn mode on main thread
            Bukkit.getScheduler().runTask(
                dev.etran.towerDefMc.TowerDefMC.instance,
                Runnable {
                    SpawnModeManager.endSpawnMode(player)
                }
            )
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Clean up if player leaves while in spawn mode
        if (SpawnModeManager.isInSpawnMode(event.player)) {
            SpawnModeManager.endSpawnMode(event.player)
        }
    }
}

