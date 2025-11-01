package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.menus.enemies.PathsSelector
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**
 * Manages the modification mode where players can punch waypoints to edit them
 */
object PathModificationSession {

    private data class Session(
        val player: Player,
        val gameId: Int,
        val pathId: Int,
        val savedInventory: Array<ItemStack?>,
        val savedArmor: Array<ItemStack?>,
        var removedStartPoint: Boolean = false,
        var removedEndPoint: Boolean = false,
        val removedCheckpoints: MutableSet<Int> = mutableSetOf(),
        var newStartPoint: Location? = null,
        var newEndPoint: Location? = null,
        val newCheckpoints: MutableMap<Int, Location> = mutableMapOf()
    )

    private val activeSessions = mutableMapOf<UUID, Session>()

    /**
     * Start a modification session
     */
    fun startSession(player: Player, gameId: Int, pathId: Int) {
        if (activeSessions.containsKey(player.uniqueId)) {
            player.sendMessage("§cYou are already in modification mode!")
            return
        }

        val gameManager = GameRegistry.allGames[gameId]
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            return
        }

        val path = gameManager.pathManager.getPath(pathId)
        if (path == null) {
            player.sendMessage("§cError: Path not found!")
            return
        }

        // Show the path for modification
        gameManager.pathManager.showPath(pathId)

        // Save current inventory
        val savedInventory = player.inventory.contents.clone()
        val savedArmor = player.inventory.armorContents.clone()

        // Clear inventory
        player.inventory.clear()
        player.inventory.armorContents = arrayOfNulls(4)

        // Give finish item
        giveFinishItem(player)

        // Create session
        val session = Session(player, gameId, pathId, savedInventory, savedArmor)
        activeSessions[player.uniqueId] = session

        // Send instructions
        player.sendMessage("§e§l=================================")
        player.sendMessage("§6Path Modification Mode")
        player.sendMessage("§e§l=================================")
        player.sendMessage("§7Punch any waypoint to remove it")
        player.sendMessage("§7Right-click with items to replace:")
        player.sendMessage("§7  - §aLime Wool §7for Start Point")
        player.sendMessage("§7  - §cRed Wool §7for End Point")
        player.sendMessage("§7")
        player.sendMessage("§c§lIMPORTANT: §7You must replace start and")
        player.sendMessage("§7end points if removed, or the menu won't appear!")
        player.sendMessage("§7")
        player.sendMessage("§aType 'finish' in chat when done")
        player.sendMessage("§e§l=================================")
    }

    /**
     * Handle punching a waypoint to remove it
     */
    fun handleWaypointPunch(player: Player, stand: ArmorStand): Boolean {
        val session = activeSessions[player.uniqueId] ?: return false

        val elementType = stand.persistentDataContainer.get(
            TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
        ) ?: return false

        val gameManager = GameRegistry.allGames[session.gameId] ?: return false
        val path = gameManager.pathManager.getPath(session.pathId) ?: return false

        when (elementType) {
            "PathStart" -> {
                if (session.removedStartPoint) {
                    player.sendMessage("§cStart point already removed!")
                    return false
                }

                session.removedStartPoint = true
                stand.remove()
                player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1f, 0.8f)
                player.sendMessage("§cStart point removed! §7You must replace it with §aLime Wool")

                // Give replacement item if not already in inventory
                if (!hasReplacementItem(player, Material.LIME_WOOL)) {
                    giveReplacementItem(player, Material.LIME_WOOL, "§a§lReplace Start Point")
                }
            }

            "PathEnd" -> {
                if (session.removedEndPoint) {
                    player.sendMessage("§cEnd point already removed!")
                    return false
                }

                session.removedEndPoint = true
                stand.remove()
                player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1f, 0.8f)
                player.sendMessage("§cEnd point removed! §7You must replace it with §cRed Wool")

                // Give replacement item if not already in inventory
                if (!hasReplacementItem(player, Material.RED_WOOL)) {
                    giveReplacementItem(player, Material.RED_WOOL, "§c§lReplace End Point")
                }
            }

            "PathCheckpoint" -> {
                // Find which checkpoint this is
                val standLocation = stand.location
                val checkpointIndex = path.checkpoints.indexOfFirst { loc ->
                    loc.world == standLocation.world && loc.blockX == standLocation.blockX && loc.blockY == standLocation.blockY && loc.blockZ == standLocation.blockZ
                }

                if (checkpointIndex >= 0) {
                    session.removedCheckpoints.add(checkpointIndex)
                    stand.remove()
                    player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1f, 1f)
                    player.sendMessage("§eCheckpoint ${checkpointIndex + 1} removed!")

                    // Immediately update the path to reflect the removal and renumber
                    updatePathVisualization(session)
                }
            }
        }

        return true
    }

    /**
     * Handle placing a replacement waypoint
     */
    fun handleReplacement(player: Player, location: Location, material: Material): Boolean {
        val session = activeSessions[player.uniqueId] ?: return false

        // Validate that the location is on solid ground
        val blockBelow = location.clone().subtract(0.0, 1.0, 0.0).block
        if (!blockBelow.type.isSolid) {
            player.sendMessage("§cYou can only place waypoints on solid ground!")
            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return false
        }

        when (material) {
            Material.LIME_WOOL -> {
                if (!session.removedStartPoint) {
                    player.sendMessage("§cYou haven't removed the start point!")
                    return false
                }

                session.newStartPoint = location
                session.removedStartPoint = false
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
                player.sendMessage("§aStart point replaced!")

                // Remove the item from inventory
                player.inventory.remove(Material.LIME_WOOL)
            }

            Material.RED_WOOL -> {
                if (!session.removedEndPoint) {
                    player.sendMessage("§cYou haven't removed the end point!")
                    return false
                }

                session.newEndPoint = location
                session.removedEndPoint = false
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
                player.sendMessage("§cEnd point replaced!")

                // Remove the item from inventory
                player.inventory.remove(Material.RED_WOOL)
            }

            else -> return false
        }

        return true
    }

    /**
     * Finish the modification session
     */
    fun finishSession(player: Player) {
        val session = activeSessions[player.uniqueId] ?: return

        // Check if start and end points are set
        if (session.removedStartPoint && session.newStartPoint == null) {
            player.sendMessage("§cYou must replace the start point before finishing!")
            return
        }

        if (session.removedEndPoint && session.newEndPoint == null) {
            player.sendMessage("§cYou must replace the end point before finishing!")
            return
        }

        val gameManager = GameRegistry.allGames[session.gameId]
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            cancelSession(player)
            return
        }

        val path = gameManager.pathManager.getPath(session.pathId)
        if (path == null) {
            player.sendMessage("§cError: Path not found!")
            cancelSession(player)
            return
        }

        // Apply changes
        if (session.newStartPoint != null) {
            path.startPoint = session.newStartPoint!!
        }

        if (session.newEndPoint != null) {
            path.endPoint = session.newEndPoint!!
        }

        // Remove checkpoints in reverse order to maintain indices
        session.removedCheckpoints.sortedDescending().forEach { index ->
            path.checkpoints.removeAt(index)
        }

        // Update visualization
        gameManager.pathManager.deletePath(session.pathId)
        val newPathId = gameManager.pathManager.createPath(path.name, path.startPoint, path.endPoint)
        path.checkpoints.forEach { checkpoint ->
            gameManager.pathManager.addCheckpointToPath(newPathId, checkpoint)
        }

        gameManager.saveGame()

        // Restore inventory
        player.inventory.clear()
        player.inventory.contents = session.savedInventory
        player.inventory.armorContents = session.savedArmor

        activeSessions.remove(player.uniqueId)

        player.sendMessage("§a§l=================================")
        player.sendMessage("§6Path modified successfully!")
        player.sendMessage("§a§l=================================")

        // Return to menu
        Bukkit.getScheduler().runTaskLater(TowerDefMC.instance, Runnable {
            val menu = PathsSelector(player, session.gameId)
            menu.open()
        }, 20L)
    }

    /**
     * Cancel the session
     */
    fun cancelSession(player: Player) {
        val session = activeSessions.remove(player.uniqueId) ?: return

        // Hide the path if it was being modified
        val gameManager = GameRegistry.allGames[session.gameId]
        if (gameManager != null) {
            gameManager.pathManager.hidePath(session.pathId)
        }

        // Restore inventory
        player.inventory.clear()
        player.inventory.contents = session.savedInventory
        player.inventory.armorContents = session.savedArmor

        player.sendMessage("§cModification cancelled. Your inventory has been restored.")
    }

    /**
     * Check if player is in a modification session
     */
    fun isInSession(player: Player): Boolean {
        return activeSessions.containsKey(player.uniqueId)
    }

    private fun giveFinishItem(player: Player) {
        val item = ItemStack(Material.EMERALD)
        val meta = item.itemMeta

        meta.displayName(Component.text("§a§lType 'finish' to complete"))
        meta.lore(
            listOf(
                Component.text("§7Type 'finish' in chat when you're"), Component.text("§7done modifying the path")
            )
        )

        item.itemMeta = meta
        player.inventory.setItem(8, item)
    }

    private fun giveReplacementItem(player: Player, material: Material, name: String) {
        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.displayName(Component.text(name))
        meta.lore(
            listOf(
                Component.text("§7Right-click to place replacement")
            )
        )

        meta.persistentDataContainer.set(
            TowerDefMC.createKey("pathReplacementItem"), PersistentDataType.STRING, material.name
        )

        item.itemMeta = meta

        // Find empty slot
        val emptySlot = player.inventory.firstEmpty()
        if (emptySlot >= 0) {
            player.inventory.setItem(emptySlot, item)
        } else {
            player.inventory.addItem(item)
        }
    }

    private fun hasReplacementItem(player: Player, material: Material): Boolean {
        return player.inventory.contents.any { item ->
            item?.type == material && item.itemMeta?.persistentDataContainer?.has(
                TowerDefMC.createKey("pathReplacementItem"), PersistentDataType.STRING
            ) == true
        }
    }

    /**
     * Update the path visualization to reflect current changes
     */
    private fun updatePathVisualization(session: Session) {
        val gameManager = GameRegistry.allGames[session.gameId] ?: return
        val path = gameManager.pathManager.getPath(session.pathId) ?: return

        // Create a temporary path with current modifications
        val currentCheckpoints = path.checkpoints.filterIndexed { index, _ ->
            index !in session.removedCheckpoints
        }.toMutableList()

        val currentStartPoint = session.newStartPoint ?: path.startPoint
        val currentEndPoint = session.newEndPoint ?: path.endPoint

        // Delete and recreate the path with updated numbering
        gameManager.pathManager.deletePath(session.pathId)
        val newPathId = gameManager.pathManager.createPath(path.name, currentStartPoint, currentEndPoint)

        // Update the session to track the new path ID
        // Use reflection to update the pathId in the session
        val pathIdField = Session::class.java.getDeclaredField("pathId")
        pathIdField.isAccessible = true
        pathIdField.set(session, newPathId)

        // Add the current checkpoints
        currentCheckpoints.forEach { checkpoint ->
            gameManager.pathManager.addCheckpointToPath(newPathId, checkpoint)
        }

        // Show the path for continued modification
        gameManager.pathManager.showPath(newPathId)

        // Clear removed checkpoints since they're now reflected in the visualization
        session.removedCheckpoints.clear()
    }
}
