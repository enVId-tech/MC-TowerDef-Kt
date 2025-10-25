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

object EndpointFactory {
    fun newEndElement(amount: Int = 1): ItemStack {
        val endPointSpawn = ItemStack(Material.DIAMOND_BLOCK, 1)

        NBT.modify(endPointSpawn) { nbt -> nbt.setString("tdef_item_name", "EndPoint") }

        return endPointSpawn
    }

    fun endPointPlace(event: PlayerInteractEvent) {
        val entity = placeElement(event, "endpoint")

        if (entity == null) return

        val player = event.player
        val world = player.world

        // Global accessor for checkpoint
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "EndPoint")
        entity.persistentDataContainer.set(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER,
            (CheckpointManager.checkpoints.size + 1)
        )
        CheckpointManager.add(entity)

        // Take away 1 from the user if they aren't in creative mode
        if (player.gameMode != GameMode.CREATIVE) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}