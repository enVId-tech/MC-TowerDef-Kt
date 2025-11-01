package dev.etran.towerDefMc.menus.enemies

import dev.etran.towerDefMc.registries.EnemyRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag

class DeleteEnemyMenu(player: Player) : CustomMenu(player, 54, "Tower Defense - Delete Enemies") {

    private var currentPage: Int = 0
    private val enemiesPerPage = 36

    override fun setMenuItems() {
        inventory.clear()

        val allEnemies = EnemyRegistry.getAllEnemies()

        // Display enemies for current page (slots 0-35)
        val startIndex = currentPage * enemiesPerPage
        val endIndex = minOf(startIndex + enemiesPerPage, allEnemies.size)

        for (i in startIndex until endIndex) {
            val slotIndex = i - startIndex
            val enemy = allEnemies[i]

            val lore = mutableListOf<String>()
            lore.add("§7${enemy.displayName}")
            lore.addAll(enemy.description.map { "§7$it" })
            lore.add("")
            lore.add("§7Health: §e${enemy.health}")
            lore.add("§7Speed: §e${enemy.speed}")
            lore.add("§7Damage: §e${enemy.damage}")
            lore.add("")
            lore.add("§c§lClick to DELETE this enemy")
            lore.add("§7This action cannot be undone!")

            val item = createMenuItem(enemy.icon, "§c${enemy.displayName}", lore)

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
        val totalPages = (allEnemies.size + enemiesPerPage - 1) / enemiesPerPage

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

        // Show empty message if no enemies
        if (allEnemies.isEmpty()) {
            inventory.setItem(
                22, createMenuItem(
                    Material.BARRIER, "§cNo Enemies Available", listOf("§7Create enemies first to delete them")
                )
            )
        }
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val slot = event.slot

        when (slot) {
            in 0..35 -> handleEnemyClick(slot)
            45 -> handleBackPage()
            49 -> handleClose()
            53 -> handleNextPage()
        }
    }

    private fun handleEnemyClick(slot: Int) {
        val allEnemies = EnemyRegistry.getAllEnemies()
        val enemyIndex = currentPage * enemiesPerPage + slot
        if (enemyIndex >= allEnemies.size) return

        val enemy = allEnemies[enemyIndex]

        // Open confirmation menu
        player.closeInventory()
        ConfirmDeleteMenu(player, enemy.id, enemy.displayName) {
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
        val allEnemies = EnemyRegistry.getAllEnemies()
        val totalPages = (allEnemies.size + enemiesPerPage - 1) / enemiesPerPage
        if (currentPage < totalPages - 1) {
            currentPage++
            setMenuItems()
        }
    }

    private fun handleClose() {
        player.closeInventory()
        player.sendMessage("§7Closed delete menu")
    }

    // Confirmation menu for deleting an enemy
    private class ConfirmDeleteMenu(
        player: Player,
        private val enemyId: String,
        private val enemyName: String,
        private val onComplete: () -> Unit
    ) : CustomMenu(player, 27, "Confirm Delete - $enemyName") {

        override fun setMenuItems() {
            inventory.clear()

            // Fill with red glass panes for danger
            for (i in 0..26) {
                inventory.setItem(i, createMenuItem(Material.RED_STAINED_GLASS_PANE, " ", listOf()))
            }

            // Warning message
            inventory.setItem(
                13, createMenuItem(
                    Material.BARRIER, "§c§lDELETE $enemyName?", listOf(
                        "§7This will permanently delete",
                        "§7this enemy type from the game.",
                        "§c§lThis action cannot be undone!"
                    )
                )
            )

            // Confirm button
            inventory.setItem(
                11, createMenuItem(
                    Material.LIME_DYE, "§a§lCONFIRM DELETE", listOf(
                        "§aYes, delete this enemy"
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
            val success = EnemyRegistry.deleteEnemy(enemyId)
            player.closeInventory()

            if (success) {
                player.sendMessage("§a✓ Successfully deleted enemy: $enemyName")
            } else {
                player.sendMessage("§c✗ Failed to delete enemy: $enemyName")
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
