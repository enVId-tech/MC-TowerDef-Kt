package dev.etran.towerDefMc.menus.waves

import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ModifyWave(
    player: Player,
    val waveNum: Int
) : CustomMenu(player, 54, "Tower Defense - New Wave") {
    override fun setMenuItems() {
        TODO("Not yet implemented")
        /**
         * -- Top 3 rows
         * Current wave order
         * -- Middle row (gray stained glass)
         * -- Bottom 2 rows
         * Add wait item
         * Remove wait item
         * Add enemies item
         * Remove enemies item
         * Min time item (default 0)
         * Max time item (cannot be less than combined wait times)
         * Wave health item (will use default health if not present)
         * Cash given item
         */
    }

    override fun handleClick(event: InventoryClickEvent) {
        TODO("Not yet implemented")
    }
}