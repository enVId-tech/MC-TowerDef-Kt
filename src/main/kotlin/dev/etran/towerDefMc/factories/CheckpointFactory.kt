package dev.etran.towerDefMc.factories

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.placeElement
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
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
        val entity = placeElement(event, "checkpoint", "tdef_checkpoint", "CheckPoint")

        if (entity !is Entity) return

        var checkpointCount = 0

        for (entity in event.player.world.entities) {
            if (entity.type == EntityType.ARMOR_STAND) {
                val armorStand = entity as ArmorStand

                val tag = NBT.get<Int>(armorStand, { nbt -> nbt.getInteger("tdef_checkpoint_id")})

                if (tag != null) checkpointCount++
            }
        }

        NBT.modify(entity) { nbt -> nbt.setInteger("tdef_checkpoint_id", checkpointCount + 1) }

        val player = event.player

        // Global accessor for checkpoint
        entity.persistentDataContainer.set(TowerDefMC.GAME_ELEMENT_KEY, PersistentDataType.STRING, "Checkpoint")

        // Take away 1 from the user if they aren't in creative mode
        if (player.gameMode != GameMode.CREATIVE) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}