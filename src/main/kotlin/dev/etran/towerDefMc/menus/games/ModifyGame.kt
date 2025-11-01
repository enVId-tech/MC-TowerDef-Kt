package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.factories.WaypointFactory
import dev.etran.towerDefMc.menus.waves.Waves
import dev.etran.towerDefMc.menus.towers.TowerSelection
import dev.etran.towerDefMc.menus.enemies.PathsSelector
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.registries.TowerRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

class ModifyGame(
    player: Player, val gameId: Int
) : CustomMenu(player, 54, "Modify Game") {

    private val gameManager = GameRegistry.allGames[gameId]

    override fun setMenuItems() {
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            player.closeInventory()
            return
        }

        val config = gameManager.config

        // Calculate enabled tower count
        val enabledTowerCount = if (config.allowedTowers.isEmpty()) {
            // If allowedTowers is empty, all towers are enabled by default
            TowerRegistry.getAllTowers().size
        } else {
            // Count towers that are not disabled (limit != 0)
            val towerLimits = config.allowedTowers.associate { entry: String ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    parts[0] to (parts[1].toIntOrNull() ?: -1)
                } else {
                    entry to -1
                }
            }
            towerLimits.count { it.value != 0 }
        }

        // Top row - Basic settings
        inventory.setItem(
            10, createRenamableItem(
                Material.REDSTONE_BLOCK,
                "Max Health: {VALUE}",
                listOf("The default maximum game health.", "Current: {VALUE}"),
                config.maxHealth.toString()
            )
        )

        inventory.setItem(
            13, createRenamableItem(
                Material.EMERALD, "Default Starting Cash: {VALUE}", listOf(
                    "The default starting cash.", "Current starting cash: {VALUE}"
                ), config.defaultCash.toString()
            )
        )

        inventory.setItem(
            16, createRenamableItem(
                Material.OAK_SIGN, "Game Name: {VALUE}", listOf("Your saved game name", "Current: {VALUE}"), config.name
            )
        )

        // Second row - Waves and Towers
        inventory.setItem(
            19, createMenuItem(
                Material.ZOMBIE_HEAD, "Waves", listOf(
                    "All wave configurations", "§7Total waves: ${config.waves.size}", "§eClick to manage waves"
                )
            )
        )

        inventory.setItem(
            22, createMenuItem(
                Material.BOW, "Towers", listOf(
                    "List of allowed towers", "§7Allowed: $enabledTowerCount", "§eClick to manage towers"
                )
            )
        )

        // Third row - Paths
        val pathCount = gameManager.pathManager.getAllPaths().size
        inventory.setItem(
            30, createMenuItem(
                Material.COMPASS, "§6Enemy Paths", listOf(
                    "§7Manage enemy movement paths",
                    "§7Total paths: $pathCount",
                    "§7Create paths with start, checkpoints, and end",
                    "§7Enemies will randomly choose a path",
                    "",
                    "§eClick to manage paths"
                )
            )
        )

        // Start Game Button
        inventory.setItem(
            33, createMenuItem(
                Material.EMERALD_BLOCK,
                "§a§lSTART GAME",
                listOf(
                    "§7Start this tower defense game",
                    "§7You will be added as a player",
                    "",
                    "§eClick to start the game!"
                )
            )
        )

        // Bottom row - Actions
        inventory.setItem(
            49, createMenuItem(
                Material.BARRIER, "§cBack", listOf("Return to game selection")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (gameManager == null) return

        when (event.slot) {
            19 -> handleWavesClick()
            22 -> handleTowersClick()
            30 -> handlePathsClick()
            33 -> handleStartGameClick()
            49 -> handleBack()
            10, 13, 16 -> handleValueUpdate(event)
        }
    }

    private fun handleWavesClick() {
        player.closeInventory()
        val wavesMenu = Waves(player, gameManager!!.config, gameId = gameId)
        wavesMenu.open()
    }

    private fun handleTowersClick() {
        player.closeInventory()
        val towerSelectionMenu = TowerSelection(player, gameManager!!.config, gameId)
        towerSelectionMenu.open()
    }

    private fun handlePathsClick() {
        player.closeInventory()
        val pathsMenu = PathsSelector(player, gameId)
        pathsMenu.open()
    }

    private fun handleStartGameClick() {
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            return
        }

        // Validate game setup
        val paths = gameManager.pathManager.getAllPaths()
        if (paths.isEmpty()) {
            player.sendMessage("§c§lCannot start game!")
            player.sendMessage("§cYou must create at least one enemy path first.")
            player.sendMessage("§7Go to 'Enemy Paths' to create a path.")
            return
        }

        if (gameManager.config.waves.isEmpty()) {
            player.sendMessage("§c§lCannot start game!")
            player.sendMessage("§cYou must configure at least one wave first.")
            player.sendMessage("§7Go to 'Waves' to create waves.")
            return
        }

        // Start the game
        player.closeInventory()
        gameManager.startGame(listOf(player.uniqueId))

        player.sendMessage("§a§l========================================")
        player.sendMessage("§6§l        GAME STARTED!")
        player.sendMessage("§a§l========================================")
        player.sendMessage("§e${gameManager.config.name}")
        player.sendMessage("§7Health: §c${gameManager.config.maxHealth}")
        player.sendMessage("§7Starting Cash: §a${gameManager.config.defaultCash}")
        player.sendMessage("§7Waves: §e${gameManager.config.waves.size}")
        player.sendMessage("§7Paths: §b${paths.size}")
        player.sendMessage("§a§l========================================")
        player.sendMessage("§aThe first wave will begin shortly!")
    }

    private fun handleBack() {
        player.closeInventory()
        val gameSelectorMenu = GameSelector(player)
        gameSelectorMenu.open()
    }

    private fun handleValueUpdate(event: InventoryClickEvent) {
        // Auto-save when values are modified via renamable items
        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        val value = pdc.get(
            TowerDefMC.TITLE_KEY, PersistentDataType.STRING
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

    override fun onItemRenamed(slot: Int, newValue: String) {
        // Called after a rename completes - auto-save the changes
        if (gameManager == null) return

        when (slot) {
            10 -> {
                val maxHealth = newValue.toIntOrNull()
                if (maxHealth != null) {
                    gameManager.updateMaxHealth(maxHealth)
                    player.sendMessage("§aMax Health updated to $maxHealth and saved!")
                } else {
                    player.sendMessage("§cInvalid number for Max Health!")
                }
            }

            13 -> {
                val defaultCash = newValue.toIntOrNull()
                if (defaultCash != null) {
                    gameManager.updateDefaultCash(defaultCash)
                    player.sendMessage("§aDefault Cash updated to $defaultCash and saved!")
                } else {
                    player.sendMessage("§cInvalid number for Default Cash!")
                }
            }

            16 -> {
                gameManager.updateGameName(newValue)
                player.sendMessage("§aGame name updated to '$newValue' and saved!")
            }
        }
    }
}
