package dev.etran.towerDefMc.menus.waves

import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class EnemiesSelection(
    player: Player
) : CustomMenu (player, 54, "Tower Defense - Add Enemies") {
    override fun setMenuItems() {
        TODO("Not yet implemented")
        /**
         * Add all available enemies
         * 2nd to last row - stained glass panes
         * ---- last row ----
         * Next Page and Back page if exceeds 36 items on right
         * On left, enemy frequency, insert step, cancel button
         */
    }

    override fun handleClick(event: InventoryClickEvent) {
        TODO("Not yet implemented")
        /**
         * Implement number selector when user clicks on any enemy
         */
    }
}