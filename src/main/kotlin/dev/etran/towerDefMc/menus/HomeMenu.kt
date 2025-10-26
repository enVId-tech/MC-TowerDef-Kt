package dev.etran.towerDefMc.menus

import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class HomeMenu(player: Player) : CustomMenu(player, 27, "Tower Defense - Home Menu") {
    // Items in each slot
    override fun setMenuItems() {
        inventory.setItem(
            10,
            createMenuItem(Material.BOW, "Create a new game", listOf("Click to create the structure for a new game"))
        )
        inventory.setItem(
            13,
            createMenuItem(Material.SHEARS, "Modify an existing game", listOf("Click to modify an existing game"))
        )
        inventory.setItem(
            16,
            createMenuItem(Material.BARRIER, "Delete an existing game", listOf("Click to remove an existing game"))
        )
    }

    // Slots
    override fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        event.isCancelled = true

        when (event.slot) {
            10 -> {
                player.performCommand("td setup game")
                player.closeInventory()
                player.openInventory(player.inventory) // TODO: Replace placeholder inventory with actual inventory
            }
            13 -> {
                player.performCommand("td modify game")
                player.closeInventory()
                player.openInventory(player.inventory)
            }
            16 -> {
                player.performCommand("td delete game")
            }
        }
    }

    private fun createMenuItem(material: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(Component.text(name))
        meta.lore(lore.map { Component.text(it) })
        item.itemMeta = meta
        return item
    }
}