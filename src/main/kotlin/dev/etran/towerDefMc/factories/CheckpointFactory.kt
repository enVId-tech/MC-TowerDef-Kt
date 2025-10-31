package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.CheckpointManager
import dev.etran.towerDefMc.utils.placeElement
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object CheckpointFactory {
    fun newCheckpoint(amount: Int = 1): ItemStack {
        val checkPointSpawn = ItemStack(Material.GOLD_BLOCK, amount)

        // Get the current item metadata (which is mutable)
        val meta = checkPointSpawn.itemMeta ?: return checkPointSpawn // Fallback if meta cannot be retrieved

        // Modify the Persistent Data Container within the metaobject
        meta.persistentDataContainer.set(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Checkpoint")

        checkPointSpawn.itemMeta = meta

        return checkPointSpawn
    }

    fun checkPointPlace(event: PlayerInteractEvent, checkpointManager: CheckpointManager) {
        val entity = placeElement(event, "checkpoint")
        val player = event.player

        if (entity == null) return

        val lastCheckpoint = checkpointManager.checkpoints.values.lastOrNull()

        // Only check if the checkpoint isn't empty to avoid NoSuchElementException
        // If the final checkpoint ID is an endpoint, do not allow additional checkpoints
        if (lastCheckpoint != null && checkpointManager.checkpoints.values.last().persistentDataContainer.get(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
            ).equals("EndPoint")
        ) {
            event.player.sendMessage("There is an existing endpoint on this path! Remove the endpoint before placing any new checkpoints")
            entity.remove()
            return
        }

        val correctNewId = checkpointManager.add(entity as ArmorStand)

        // Set the PDC with the correct ID.
        entity.persistentDataContainer.set(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, correctNewId)

        // Set the general type (Global accessor for checkpoint)
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Checkpoint")

        // Validate path for all enemy types
        val hasValidPath = validateCheckpointPath(checkpointManager, entity.location)
        if (!hasValidPath) {
            player.sendMessage("§6Warning: Some enemies may not be able to reach this checkpoint!")
            player.sendMessage("§6Make sure there is a clear path from previous checkpoints.")
        }

        // Take away 1 from the user if they aren't in creative or spectator mode.
        if (player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }

    private fun validateCheckpointPath(checkpointManager: CheckpointManager, location: org.bukkit.Location): Boolean {
        // Simple validation: check if there's a reasonable distance from previous checkpoint
        val checkpoints = checkpointManager.checkpoints
        if (checkpoints.isEmpty()) return true

        val lastCheckpoint = checkpoints[checkpoints.size]
        if (lastCheckpoint != null) {
            val distance = location.distance(lastCheckpoint.location)
            // Warn if checkpoints are too far apart (e.g., > 50 blocks)
            return distance <= 50.0
        }
        return true
    }
}