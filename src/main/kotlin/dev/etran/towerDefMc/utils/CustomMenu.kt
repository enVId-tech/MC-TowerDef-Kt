package dev.etran.towerDefMc.utils

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

abstract class CustomMenu(val player: Player, val size: Int, val title: String) {
    val inventory: Inventory = Bukkit.createInventory(player, size, title.toComponent())

    // Method to create the items for the menu. Each specific menu implements this.
    abstract fun setMenuItems()

    // Method to handle a specific slot click. Each specific menu implements this.
    abstract fun handleClick(event: InventoryClickEvent)

    fun open() {
        setMenuItems() // Populate the inventory
        player.openInventory(inventory)
    }

    // Helper to allow Adventure Component
    private fun String.toComponent(): Component = Component.text(this)
}