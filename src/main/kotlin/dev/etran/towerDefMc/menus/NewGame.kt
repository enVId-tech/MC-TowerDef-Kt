package dev.etran.towerDefMc.menus

import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class NewGame(player: Player) : CustomMenu(player, 27, "Tower Defense - New Game") {
    // Items in each slot
    override fun setMenuItems() {
        inventory.setItem(
            10,
            createRenamableItem(Material.REDSTONE_BLOCK, "Max Health", listOf("The default maximum game health."))
        )
        inventory.setItem(
            13,
            createRenamableItem(Material.EMERALD, "Default Starting Cash", listOf("The default starting cash."))
        )
    }

    // Slots
    override fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        event.isCancelled = true

        when (event.slot) {

        }
    }
}