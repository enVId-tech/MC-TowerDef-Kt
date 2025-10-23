package dev.etran.towerDefMc.factories

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object CheckpointFactory {
    fun newCheckpoint(amount: Int = 1): ItemStack {
        val checkPointSpawn = ItemStack(Material.GOLD_BLOCK, amount)

        NBT.modify(checkPointSpawn) { nbt ->
            nbt.setString("tdef_item_name", "Checkpoint")
        }

        return checkPointSpawn
    }

    fun checkPointPlace(event: PlayerInteractEvent) {
        event.isCancelled = true


        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val player = event.player

        player.sendBlockChange(location, location.block.blockData)

        if (location.getNearbyEntities(0.5, 1.0, 0.5).count() >= 1) {
            player.sendMessage("You cannot place a tower here!")
            return
        }

        val world = location.world
        val entity = world.spawnEntity(location, EntityType.ARMOR_STAND)
        entity.isInvulnerable = true
        entity.fireTicks = 0
        entity.persistentDataContainer.set(TowerDefMC.TOWER_KEY, PersistentDataType.STRING, "Checkpoint")
        event.player.inventory.itemInMainHand.amount -= 1
    }
}