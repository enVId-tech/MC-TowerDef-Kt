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

abstract class CustomMenu(val player: Player, val size: Int, title: String) {
    val inventory: Inventory = Bukkit.createInventory(player, size, title.toComponent())
    private var isInitialized = false

    companion object {
        const val RENAMABLE = TowerDefMC.RENAMABLE_MARKER_VALUE
    }

    // Method to create the items for the menu. Each specific menu implements this.
    abstract fun setMenuItems()


    fun open() {
        player.closeInventory()

        if (!isInitialized) {
            setMenuItems() // Populate the inventory
            isInitialized = true
        }

        MenuListener.registerMenu(player, this)
        player.openInventory(inventory)
    }

    protected fun createRenamableItem(
        material: Material,
        defaultName: String,
        defaultLore: List<String>,
        placeholderCustomVal: String,
    ): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        val pdc = meta.persistentDataContainer

        val usesValuePlaceholder = defaultLore.any { it.contains("{VALUE}") || defaultName.contains("{VALUE}") }

        if (usesValuePlaceholder) {
            pdc.set(TowerDefMC.RENAMABLE_KEY, PersistentDataType.STRING, RENAMABLE)

            val loreTemplateString = defaultLore.joinToString("|||")
            pdc.set(TowerDefMC.LORE_TEMPLATE_KEY, PersistentDataType.STRING, loreTemplateString)
        }

        val customValue = pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)

        var dynamicValueSource = customValue

        if (dynamicValueSource == null) {
            dynamicValueSource = placeholderCustomVal
            pdc.set(TowerDefMC.TITLE_KEY, PersistentDataType.STRING, dynamicValueSource)
        }

        val titleTemplate = customValue ?: defaultName

        val processedTitle =
            titleTemplate.replace("{VALUE}", dynamicValueSource).replace("\${VALUE}", dynamicValueSource)

        val nameComponent = TowerDefMC.MINI_MESSAGE.deserialize(processedTitle.replace("§", "&"))
        meta.displayName(nameComponent)

        val finalLoreComponents = mutableListOf<Component>()

        for (rawLine in defaultLore) {
            val processedLine = rawLine.replace("{VALUE}", dynamicValueSource).replace("\${VALUE}", dynamicValueSource)

            finalLoreComponents.add(TowerDefMC.MINI_MESSAGE.deserialize(processedLine.replace("§", "&")))
        }

        meta.lore(finalLoreComponents)
        item.itemMeta = meta
        return item
    }

    protected fun createMenuItem(material: Material, name: String, lore: List<String>? = listOf("")): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.displayName(Component.text(name))
        meta.lore(lore?.map { Component.text(it) })
        item.itemMeta = meta

        return item
    }

    private fun isItemRenamable(item: ItemStack?): Boolean {
        val meta = item?.itemMeta ?: return false
        val pdc = meta.persistentDataContainer

        // Check if the item has the "renamable" item marker
        return pdc.has(TowerDefMC.RENAMABLE_KEY, PersistentDataType.STRING) && pdc.get(
            TowerDefMC.RENAMABLE_KEY, PersistentDataType.STRING
        ).equals(RENAMABLE)
    }

    fun initiateRename(item: ItemStack, slot: Int) {
        val player = inventory.viewers.firstOrNull() as? Player ?: return

        player.closeInventory()

        val context = MenuListener.RenameContext(
            itemToRename = item.clone(), sourceSlot = slot, menuInstance = this
        )

        MenuListener.awaitingRename[player.uniqueId] = context

        // Prompt is always for the new value
        val promptText = "new value"

        player.sendMessage(Component.text("----------------------------------").color(TextColor.color(0x00FFFF)))
        player.sendMessage(
            Component.text("Enter the $promptText. Type 'cancel' to stop.").color(TextColor.color(0x00FF00))
        )
        player.sendMessage(
            Component.text("The first line of your message will be the $promptText.").color(TextColor.color(0x808080))
        )
        player.sendMessage(Component.text("----------------------------------").color(TextColor.color(0x00FFFF)))
    }

    fun handleGlobalClick(event: InventoryClickEvent) {
        val clickedItem = event.currentItem ?: return
        val slot = event.slot

        if (isItemRenamable(clickedItem) && event.isLeftClick) {
            event.isCancelled = true
            initiateRename(clickedItem, slot) // Call without mode
            return
        }
        handleClick(event)
    }

    // Method to handle a specific slot click. Each specific menu implements this.
    abstract fun handleClick(event: InventoryClickEvent)

    // Method called after an item is renamed via chat input
    open fun onItemRenamed(slot: Int, newValue: String) {
        // Default implementation does nothing
        // Subclasses can override to handle auto-save or other logic
    }

    // Helper to allow Adventure Component
    private fun String.toComponent(): Component = Component.text(this)
}