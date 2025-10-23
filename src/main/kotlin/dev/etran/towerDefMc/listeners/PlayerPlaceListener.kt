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
        val player = event.player
        val spawnEgg = event.item ?: return
        if (spawnEgg.type != Material.ZOMBIE_SPAWN_EGG) return

        val name: String = NBT.get<String>(spawnEgg, { nbt -> nbt.getString("tower_id")})

        if (name.equals("Tower 1")) {
            event.isCancelled = true

            val block = event.clickedBlock ?: return
            val location = block.location.add(0.5, 1.0, 0.5)
            val world = location.world
            val entity = world.spawnEntity(location, EntityType.ZOMBIE)

            if (entity is LivingEntity) {
                entity.setAI(false)
                entity.isInvulnerable = true
                NBT.modify(entity, { nbt ->
                    nbt.setString("CustomTag", "PlacedTower1")
                })
            }


            val itemHeld = player.inventory.itemInMainHand

            itemHeld.amount -= 1
            player.inventory.setItemInMainHand(itemHeld)
        }
    }
}