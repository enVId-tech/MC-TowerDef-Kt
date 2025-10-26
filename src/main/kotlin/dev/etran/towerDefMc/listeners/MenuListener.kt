package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.UUID

object MenuListener : Listener {
    private val openMenus = mutableMapOf<UUID, CustomMenu>()

    fun registerMenu(player: Player, menu: CustomMenu) {
        openMenus[player.uniqueId] = menu
    }

    fun unregisterMenu(player: Player) {
        openMenus.remove(player.uniqueId)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val currentMenu = openMenus[player.uniqueId] ?: return

        // Prevent user from moving inventory items
        if (event.inventory == currentMenu.inventory) {
            event.isCancelled = true

            if (event.clickedInventory == currentMenu.inventory) {
                currentMenu.handleClick(event)
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        unregisterMenu(event.player as Player)
    }
}