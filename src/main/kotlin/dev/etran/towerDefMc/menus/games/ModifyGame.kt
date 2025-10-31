package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.menus.waves.Waves
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ModifyGame(
    player: Player,
    val gameId: Int
) : CustomMenu(player, 54, "Modify Game") {

    private val gameManager = GameRegistry.allGames[gameId]

    override fun setMenuItems() {
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            player.closeInventory()
            return
        }

        val config = gameManager.config

        inventory.setItem(
            10,
            createRenamableItem(
                Material.REDSTONE_BLOCK,
                "Max Health: {VALUE}",
                listOf("The default maximum game health.", "Current: {VALUE}"),
                config.maxHealth.toString()
            )
        )

        inventory.setItem(
            13,
            createRenamableItem(
                Material.EMERALD,
                "Default Starting Cash: {VALUE}",
                listOf(
                    "The default starting cash.",
                    "Current starting cash: {VALUE}"
                ),
                config.defaultCash.toString()
            )
        )

        inventory.setItem(
            16,
            createRenamableItem(
                Material.OAK_SIGN,
                "Game Name: {VALUE}",
                listOf("Your saved game name", "Current: {VALUE}"),
                config.name
            )
        )

        inventory.setItem(
            19,
            createMenuItem(
                Material.ZOMBIE_HEAD,
                "Waves",
                listOf(
                    "All wave configurations",
                    "§7Total waves: ${config.waves.size}",
                    "§eClick to manage waves"
                )
            )
        )

        inventory.setItem(
            21,
            createMenuItem(
                Material.BOW,
                "Towers",
                listOf(
                    "List of allowed towers",
                    "§7Allowed: ${config.allowedTowers.size}",
                    "§eClick to manage towers"
                )
            )
        )

        inventory.setItem(
            49,
            createMenuItem(
                Material.BARRIER,
                "§cBack",
                listOf("Return to game selection")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (gameManager == null) return

        when (event.slot) {
            19 -> handleWavesClick()
            21 -> handleTowersClick()
            49 -> handleBack()
            10, 13, 16 -> handleValueUpdate(event)
        }
    }

    private fun handleWavesClick() {
        player.closeInventory()
        val wavesMenu = Waves(player, gameManager!!.config)
        wavesMenu.open()
    }

    private fun handleTowersClick() {
        player.sendMessage("§eTowers management coming soon!")
        // TODO: Implement towers management menu
    }

    private fun handleBack() {
        player.closeInventory()
        player.sendMessage("§aReturning to game selection")
        // TODO: Open game selector menu
    }

    private fun handleValueUpdate(event: InventoryClickEvent) {
        // Auto-save when values are modified via renamable items
        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        val value = pdc.get(
            dev.etran.towerDefMc.TowerDefMC.TITLE_KEY,
            org.bukkit.persistence.PersistentDataType.STRING
        ) ?: return

        when (event.slot) {
            10 -> {
                val maxHealth = value.toIntOrNull()
                if (maxHealth != null) {
                    gameManager!!.updateMaxHealth(maxHealth)
                    player.sendMessage("§aMax Health updated to $maxHealth and saved!")
                } else {
                    player.sendMessage("§cInvalid number for Max Health!")
                }
            }
            13 -> {
                val defaultCash = value.toIntOrNull()
                if (defaultCash != null) {
                    gameManager!!.updateDefaultCash(defaultCash)
                    player.sendMessage("§aDefault Cash updated to $defaultCash and saved!")
                } else {
                    player.sendMessage("§cInvalid number for Default Cash!")
                }
            }
            16 -> {
                gameManager!!.updateGameName(value)
                player.sendMessage("§aGame name updated to '$value' and saved!")
            }
        }
    }
}