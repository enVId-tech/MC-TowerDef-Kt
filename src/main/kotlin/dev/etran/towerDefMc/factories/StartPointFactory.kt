package dev.etran.towerDefMc.factories

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.placeElement
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object StartPointFactory {
    fun newStartElement(amount: Int = 1): ItemStack {
        val startPointSpawn = ItemStack(Material.EMERALD_BLOCK, 1)

        NBT.modify(startPointSpawn) { nbt -> nbt.setString("tdef_item_name", "StartPoint") }

        return startPointSpawn
    }

    fun startPointPlace(event: PlayerInteractEvent) {
        val entity = placeElement(event, "startpoint")

        if (entity !is Entity) return

        val player = event.player

        // Global accessor for checkpoint
        entity.persistentDataContainer.set(TowerDefMC.GAME_ELEMENT_KEY, PersistentDataType.STRING, "StartPoint")
        entity.persistentDataContainer.set(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, -1)

        // Take away 1 from the user if they aren't in creative mode
        if (player.gameMode != GameMode.CREATIVE) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}