package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.managers.GameManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class GameSelector(player: Player) : CustomMenu(player, 54, "Tower Defense - Modify Games") {
    var currentMenuOpen = 0
    lateinit var gamesList: Map<Int, GameManager>

    // Items in each slot
    override fun setMenuItems() {
        gamesList = GameRegistry.allGames

        if (gamesList.values.size >= 54) {
            for (i in 0 + (currentMenuOpen * 36)..35 + (currentMenuOpen * 36)) {
                // Check if the index is valid before accessing
                if (i < gamesList.values.size) {
                    inventory.setItem(
                        i - (currentMenuOpen * 36), createMenuItem(
                            Material.BOW,
                            gamesList.values.elementAt(i).config.name
                        )
                    )
                }
            }

            for (i in 36..44) {
                inventory.setItem(
                    i, createMenuItem(
                        Material.GRAY_STAINED_GLASS_PANE, ""
                    )
                )
            }

            if (currentMenuOpen > 0) {
                inventory.setItem(
                    45, createMenuItem(
                        Material.RED_CONCRETE, "Back Page"
                    )
                )
            }

            // Refresh button (slot 49)
            inventory.setItem(
                49,
                createMenuItem(
                    Material.LIME_DYE,
                    "§aRefresh",
                    listOf("§7Reload the games list", "§7Useful after creating or deleting games")
                )
            )

            if (currentMenuOpen < (gamesList.values.size / 36)) {
                inventory.setItem(
                    53, createMenuItem(
                        Material.GREEN_CONCRETE, "Next Page"
                    )
                )
            }
        } else {
            // For less than 54 games, display them all
            gamesList.values.forEachIndexed { index, game ->
                if (index < 54) {
                    inventory.setItem(
                        index, createMenuItem(
                            Material.BOW,
                            game.config.name
                        )
                    )
                }
            }

            // Refresh button for single page mode (slot 49)
            inventory.setItem(
                49,
                createMenuItem(
                    Material.LIME_DYE,
                    "§aRefresh",
                    listOf("§7Reload the games list", "§7Useful after creating or deleting games")
                )
            )
        }
    }

    // Slots
    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val slot = event.slot

        // Handle special buttons first
        when (slot) {
            45 -> {
                // Back page button
                if (currentMenuOpen > 0) {
                    currentMenuOpen--
                    open() // Refresh the menu
                }
                return
            }
            49 -> {
                // Refresh button
                open() // Refresh the menu
                player.sendMessage("§aGames list refreshed")
                return
            }
            53 -> {
                // Next page button
                if (currentMenuOpen < (gamesList.values.size / 36)) {
                    currentMenuOpen++
                    open() // Refresh the menu
                }
                return
            }
            in 36..44 -> {
                // Gray glass pane separator - do nothing
                return
            }
        }

        // Handle game selection
        if (gamesList.values.size >= 54) {
            // Multi-page mode
            val gameIndex = slot + (currentMenuOpen * 36)
            if (gameIndex < gamesList.values.size) {
                val game = gamesList.values.elementAt(gameIndex)
                openModifyMenu(game)
            }
        } else {
            // Single page mode
            if (slot < gamesList.values.size) {
                val game = gamesList.values.elementAt(slot)
                openModifyMenu(game)
            }
        }
    }

    private fun openModifyMenu(game: GameManager) {
        player.closeInventory()
        val modifyMenu = ModifyGame(player, game.gameId)
        modifyMenu.open()
    }
}