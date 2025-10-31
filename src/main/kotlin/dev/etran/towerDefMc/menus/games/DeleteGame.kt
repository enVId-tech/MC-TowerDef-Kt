package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.managers.GameManager
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
            for (i in 0 + (currentMenuOpen * 36)..35 + (currentMenuOpen * 36)) {
                inventory.setItem(
                    i, createMenuItem(
                        Material.BOW, gamesList.values.elementAt(i).config.name, listOf()
                    )
                )
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
            for (i in 0..53) {
                inventory.setItem(
                    i, createMenuItem(
                        Material.BOW, "", listOf()
                    )
                )
            }
        }
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val slot = event.slot

        when (slot) {
            45 -> {
                if (currentMenuOpen > 0) {
                    currentMenuOpen--
                }
            }

            53 -> {
                if (currentMenuOpen < (gamesList.values.size / 36)) {
                    currentMenuOpen++
                }
            }
        }

        if (gamesList.values.size < 54) {
            val gameAtSlot = gamesList.values.elementAt(slot)
            GameRegistry.removeGame(gameAtSlot)
            player.sendMessage("Game")
        } else {
            val gameIndex = slot + (currentMenuOpen * 36)
            val gameAtSlot = gamesList.values.elementAt(gameIndex)
            GameRegistry.removeGame(gameAtSlot)
        }
    }
}