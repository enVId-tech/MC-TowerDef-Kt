package dev.etran.towerDefMc.listeners

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.utils.getHighlightedBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent

class PlayerMouseMoveListener : Listener {
    @EventHandler
    fun onPlayerMouseMoveEvent(event: PlayerItemHeldEvent) {
        val player = event.player

        val selectedBlock = getHighlightedBlock(player)

        if (selectedBlock != null) return

        val mainHandItem = player.inventory.itemInMainHand

        val mainHandNBT = NBT.get<String>(mainHandItem, { nbt ->
            nbt.getString("tdef_item_name")
        })

        if (mainHandNBT.isNotEmpty()) {

        }
    }
}