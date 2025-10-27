package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.listeners.MenuListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class CustomMenu(val player: Player, val size: Int, val title: String) {
    val inventory: Inventory = Bukkit.createInventory(player, size, title.toComponent())
    private val RENAMABLE = "ITEM_IS_RENAMABLE"

    // Method to create the items for the menu. Each specific menu implements this.
    abstract fun setMenuItems()

    fun open() {
        setMenuItems() // Populate the inventory
        player.openInventory(inventory)
    }

    protected fun createRenamableItem(material: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        val pdc = meta.persistentDataContainer
        pdc.set(TowerDefMC.RENAMABLE_KEY, PersistentDataType.STRING, RENAMABLE)

        meta.displayName(Component.text(name))
        meta.lore(lore.map { Component.text(it) })
        item.itemMeta = meta
        return item
    }

    protected fun createMenuItem(material: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.displayName(Component.text(name))
        meta.lore(lore.map { Component.text(it) })
        item.itemMeta = meta

        return item
    }

    private fun isItemRenamable(item: ItemStack?): Boolean {
        val meta = item?.itemMeta ?: return false
        val pdc = meta.persistentDataContainer

        // Check if the item has the "renamable" item marker
        return pdc.has(TowerDefMC.RENAMABLE_KEY, PersistentDataType.STRING) &&
                pdc.get(TowerDefMC.RENAMABLE_KEY, PersistentDataType.STRING).equals(RENAMABLE)
    }

    fun initiateRename(item: ItemStack, slot: Int) {
        val player = inventory.viewers.firstOrNull() as? Player ?: return

        player.closeInventory()

        val context = MenuListener.RenameContext(
            itemToRename = item.clone(),
            sourceSlot = slot,
            menuInstance = this
        )

        MenuListener.awaitingRename[player.uniqueId] = context

        // Send prompt messages using Adventure components
        player.sendMessage(Component.text("----------------------------------").color(TextColor.color(0x00FFFF)))
        player.sendMessage(Component.text("Enter the new name for your item. Type 'cancel' to stop.").color(TextColor.color(0x00FF00)))
        player.sendMessage(Component.text("The first line of your message will be the new item name.").color(TextColor.color(0x808080)))
        player.sendMessage(Component.text("----------------------------------").color(TextColor.color(0x00FFFF)))
    }

    fun handleGlobalClick(event: InventoryClickEvent) {
        val clickedItem = event.currentItem ?: return
        val slot = event.slot

        if (isItemRenamable(clickedItem)) {
            event.isCancelled = true
            initiateRename(clickedItem, slot)
            return
        }
        handleClick(event)
    }

    // Method to handle a specific slot click. Each specific menu implements this.
    abstract fun handleClick(event: InventoryClickEvent)

    // Helper to allow Adventure Component
    private fun String.toComponent(): Component = Component.text(this)
}