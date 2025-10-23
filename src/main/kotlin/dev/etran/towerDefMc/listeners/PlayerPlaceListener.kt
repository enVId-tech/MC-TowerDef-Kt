package dev.etran.towerDefMc.listeners

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.factories.CheckpointFactory
import dev.etran.towerDefMc.factories.TowerFactory
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
        val gameElementSpawn = event.item ?: return

        val name: String = NBT.get<String>(gameElementSpawn, { nbt -> nbt.getString("tdef_item_name") })

        // Run functions specific to their unique identifiers
        when (name) {
            "Tower 1" -> TowerFactory.towerPlace(event)
            "Checkpoint" -> CheckpointFactory.checkPointPlace(event)
        }
    }
}