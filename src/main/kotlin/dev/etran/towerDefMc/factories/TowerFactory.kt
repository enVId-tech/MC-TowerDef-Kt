package dev.etran.towerDefMc.factories


import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.iface.ReadWriteNBT
import dev.etran.towerDefMc.TowerDefMC

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object TowerFactory {
    fun newBasicTower(amount: Int = 1): ItemStack {
        val towerSpawn = ItemStack(Material.END_ROD, amount)

        NBT.modify(towerSpawn) { nbt ->
            nbt.setString("tdef_item_name", "Tower 1")
        }

        return towerSpawn
    }

    fun towerPlace(event: PlayerInteractEvent) {
        event.isCancelled = true

        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val world = location.world
        val entity = world.spawnEntity(location, EntityType.ZOMBIE)

        if (entity is LivingEntity) {
            entity.setAI(false)
            entity.isInvulnerable = true
            entity.fireTicks = 0
            entity.persistentDataContainer.set(TowerDefMC.TOWER_KEY, PersistentDataType.STRING, "Basic_Tower_1")
        }


        event.player.inventory.itemInMainHand.amount -= 1
    }
}