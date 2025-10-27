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
    private val RENAMABLE = TowerDefMC.RENAMABLE_MARKER_VALUE

    // Method to create the items for the menu. Each specific menu implements this.
    abstract fun setMenuItems()

    fun open() {
        player.closeInventory()
        setMenuItems() // Populate the inventory
        MenuListener.registerMenu(player, this)
        player.openInventory(inventory)
    }

    protected fun createRenamableItem(
        material: Material,
        defaultName: String,
        defaultLore: List<String>,
        renameLoreMode: Boolean = true
    ): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        val pdc = meta.persistentDataContainer
        pdc.set(TowerDefMC.RENAMABLE_KEY, PersistentDataType.STRING, RENAMABLE)

        val mode = if (renameLoreMode) TowerDefMC.RENAME_MODE_LORE else TowerDefMC.RENAME_MODE_TITLE
        pdc.set(TowerDefMC.RENAMABLE_TARGET_KEY, PersistentDataType.STRING, mode)

        // Read saved title from PDC
        val customTitle = pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)
        val nameComponent = if (customTitle != null) {
            TowerDefMC.MINI_MESSAGE.deserialize(customTitle.replace("§", "&"))
        } else {
            Component.text(defaultName)
        }
        meta.displayName(nameComponent)

        // Read saved lore from PDC
        val customLore = pdc.get(TowerDefMC.LORE_KEY, PersistentDataType.STRING)
        val loreComponents = if (customLore != null) {
            listOf(TowerDefMC.MINI_MESSAGE.deserialize(customLore.replace("§", "&")))
        } else {
            defaultLore.map { Component.text(it) }
        }
        meta.lore(loreComponents)

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

    private fun getItemRenameMode(item: ItemStack?): String? {
        val meta = item?.itemMeta ?: return null
        val pdc = meta.persistentDataContainer

        return pdc.get(TowerDefMC.RENAMABLE_TARGET_KEY, PersistentDataType.STRING)
    }

    fun initiateRename(item: ItemStack, slot: Int, renameMode: String) {
        val player = inventory.viewers.firstOrNull() as? Player ?: return

        player.closeInventory()

        val context = MenuListener.RenameContext(
            itemToRename = item.clone(),
            sourceSlot = slot,
            menuInstance = this,
            renameMode = renameMode
        )

        MenuListener.awaitingRename[player.uniqueId] = context

        // Adjust prompt based on mode
        val promptText = when (renameMode) {
            TowerDefMC.RENAME_MODE_TITLE -> "new item name"
            TowerDefMC.RENAME_MODE_LORE -> "new item description"
            else -> "new text"
        }

        player.sendMessage(Component.text("----------------------------------").color(TextColor.color(0x00FFFF)))
        player.sendMessage(Component.text("Enter the $promptText. Type 'cancel' to stop.").color(TextColor.color(0x00FF00)))
        player.sendMessage(Component.text("The first line of your message will be the $promptText.").color(TextColor.color(0x808080)))
        player.sendMessage(Component.text("----------------------------------").color(TextColor.color(0x00FFFF)))
    }

    fun handleGlobalClick(event: InventoryClickEvent) {
        val clickedItem = event.currentItem ?: return
        val slot = event.slot

        val renameMode = getItemRenameMode(clickedItem)

        if (renameMode != null) {
            event.isCancelled = true
            initiateRename(clickedItem, slot, renameMode)
            return
        }
        handleClick(event)
    }

    // Method to handle a specific slot click. Each specific menu implements this.
    abstract fun handleClick(event: InventoryClickEvent)

    // Helper to allow Adventure Component
    private fun String.toComponent(): Component = Component.text(this)
}