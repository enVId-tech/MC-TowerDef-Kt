package dev.etran.towerDefMc.menus

import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class NewGame(player: Player) : CustomMenu(player, 27, "Tower Defense - New Game") {
    // Items in each slot
    override fun setMenuItems() {
        inventory.setItem(
            10,
            createRenamableItem(
                Material.REDSTONE_BLOCK,
                "Max Health",
                listOf("The default maximum game health."),
                "100"
            )
        )
        inventory.setItem(
            13,
            createRenamableItem(
                Material.EMERALD,
                "Default Starting Cash: \${VALUE}",
                listOf(
                    "The default starting cash.",
                    "Current starting cash: \${VALUE}"
                ),
                "200"
            )
        )
    }

    // Slots
    override fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        event.isCancelled = true

        // The target item we want to rename is placed in slot 13
        val targetItem = inventory.getItem(13) ?: return

        when (event.slot) {

        }
    }
}