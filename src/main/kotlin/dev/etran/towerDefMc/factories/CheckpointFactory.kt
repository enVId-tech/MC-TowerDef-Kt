package dev.etran.towerDefMc.factories

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object CheckpointFactory {
    fun newCheckpoint(amount: Int = 1): ItemStack {
        val checkPointSpawn = ItemStack(Material.GOLD_BLOCK, amount)

        // Add an NBT tag to the checkpoint that says that it is actually a checkpoint and not a generic gold block
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

        // Prevents the player from placing checkpoints on top of each other, possibly causing bugs
        if (location.getNearbyEntities(0.5, 1.0, 0.5).count() >= 1) {
            player.sendMessage("You cannot place a checkpoint here!")
            return
        }

        val world = location.world
        val entity = world.spawnEntity(location, EntityType.ARMOR_STAND)

        // Add NBT data to armor stands
        entity.isInvulnerable = true
        entity.fireTicks = 0

        // Global accessor for checkpoint
        entity.persistentDataContainer.set(TowerDefMC.TOWER_KEY, PersistentDataType.STRING, "Checkpoint")

        // Take away 1 from the user if they aren't in creative mode
        if (player.gameMode != GameMode.CREATIVE) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}