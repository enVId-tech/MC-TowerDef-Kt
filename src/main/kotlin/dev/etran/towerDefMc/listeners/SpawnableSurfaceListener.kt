package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.SpawnModeManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

object SpawnableSurfaceListener : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        // Check if player is holding a spawnable surface item
        val meta = item.itemMeta ?: return
        val surfaceId = meta.persistentDataContainer.get(
            TowerDefMC.createKey("spawnable_surface_id"),
            PersistentDataType.INTEGER
        ) ?: return

        val gameId = meta.persistentDataContainer.get(
            TowerDefMC.createKey("game_id"),
            PersistentDataType.INTEGER
        ) ?: return

        // Check if player is in spawnable surface mode
        if (!SpawnModeManager.isInSpawnableSurfaceMode(player)) {
            // Start the mode if not already in it
            SpawnModeManager.startSpawnableSurfaceMode(player, gameId, surfaceId)
        }

        val clickedBlock = event.clickedBlock ?: return

        when (event.action) {
            Action.RIGHT_CLICK_BLOCK -> {
                // Place spawnable surface block
                event.isCancelled = true
                SpawnModeManager.placeSpawnableSurfaceBlock(player, clickedBlock.location)
            }
            Action.LEFT_CLICK_BLOCK -> {
                // Remove spawnable surface block
                event.isCancelled = true
                SpawnModeManager.removeSpawnableSurfaceBlock(player, clickedBlock.location)
            }
            else -> {}
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        val item = event.itemDrop.itemStack

        // Check if dropping a spawnable surface item
        val meta = item.itemMeta ?: return
        val surfaceId = meta.persistentDataContainer.get(
            TowerDefMC.createKey("spawnable_surface_id"),
            PersistentDataType.INTEGER
        ) ?: return

        // Player is dropping the item, exit spawnable surface mode
        if (SpawnModeManager.isInSpawnableSurfaceMode(player)) {
            SpawnModeManager.endSpawnableSurfaceMode(player)
            event.itemDrop.remove()
            event.isCancelled = true
            player.sendMessage("§aExited spawnable surface editing mode")
        }
    }
}

