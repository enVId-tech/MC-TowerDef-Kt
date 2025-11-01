package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Handles placement of Game Stats Display lecterns
 */
class GameStatsDisplayListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        if (item.type != Material.LECTERN) return

        val meta = item.itemMeta ?: return
        val isGameStatsDisplay = meta.persistentDataContainer.has(
            TowerDefMC.GAME_ITEMS,
            PersistentDataType.STRING
        ) && meta.persistentDataContainer.get(
            TowerDefMC.GAME_ITEMS,
            PersistentDataType.STRING
        ) == "Game_Stats_Display"

        if (!isGameStatsDisplay) return

        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true

            val clickedBlock = event.clickedBlock ?: return
            val blockFace = event.blockFace

            // Calculate where the lectern should be placed
            val lecternLocation = clickedBlock.getRelative(blockFace).location

            // Check if location is available
            if (lecternLocation.block.type != Material.AIR) {
                player.sendMessage("§cCannot place lectern here - block is not empty!")
                return
            }

            // Place the lectern block
            lecternLocation.block.type = Material.LECTERN

            // Set the lectern's facing direction based on the block face clicked
            val lecternBlockData = lecternLocation.block.blockData as? org.bukkit.block.data.type.Lectern
            if (lecternBlockData != null) {
                // Set facing direction opposite to the block face that was clicked
                lecternBlockData.facing = when (blockFace) {
                    org.bukkit.block.BlockFace.NORTH -> org.bukkit.block.BlockFace.SOUTH
                    org.bukkit.block.BlockFace.SOUTH -> org.bukkit.block.BlockFace.NORTH
                    org.bukkit.block.BlockFace.EAST -> org.bukkit.block.BlockFace.WEST
                    org.bukkit.block.BlockFace.WEST -> org.bukkit.block.BlockFace.EAST
                    org.bukkit.block.BlockFace.UP, org.bukkit.block.BlockFace.DOWN -> {
                        // If placed on top or bottom, use player's facing direction
                        val playerYaw = player.location.yaw
                        when {
                            playerYaw >= -45 && playerYaw < 45 -> org.bukkit.block.BlockFace.SOUTH
                            playerYaw >= 45 && playerYaw < 135 -> org.bukkit.block.BlockFace.WEST
                            playerYaw >= 135 || playerYaw < -135 -> org.bukkit.block.BlockFace.NORTH
                            else -> org.bukkit.block.BlockFace.EAST
                        }
                    }
                    else -> org.bukkit.block.BlockFace.NORTH // Default fallback
                }
                lecternLocation.block.blockData = lecternBlockData
            }

            // Mark this block as a game stats display lectern using chunk persistent data
            val chunk = lecternLocation.chunk
            val chunkData = chunk.persistentDataContainer

            // Store lectern locations in chunk data (we'll use a simple string format: "x,y,z")
            val key = TowerDefMC.createKey("game_stats_lecterns")
            val existingData = chunkData.get(key, PersistentDataType.STRING) ?: ""
            val locationString = "${lecternLocation.blockX},${lecternLocation.blockY},${lecternLocation.blockZ}"

            val newData = if (existingData.isEmpty()) {
                locationString
            } else {
                "$existingData;$locationString"
            }

            chunkData.set(key, PersistentDataType.STRING, newData)

            // Remove item from inventory if not in creative
            if (player.gameMode != org.bukkit.GameMode.CREATIVE &&
                player.gameMode != org.bukkit.GameMode.SPECTATOR) {
                player.inventory.itemInMainHand.amount -= 1
            }

            player.sendMessage("§a§lGame Stats Display lectern placed!")
            player.sendMessage("§7Stats will appear above this lectern when a game starts.")
        }
    }
}
