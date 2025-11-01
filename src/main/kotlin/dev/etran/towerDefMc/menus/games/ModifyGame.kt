package dev.etran.towerDefMc.menus.games

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.factories.GameStatsDisplayFactory
import dev.etran.towerDefMc.menus.Home
import dev.etran.towerDefMc.menus.waves.Waves
import dev.etran.towerDefMc.menus.towers.TowerSelection
import dev.etran.towerDefMc.menus.enemies.PathsSelector
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.registries.TowerRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.inventory.ItemStack

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
                Material.EMERALD_BLOCK, "§a§lSTART GAME", listOf(
                    "§7Start this tower defense game",
                    "§7You will be added as a player",
                    "",
                    "§eClick to start the game!"
                )
            )
        )

        // Stop Game Button (only show if game is running)
        if (gameManager.isGameRunning) {
            inventory.setItem(
                25, createMenuItem(
                    Material.REDSTONE_BLOCK, "§c§lSTOP GAME", listOf(
                        "§7Stop the currently running game",
                        "§7This will end the game for all players",
                        "",
                        "§cClick to stop the game"
                    )
                )
            )
        }

        // Game Stats Display Button
        inventory.setItem(
            36, createMenuItem(
                Material.LECTERN, "§b§lPlace Game Stats Display", listOf(
                    "§7Receive an item to place a",
                    "§7game statistics display board",
                    "§7Shows: Health, Wave, Players, etc.",
                    "",
                    "§eClick to receive item"
                )
            )
        )

        // Shop Villager Spawner Button
        inventory.setItem(
            39, createMenuItem(
                Material.VILLAGER_SPAWN_EGG, "§6§lTower Shop Villager", listOf(
                    "§7Receive a shop villager spawner",
                    "§7Place this villager to create a",
                    "§7tower shop for your game",
                    "§7Players can buy towers during gameplay",
                    "",
                    "§eClick to receive spawner"
                )
            )
        )

        // Bottom row - Actions
        // Delete Game Button (bottom-left)
        inventory.setItem(
            45, createMenuItem(
                Material.BARRIER, "§c§lDelete Game", listOf(
                    "§7Permanently delete this game",
                    "§7This action cannot be undone!",
                    "§7All game data will be lost",
                    "",
                    "§cClick to delete game"
                )
            )
        )

        // Back Button (center-bottom)
        inventory.setItem(
            49, createMenuItem(
                Material.OAK_DOOR, "§eBack", listOf("Return to game selection")
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
            36 -> handleStatsDisplayClick()
            39 -> handleShopVillagerClick()
            45 -> handleDeleteGameClick()
            49 -> handleBack()
            10, 13, 16 -> handleValueUpdate(event)
            25 -> handleStopGameClick() // Handle stop game click (only if game is running)
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

    private fun handleStopGameClick() {
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            return
        }

        // Stop the game
        gameManager.stopGame()

        player.sendMessage("§a§l========================================")
        player.sendMessage("§6§l        GAME STOPPED!")
        player.sendMessage("§a§l========================================")
        player.sendMessage("§e${gameManager.config.name}")
        player.sendMessage("§7The game has been stopped.")
        player.sendMessage("§a§l========================================")
    }

    private fun handleStatsDisplayClick() {
        // Call the actual method that gives the item
        handleGameStatsDisplayClick()
    }

    private fun handleBack() {
        player.closeInventory()
        val gameSelectorMenu = GameSelector(player)
        gameSelectorMenu.open()
    }

    private fun handleGameStatsDisplayClick() {
        player.closeInventory()

        val item = GameStatsDisplayFactory.createGameStatsItem(1)

        // Add to inventory or drop if full
        val leftover = player.inventory.addItem(item)
        leftover.values.forEach { itemStack ->
            player.world.dropItemNaturally(player.location, itemStack)
        }

        player.sendMessage("§a§l===========================================")
        player.sendMessage("§6§lGame Stats Display Item Received!")
        player.sendMessage("§a§l===========================================")
        player.sendMessage("§7Right-click on a block to place the")
        player.sendMessage("§7game statistics display board.")
        player.sendMessage("§7")
        player.sendMessage("§7The display will show:")
        player.sendMessage("§e  • §7Current wave and total waves")
        player.sendMessage("§e  • §7Game health remaining")
        player.sendMessage("§e  • §7Number of active players")
        player.sendMessage("§e  • §7Active enemy paths")
        player.sendMessage("§e  • §7Team statistics (kills, towers, damage)")
        player.sendMessage("§a§l===========================================")
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

    private fun handleShopVillagerClick() {
        player.closeInventory()

        // Use the existing GiveShopVillager command logic to create the item
        val shopEgg = ItemStack(Material.VILLAGER_SPAWN_EGG, 1)
        val meta = shopEgg.itemMeta

        meta.displayName(Component.text("§6§lTower Shop Villager"))
        meta.lore(
            listOf(
                Component.text("§7Place this villager to create"),
                Component.text("§7a tower shop for your game"),
                Component.text(""),
                Component.text("§eRight-click to place"),
                Component.text("§7Players can buy towers from this shop")
            )
        )

        // Mark it as a shop villager spawner
        meta.persistentDataContainer.set(
            TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Tower_Shop_Spawner"
        )

        // Store the game ID in the item so it knows which game it belongs to
        meta.persistentDataContainer.set(
            TowerDefMC.GAME_ID_KEY, PersistentDataType.INTEGER, gameId
        )

        shopEgg.itemMeta = meta

        // Add to inventory or drop if full
        val leftover = player.inventory.addItem(shopEgg)
        leftover.values.forEach { itemStack ->
            player.world.dropItemNaturally(player.location, itemStack)
        }

        player.sendMessage("§a§l===========================================")
        player.sendMessage("§6§lTower Shop Villager Spawner Received!")
        player.sendMessage("§a§l===========================================")
        player.sendMessage("§7Place this villager in your game area")
        player.sendMessage("§7to create a tower shop.")
        player.sendMessage("§7Players can buy towers during gameplay.")
        player.sendMessage("§a§l===========================================")
    }

    private fun handleDeleteGameClick() {
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            return
        }

        // Check if the game is currently running
        if (gameManager.isGameRunning) {
            player.sendMessage("§c§lCannot Delete Game!")
            player.sendMessage("§cThe game is currently running.")
            player.sendMessage("§7Stop the game first before deleting it.")
            return
        }

        player.closeInventory()

        // Delete the game
        val game = GameRegistry.getGameById(gameId)
        if (game != null) {
            GameRegistry.removeGame(game)

            player.sendMessage("§c§l===========================================")
            player.sendMessage("§c§l        GAME DELETED!")
            player.sendMessage("§c§l===========================================")
            player.sendMessage("§7Game '§e${gameManager.config.name}§7' has been")
            player.sendMessage("§7permanently deleted.")
            player.sendMessage("§c§l===========================================")
        } else {
            player.sendMessage("No game could be found.")
        }
        // Return to home menu
        val homeMenu = Home(player)
        homeMenu.open()
    }
}
