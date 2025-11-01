package dev.etran.towerDefMc.menus.generators

import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class MaterialSelectorMenu(
    player: Player,
    private val currentMaterial: Material,
    private val onMaterialSelected: (Material) -> Unit
) : CustomMenu(player, 54, "Select Item Material") {

    private var searchPage = 0
    private val materialsPerPage = 45

    // Get all materials that are items (not blocks that can't be held, technical items, etc.)
    private val availableMaterials = Material.entries
        .filter { it.isItem && !it.isAir }
        .sortedBy { it.name }

    override fun setMenuItems() {
        val startIndex = searchPage * materialsPerPage
        val endIndex = minOf(startIndex + materialsPerPage, availableMaterials.size)

        // Display materials
        for (i in startIndex until endIndex) {
            val material = availableMaterials[i]
            val slot = i - startIndex

            val item = ItemStack(material)
            val meta = item.itemMeta
            meta.displayName(Component.text("§e${material.name}"))

            val lore = mutableListOf<Component>()
            if (material == currentMaterial) {
                lore.add(Component.text("§a§l✔ Currently Selected"))
            }
            lore.add(Component.text("§7Click to select this item"))
            meta.lore(lore)

            item.itemMeta = meta
            inventory.setItem(slot, item)
        }

        // Navigation buttons
        if (searchPage > 0) {
            inventory.setItem(
                45,
                createMenuItem(
                    Material.ARROW,
                    "§e§l◀ Previous Page",
                    listOf("§7Page ${searchPage}")
                )
            )
        }

        if (endIndex < availableMaterials.size) {
            inventory.setItem(
                53,
                createMenuItem(
                    Material.ARROW,
                    "§e§lNext Page ▶",
                    listOf("§7Page ${searchPage + 2}")
                )
            )
        }

        // Current selection display
        val currentItem = ItemStack(currentMaterial)
        val currentMeta = currentItem.itemMeta
        currentMeta.displayName(Component.text("§6§lCurrent: ${currentMaterial.name}"))
        currentMeta.addEnchant(Enchantment.UNBREAKING, 1, true)
        currentMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        currentItem.itemMeta = currentMeta
        inventory.setItem(49, currentItem)

        // Cancel button
        inventory.setItem(
            48,
            createMenuItem(
                Material.BARRIER,
                "§c§lCancel",
                listOf("§7Return without changing")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        val slot = event.slot

        when {
            slot < 45 -> {
                // Material selection
                val materialIndex = searchPage * materialsPerPage + slot
                if (materialIndex < availableMaterials.size) {
                    val selectedMaterial = availableMaterials[materialIndex]
                    player.closeInventory()
                    onMaterialSelected(selectedMaterial)
                }
            }
            slot == 45 && searchPage > 0 -> {
                // Previous page
                searchPage--
                setMenuItems()
            }
            slot == 53 && (searchPage + 1) * materialsPerPage < availableMaterials.size -> {
                // Next page
                searchPage++
                setMenuItems()
            }
            slot == 48 -> {
                // Cancel
                player.closeInventory()
                onMaterialSelected(currentMaterial) // Return current material
            }
        }
    }
}
