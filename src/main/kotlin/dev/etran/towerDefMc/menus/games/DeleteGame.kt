package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.managers.GameManager
import dev.etran.towerDefMc.menus.Home
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class DeleteGame(player: Player) : CustomMenu(player, 54, "Tower Defense - Delete Game") {
    var currentMenuOpen = 0
    lateinit var gamesList: Map<Int, GameManager>

    override fun setMenuItems() {
        gamesList = GameRegistry.allGames

        if (gamesList.values.size >= 54) {
            // Multipage mode for 54+ games
            for (i in 0 + (currentMenuOpen * 36)..35 + (currentMenuOpen * 36)) {
                if (i < gamesList.values.size) {
                    inventory.setItem(
                        i - (currentMenuOpen * 36), createMenuItem(
                            Material.BOW, gamesList.values.elementAt(i).config.name, listOf()
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

            if (currentMenuOpen < (gamesList.values.size / 36)) {
                inventory.setItem(
                    53, createMenuItem(
                        Material.GREEN_CONCRETE, "Next Page"
                    )
                )
            }
        } else {
            // Single page mode - only fill slots for actual games
            gamesList.values.forEachIndexed { index, game ->
                if (index < 45) { // Only use first 45 slots (rows 1-5)
                    inventory.setItem(
                        index, createMenuItem(
                            Material.BOW, game.config.name, listOf("§cClick to delete this game")
                        )
                    )
                }
            }

            // Add back button at bottom
            inventory.setItem(
                49, createMenuItem(
                    Material.BARRIER, "§cBack", listOf("Return to home menu")
                )
            )
        }
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val slot = event.slot

        when (slot) {
            45 -> {
                if (currentMenuOpen > 0) {
                    currentMenuOpen--
                    setMenuItems()
                }
            }

            49 -> {
                // Back button
                player.closeInventory()
                val homeMenu = Home(player)
                homeMenu.open()
            }

            53 -> {
                if (currentMenuOpen < (gamesList.values.size / 36)) {
                    currentMenuOpen++
                    setMenuItems()
                }
            }
        }

        // Handle game deletion
        if (gamesList.values.size >= 54) {
            // Multi-page mode
            val gameIndex = slot + (currentMenuOpen * 36)
            if (gameIndex < gamesList.values.size && slot in 0..35) {
                val gameAtSlot = gamesList.values.elementAt(gameIndex)
                GameRegistry.removeGame(gameAtSlot)
                player.sendMessage("§aGame '${gameAtSlot.config.name}' deleted!")
                // Refresh menu
                open()
            }
        } else {
            // Single page mode
            if (slot < gamesList.values.size && slot in 0..44) {
                val gameAtSlot = gamesList.values.elementAt(slot)
                GameRegistry.removeGame(gameAtSlot)
                player.sendMessage("§aGame '${gameAtSlot.config.name}' deleted!")
                // Refresh menu
                open()
            }
        }
    }
}