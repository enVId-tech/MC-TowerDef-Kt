package dev.etran.towerDefMc.factories

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.CheckpointManager
import dev.etran.towerDefMc.utils.findMaxCheckpoint
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
        val world = event.player.world

        if (entity == null) return

        CheckpointManager.add(entity)

        val newCheckpointId = findMaxCheckpoint(world) + 1

        entity.persistentDataContainer.set(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, newCheckpointId)

        val player = event.player
        // Global accessor for checkpoint
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Checkpoint")

        // Take away 1 from the user if they aren't in creative mode
        if (player.gameMode != GameMode.CREATIVE) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}