package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class DeleteGame(player: Player) : CustomMenu(player, 0, "Tower Defense - Delete Game") {
    override fun setMenuItems() {
        TODO("Not yet implemented")
        /**
         * First 4 rows, games
         * 2nd to last row - light gray stained glass
         * last row, left - cancel, right row, next and back pages
         */
    }

    override fun handleClick(event: InventoryClickEvent) {
        TODO("Not yet implemented")
    }
}