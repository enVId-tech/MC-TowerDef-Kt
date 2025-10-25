package dev.etran.towerDefMc.factories

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.CheckpointManager
import dev.etran.towerDefMc.utils.placeElement
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object CheckpointFactory {
    fun newCheckpoint(amount: Int = 1): ItemStack {
        val checkPointSpawn = ItemStack(Material.GOLD_BLOCK, amount)

        // Add an NBT tag to the checkpoint that says that it is actually a checkpoint and not a generic gold block
        NBT.modify(checkPointSpawn) { nbt ->
            nbt.setString("tdef_item_name", "CheckPoint")
        }

        return checkPointSpawn
    }

    fun checkPointPlace(event: PlayerInteractEvent) {
        val entity = placeElement(event, "checkpoint")
        val player = event.player

        if (entity == null) return

        val lastCheckpoint = CheckpointManager.checkpoints.values.lastOrNull()

        // Only check if the checkpoint isn't empty to avoid NoSuchElementException
        // If the final checkpoint ID is an endpoint, do not allow additional checkpoints
        if (lastCheckpoint != null && CheckpointManager.checkpoints.values.last().persistentDataContainer.get(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
            ).equals("EndPoint")
        ) {
            event.player.sendMessage("There is an existing endpoint on this path! Remove the endpoint before placing any new checkpoints")
            entity.remove()
            return
        }

        val correctNewId = CheckpointManager.add(entity)

        // 3. Set the PDC with the correct ID.
        entity.persistentDataContainer.set(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, correctNewId)

        // 4. Set the general type (Global accessor for checkpoint)
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Checkpoint")

        // 5. Item Consumption
        if (player.gameMode != GameMode.CREATIVE) {
            // NOTE: This assumes the item in hand is the checkpoint item.
            player.inventory.itemInMainHand.amount -= 1
        }
    }
}