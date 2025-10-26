package dev.etran.towerDefMc.factories

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
        val endPointSpawn = ItemStack(Material.DIAMOND_BLOCK, amount)

        // Get the current item metadata (which is mutable)
        val meta = endPointSpawn.itemMeta ?: return endPointSpawn // Fallback if meta cannot be retrieved

        // Modify the Persistent Data Container within the metaobject
        meta.persistentDataContainer.set(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "EndPoint")

        endPointSpawn.itemMeta = meta

        return endPointSpawn
    }

    fun endPointPlace(event: PlayerInteractEvent) {
        val entity = placeElement(event, "endpoint")
        val player = event.player

        if (entity == null) return

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