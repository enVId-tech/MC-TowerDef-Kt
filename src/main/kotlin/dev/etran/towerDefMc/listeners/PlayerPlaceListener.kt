package dev.etran.towerDefMc.listeners

import de.tr7zw.nbtapi.NBT
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PlayerPlaceListener : Listener {
    @EventHandler
    fun onPlayerPlace(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val spawnEgg = ItemStack(Material.ZOMBIE_SPAWN_EGG)
        val name = NBT.getComponents(spawnEgg, { nbt ->
            nbt.getString("CustomTag")
        }).toString()
        if (event.item?.isSimilar(spawnEgg) == true && name == "Tower 1") {
            event.isCancelled = true

            val block = event.clickedBlock ?: return

            val location = block.location

            val world = location.world

            val entity = world.spawnEntity(location, EntityType.ZOMBIE)

            if (entity is LivingEntity) {
                entity.setAI(false)
                entity.isInvulnerable = true
                NBT.modify(entity, { nbt -> nbt.setString("CustomTag", "PlacedTower1") })
            }

            event.item?.amount -= 1
        }
    }
}