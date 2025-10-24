package dev.etran.towerDefMc.factories


import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.iface.ReadWriteNBT
import dev.etran.towerDefMc.TowerDefMC
import net.kyori.adventure.util.TriState
import org.bukkit.GameMode

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

        // Add identifier for the end rod to make sure it is a spawn object and not just an end rod
        NBT.modify(towerSpawn) { nbt ->
            nbt.setString("tdef_item_name", "Tower 1")
        }

        return towerSpawn
    }

    fun towerPlace(event: PlayerInteractEvent) {
        event.isCancelled = true

        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val player = event.player

        // TODO: Add this into a configuration file for the user to be able to make custom towers later on
        if (location.getNearbyEntities(0.5, 1.0, 0.5).count() >= 1) {
            player.sendMessage("You cannot place a tower here!")
            return
        }

        val world = location.world
        val entity = world.spawnEntity(location, EntityType.ZOMBIE)

        // Add NBT data if only the zombie exists after calling spawn
        if (entity is LivingEntity) {
            entity.setAI(false)
            entity.isInvulnerable = true
            entity.fireTicks = 0
            entity.visualFire = TriState.FALSE
            entity.isPersistent = true
            entity.isSilent = true
            entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Tower")
            entity.persistentDataContainer.set(TowerDefMC.TOWER_KEY, PersistentDataType.STRING, "Basic_Tower_1")
        }

        // Take away 1 from the user if they aren't in creative mode
        if (player.gameMode != GameMode.CREATIVE) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}