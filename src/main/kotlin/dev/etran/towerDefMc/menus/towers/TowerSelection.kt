package dev.etran.towerDefMc.menus.towers

import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.menus.games.ModifyGame
import dev.etran.towerDefMc.registries.TowerRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class TowerSelection(
    player: Player,
    private val gameConfig: GameSaveConfig,
    private val gameId: Int
) : CustomMenu(player, 54, "Tower Defense - Tower Limits") {

    private var currentPage: Int = 0
    private val towersPerPage = 36 // 4 rows of 9

    override fun setMenuItems() {
        inventory.clear()

        val allTowers = TowerRegistry.getAllTowers()
        val towersList = allTowers.values.toList()

        // Display towers for current page (slots 0-35, first 4 rows)
        val startIndex = currentPage * towersPerPage
        val endIndex = minOf(startIndex + towersPerPage, towersList.size)

        for (i in startIndex until endIndex) {
            val slotIndex = i - startIndex
            val tower = towersList[i]

            // Get the current limit for this tower from gameConfig
            val towerLimits = gameConfig.allowedTowers.associate { entry: String ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    parts[0] to (parts[1].toIntOrNull() ?: -1)
                } else {
                    entry to -1
                }
            }
            val currentLimit = towerLimits[tower.id] ?: -1
            val limitDisplay = if (currentLimit == -1) "No Limit" else currentLimit.toString()

            val lore = mutableListOf<String>()
            lore.add("§7${tower.displayName}")
            lore.addAll(tower.description.map { "§7$it" })
            lore.add("")
            lore.add("§7Range: §e${tower.range}")
            lore.add("§7Damage: §e${tower.damage}")
            lore.add("§7Attack Speed: §e${tower.attackSpeed}")
            lore.add("")
            lore.add("§7Current Limit: §a$limitDisplay")
            lore.add("")
            lore.add("§eClick to set tower limit")
            lore.add("§7Set to -1 for no limit")

            inventory.setItem(
                slotIndex,
                createRenamableItem(
                    tower.icon,
                    "§a${tower.displayName}: {VALUE}",
                    lore,
                    limitDisplay
                )
            )
        }

        // Separator row (slots 36-44, 2nd to last line)
        for (i in 36..44) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ", listOf()))
        }

        // Last line controls (slots 45-53)
        val totalPages = (towersList.size + towersPerPage - 1) / towersPerPage

        // Left side - Back page button
        if (currentPage > 0) {
            inventory.setItem(
                45,
                createMenuItem(
                    Material.RED_CONCRETE,
                    "§cBack Page",
                    listOf("Go to page $currentPage")
                )
            )
        }

        // Refresh button (slot 46)
        inventory.setItem(
            46,
            createMenuItem(
                Material.LIME_DYE,
                "§aRefresh",
                listOf("§7Reload the towers list", "§7Useful after making changes")
            )
        )

        // Close/Done button
        inventory.setItem(
            47,
            createMenuItem(
                Material.BARRIER,
                "§cDone",
                listOf("Close towers menu and save")
            )
        )

        // Center - Clear all limits button
        inventory.setItem(
            49,
            createMenuItem(
                Material.TNT,
                "§cClear All Limits",
                listOf(
                    "§7Remove all tower limits",
                    "§7All towers will have no limit"
                )
            )
        )

        // Right side - Next page button
        if (currentPage < totalPages - 1) {
            inventory.setItem(
                53,
                createMenuItem(
                    Material.GREEN_CONCRETE,
                    "§aNext Page",
                    listOf("Go to page ${currentPage + 2}")
                )
            )
        }
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val slot = event.slot

        when {
            slot in 0..35 -> handleTowerClick(slot, event)
            slot == 45 -> handleBackPage()
            slot == 46 -> handleRefresh()
            slot == 47 -> handleDone()
            slot == 49 -> handleClearAllLimits()
            slot == 53 -> handleNextPage()
        }
    }

    private fun handleTowerClick(slot: Int, event: InventoryClickEvent) {
        val towersList = TowerRegistry.getAllTowers().values.toList()
        val towerIndex = currentPage * towersPerPage + slot
        if (towerIndex >= towersList.size) return

        val tower = towersList[towerIndex]
        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        val limitStr = pdc.get(
            dev.etran.towerDefMc.TowerDefMC.TITLE_KEY,
            org.bukkit.persistence.PersistentDataType.STRING
        ) ?: "-1"

        val limit = if (limitStr == "No Limit") -1 else limitStr.toIntOrNull() ?: -1

        // Update the allowedTowers list
        val updatedTowers = gameConfig.allowedTowers.toMutableList()

        // Remove existing entry for this tower if it exists
        updatedTowers.removeIf { it.startsWith("${tower.id}:") || it == tower.id }

        // Add the new limit
        updatedTowers.add("${tower.id}:$limit")

        gameConfig.allowedTowers = updatedTowers

        player.sendMessage("§aSet ${tower.displayName} limit to ${if (limit == -1) "No Limit" else limit}")

        // Refresh the menu to show updated values
        setMenuItems()
    }

    private fun handleBackPage() {
        if (currentPage > 0) {
            currentPage--
            setMenuItems()
        }
    }

    private fun handleNextPage() {
        val towersList = TowerRegistry.getAllTowers().values.toList()
        val totalPages = (towersList.size + towersPerPage - 1) / towersPerPage
        if (currentPage < totalPages - 1) {
            currentPage++
            setMenuItems()
        }
    }

    private fun handleDone() {
        // Save the game configuration
        dev.etran.towerDefMc.registries.GameRegistry.saveGameConfig(gameId, gameConfig)

        val prevMenu = ModifyGame(player, gameId)
        prevMenu.open()
        player.sendMessage("§aTower limits saved")
    }

    private fun handleRefresh() {
        // Simply reset the menu items to refresh the display
        setMenuItems()
        player.sendMessage("§aTowers list refreshed")
    }

    private fun handleClearAllLimits() {
        val allTowers = TowerRegistry.getAllTowers()
        val updatedTowers = allTowers.keys.map { "$it:-1" }
        gameConfig.allowedTowers = updatedTowers

        setMenuItems()
        player.sendMessage("§aAll tower limits cleared")
    }
}
