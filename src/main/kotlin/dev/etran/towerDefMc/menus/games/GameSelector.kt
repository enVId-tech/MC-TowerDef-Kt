package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.managers.GameManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class GameSelector(player: Player) : CustomMenu(player, 54, "Tower Defense - Modify Games") {
    var currentMenuOpen = 0
    lateinit var gamesList: List<GameManager>

    // Items in each slot
    override fun setMenuItems() {
        gamesList = GameRegistry.allGames

        if (gamesList.size >= 54) {
            for (i in 0 + (currentMenuOpen * 36)..35 + (currentMenuOpen * 36)) {
                inventory.setItem(
                    i,
                    createMenuItem(
                        Material.BOW,
                        "",
                        listOf()
                    )
                )
            }

            for (i in 36..44) {
                inventory.setItem(
                    i,
                    createMenuItem(
                        Material.GRAY_STAINED_GLASS_PANE,
                        ""
                    )
                )
            }

            inventory.setItem(
                45,
                createMenuItem(
                    Material.RED_CONCRETE,
                    "Back Page"
                )
            )

            inventory.setItem(
                53,
                createMenuItem(
                    Material.GREEN_CONCRETE,
                    "Next Page"
                )
            )
        } else {
            for (i in 0..53) {
                inventory.setItem(
                    i,
                    createMenuItem(
                        Material.BOW,
                        "",
                        listOf()
                    )
                )
            }
        }
    }

    // Slots
    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true
    }
}