package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.factories.GameFactory
import dev.etran.towerDefMc.listeners.MenuListener
import dev.etran.towerDefMc.menus.Home
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class NewGame(player: Player) : CustomMenu(player, 36, "Tower Defense - New Game") {
    // Items in each slot
    override fun setMenuItems() {
        inventory.setItem(
            10,
            createRenamableItem(
                Material.REDSTONE_BLOCK,
                "Max Health: {VALUE}",
                listOf("The default maximum game health."),
                "100"
            )
        )

        inventory.setItem(
            13,
            createRenamableItem(
                Material.EMERALD,
                "Default Starting Cash: \${VALUE}",
                listOf(
                    "The default starting cash.",
                    "Current starting cash: \${VALUE}"
                ),
                "200"
            )
        )

        inventory.setItem(
            16,
            createRenamableItem(
                Material.OAK_SIGN,
                "Game Name: {VALUE}",
                listOf("Your saved game name"),
                "Game"
            )
        )

        inventory.setItem(
            34,
            createMenuItem(
                Material.REDSTONE_BLOCK,
                "Cancel",
                listOf(
                    "Cancels the current configuration",
                    "WARNING: The current configuration will be LOST!"
                    )
            )
        )

        inventory.setItem(
            35,
            createMenuItem(
                Material.EMERALD_BLOCK,
                "Save",
                listOf(
                    "Saves the current configuration",
                    "NOTE: You can adjust waves in the Modify Game menu."
                )
            )
        )
    }

    // Slots
    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        when (event.slot) {
            34 -> {
                val menu = Home(player)
                menu.open()
            }
            35 -> {
                GameFactory.createGame(this)
                val menu = Home(player)
                menu.open()
            }
        }
    }
}