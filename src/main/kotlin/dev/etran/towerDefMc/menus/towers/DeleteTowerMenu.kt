package dev.etran.towerDefMc.menus.towers

import dev.etran.towerDefMc.registries.TowerRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag

class DeleteTowerMenu(player: Player) : CustomMenu(player, 54, "Tower Defense - Delete Towers") {

    private var currentPage: Int = 0
    private val towersPerPage = 36

    override fun setMenuItems() {
        inventory.clear()

        val allTowers = TowerRegistry.getAllTowers().values.toList()

        // Display towers for current page (slots 0-35)
        val startIndex = currentPage * towersPerPage
        val endIndex = minOf(startIndex + towersPerPage, allTowers.size)

        for (i in startIndex until endIndex) {
            val slotIndex = i - startIndex
            val tower = allTowers[i]

            val lore = mutableListOf<String>()
            lore.add("§7${tower.displayName}")
            lore.addAll(tower.description.map { "§7$it" })
            lore.add("")
            lore.add("§7Range: §e${tower.range}")
            lore.add("§7Damage: §e${tower.damage}")
            lore.add("§7Attack Speed: §e${tower.attackSpeed}")
            lore.add("")
            lore.add("§c§lClick to DELETE this tower")
            lore.add("§7This action cannot be undone!")

            val item = createMenuItem(tower.icon, "§c${tower.displayName}", lore)

            // Add red enchantment glint for danger
            val meta = item.itemMeta
            meta.addEnchant(Enchantment.MENDING, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            item.itemMeta = meta

            inventory.setItem(slotIndex, item)
        }

        // Separator row (slots 36-44)
        for (i in 36..44) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ", listOf()))
        }

        // Bottom row controls (slots 45-53)
        val totalPages = (allTowers.size + towersPerPage - 1) / towersPerPage

        // Left side - Back page button
        if (currentPage > 0) {
            inventory.setItem(
                45, createMenuItem(
                    Material.RED_CONCRETE, "§cBack Page", listOf("Go to page $currentPage")
                )
            )
        }

        // Close button
        inventory.setItem(
            49, createMenuItem(
                Material.BARRIER, "§cClose", listOf("Exit delete menu")
            )
        )

        // Right side - Next page button
        if (currentPage < totalPages - 1) {
            inventory.setItem(
                53, createMenuItem(
                    Material.GREEN_CONCRETE, "§aNext Page", listOf("Go to page ${currentPage + 2}")
                )
            )
        }

        // Show empty message if no towers
        if (allTowers.isEmpty()) {
            inventory.setItem(
                22, createMenuItem(
                    Material.BARRIER, "§cNo Towers Available", listOf("§7Create towers first to delete them")
                )
            )
        }
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val slot = event.slot

        when (slot) {
            in 0..35 -> handleTowerClick(slot)
            45 -> handleBackPage()
            49 -> handleClose()
            53 -> handleNextPage()
        }
    }

    private fun handleTowerClick(slot: Int) {
        val allTowers = TowerRegistry.getAllTowers().values.toList()
        val towerIndex = currentPage * towersPerPage + slot
        if (towerIndex >= allTowers.size) return

        val tower = allTowers[towerIndex]

        // Open confirmation menu
        player.closeInventory()
        ConfirmDeleteMenu(player, tower.id, tower.displayName) {
            // Refresh this menu after deletion
            setMenuItems()
            this.open()
        }.open()
    }

    private fun handleBackPage() {
        if (currentPage > 0) {
            currentPage--
            setMenuItems()
        }
    }

    private fun handleNextPage() {
        val allTowers = TowerRegistry.getAllTowers().values.toList()
        val totalPages = (allTowers.size + towersPerPage - 1) / towersPerPage
        if (currentPage < totalPages - 1) {
            currentPage++
            setMenuItems()
        }
    }

    private fun handleClose() {
        player.closeInventory()
        player.sendMessage("§7Closed delete menu")
    }

    // Confirmation menu for deleting a tower
    private class ConfirmDeleteMenu(
        player: Player,
        private val towerId: String,
        private val towerName: String,
        private val onComplete: () -> Unit
    ) : CustomMenu(player, 27, "Confirm Delete - $towerName") {

        override fun setMenuItems() {
            inventory.clear()

            // Fill with red glass panes for danger
            for (i in 0..26) {
                inventory.setItem(i, createMenuItem(Material.RED_STAINED_GLASS_PANE, " ", listOf()))
            }

            // Warning message
            inventory.setItem(
                13, createMenuItem(
                    Material.BARRIER, "§c§lDELETE $towerName?", listOf(
                        "§7This will permanently delete",
                        "§7this tower type from the game.",
                        "§c§lThis action cannot be undone!"
                    )
                )
            )

            // Confirm button
            inventory.setItem(
                11, createMenuItem(
                    Material.LIME_DYE, "§a§lCONFIRM DELETE", listOf(
                        "§aYes, delete this tower"
                    )
                )
            )

            // Cancel button
            inventory.setItem(
                15, createMenuItem(
                    Material.GRAY_DYE, "§7Cancel", listOf(
                        "§7No, go back"
                    )
                )
            )
        }

        override fun handleClick(event: InventoryClickEvent) {
            event.isCancelled = true

            when (event.slot) {
                11 -> handleConfirm()
                15 -> handleCancel()
            }
        }

        private fun handleConfirm() {
            val success = TowerRegistry.deleteTower(towerId)
            player.closeInventory()

            if (success) {
                player.sendMessage("§a✓ Successfully deleted tower: $towerName")
            } else {
                player.sendMessage("§c✗ Failed to delete tower: $towerName")
            }

            onComplete()
        }

        private fun handleCancel() {
            player.closeInventory()
            player.sendMessage("§7Cancelled deletion")
            onComplete()
        }
    }
}
