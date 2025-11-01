package dev.etran.towerDefMc.menus.enemies

import dev.etran.towerDefMc.managers.PathManager
import dev.etran.towerDefMc.managers.PathCreationSession
import dev.etran.towerDefMc.managers.PathModificationSession
import dev.etran.towerDefMc.menus.games.ModifyGame
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class PathsSelector(player: Player, private val gameId: Int) : CustomMenu(player, 54, "Paths Selector") {

    private val gameManager = GameRegistry.allGames[gameId]
    private val pathManager: PathManager? = gameManager?.pathManager

    override fun setMenuItems() {
        if (pathManager == null) {
            player.sendMessage("§cError: Game not found!")
            player.closeInventory()
            return
        }

        val paths = pathManager.getAllPaths()

        // Display existing paths (top 3 rows)
        paths.take(27).forEachIndexed { index, path ->
            val visibilityIcon = if (path.isVisible) "§a✔" else "§c✖"
            inventory.setItem(
                index,
                createMenuItem(
                    if (path.isVisible) Material.LIME_CONCRETE else Material.GRAY_CONCRETE,
                    "§e${path.name}",
                    listOf(
                        "§7Path ID: ${path.id}",
                        "§7Checkpoints: ${path.checkpoints.size}",
                        "§7Visible: $visibilityIcon ${if (path.isVisible) "Enabled" else "Disabled"}",
                        "",
                        "§eClick to toggle visibility",
                        "§eShift-click to delete path"
                    )
                )
            )
        }

        // Separator row
        for (i in 27..35) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        // Action buttons (bottom 2 rows)
        inventory.setItem(
            37,
            createMenuItem(
                Material.EMERALD_BLOCK,
                "§a§lCreate New Path",
                listOf(
                    "§7Create a new enemy path interactively",
                    "§7You'll place: start point, checkpoints, end point",
                    "§7",
                    "§eClick to start placement mode"
                )
            )
        )

        inventory.setItem(
            39,
            createMenuItem(
                Material.ANVIL,
                "§6§lModify Path Waypoints",
                listOf(
                    "§7Enter modification mode",
                    "§7Punch waypoints to remove them",
                    "§7Replace start/end points if removed",
                    "§7Type 'finish' when done",
                    "",
                    "§eClick to start modification mode"
                )
            )
        )

        inventory.setItem(
            43,
            createMenuItem(
                Material.BARRIER,
                "§cClear All Paths",
                listOf(
                    "§4WARNING: This will delete ALL paths!",
                    "§4This cannot be undone!",
                    "",
                    "§eClick to clear all"
                )
            )
        )

        inventory.setItem(
            49,
            createMenuItem(
                Material.ARROW,
                "§eBack to Game",
                listOf("§7Return to modify game menu")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (pathManager == null) return

        val slot = event.slot

        when {
            slot < 27 -> handlePathClick(event)
            slot == 37 -> handleCreateNewPath()
            slot == 39 -> handleModifyPathWaypoints()
            slot == 43 -> handleClearAllPaths()
            slot == 49 -> handleBack()
        }
    }

    private fun handlePathClick(event: InventoryClickEvent) {
        val slot = event.slot
        val paths = pathManager!!.getAllPaths()

        if (slot >= paths.size) return

        val path = paths[slot]

        when {
            event.isShiftClick -> {
                // Delete path
                pathManager.deletePath(path.id)
                player.sendMessage("§cPath '${path.name}' deleted!")
                gameManager?.saveGame()
                refresh()
            }
            event.isLeftClick || event.isRightClick -> {
                // Toggle visibility (both left and right click)
                pathManager.togglePathVisibility(path.id)
                val newState = if (path.isVisible) "§ahidden" else "§avisible"
                player.sendMessage("§ePath '${path.name}' is now $newState!")
                gameManager?.saveGame()
                refresh()
            }
        }
    }

    private fun handleCreateNewPath() {
        player.closeInventory()
        PathCreationSession.startSession(player, gameId)
    }

    private fun handleModifyPathWaypoints() {
        val paths = pathManager!!.getAllPaths()

        if (paths.isEmpty()) {
            player.sendMessage("§cNo paths available to modify!")
            return
        }

        // If only one path, start modification directly
        if (paths.size == 1) {
            player.closeInventory()
            PathModificationSession.startSession(player, gameId, paths[0].id)
            return
        }

        // Multiple paths - let them select one by clicking
        player.sendMessage("§eClick on a path to modify its waypoints, or shift-click slot 39 for the first path")
        // For now, default to first path
        player.closeInventory()
        PathModificationSession.startSession(player, gameId, paths[0].id)
    }


    private fun handleClearAllPaths() {
        pathManager!!.clearAllPaths()
        player.sendMessage("§cAll paths cleared!")
        gameManager?.saveGame()
        refresh()
    }

    private fun handleBack() {
        player.closeInventory()
        val modifyGameMenu = ModifyGame(player, gameId)
        modifyGameMenu.open()
    }

    private fun refresh() {
        inventory.clear()
        setMenuItems()
    }
}