package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.StartpointManager
import dev.etran.towerDefMc.utils.placeElement
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object StartPointFactory {
    fun newStartElement(amount: Int = 1): ItemStack {
        val startPointSpawn = ItemStack(Material.EMERALD_BLOCK, amount)

        // Get the current item metadata (which is mutable)
        val meta = startPointSpawn.itemMeta ?: return startPointSpawn // Fallback if meta cannot be retrieved

        // Modify the Persistent Data Container within the metaobject
        meta.persistentDataContainer.set(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "StartPoint")

        startPointSpawn.itemMeta = meta

        return startPointSpawn
    }

    fun startPointPlace(event: PlayerInteractEvent) {
        val entity = placeElement(event, "startpoint")

        if (entity !is Entity) return

        val player = event.player

        // Global accessor for checkpoint
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "StartPoint")
        entity.persistentDataContainer.set(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, -1)
        StartpointManager.add(entity)

        // Take away 1 from the user if they aren't in creative mode
        if (player.gameMode != GameMode.CREATIVE) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}