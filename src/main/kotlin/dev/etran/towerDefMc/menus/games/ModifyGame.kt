package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ModifyGame(
    player: Player,
    val slot: Int
) : CustomMenu(player, 54, "Modify Game") {
    override fun setMenuItems() {
        inventory.setItem(
            10,
            createRenamableItem(
                Material.REDSTONE_BLOCK,
                "Max Health",
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
            19,
            createMenuItem(
                Material.ZOMBIE_HEAD,
                "Waves",
                listOf(
                    "All wave configurations"
                )
            )
        )

        inventory.setItem(
            21,
            createMenuItem(
                Material.BOW,
                "Towers",
                listOf(
                    "List of allowed towers"
                )
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        TODO("Not yet implemented")
        /*
        Should implement redirect to Waves list and Towers list
        Should implement auto save whenever a value gets modified
         */
    }
}