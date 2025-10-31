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
 * Manages the interactive path creation process where players place waypoints with items
 */
object PathCreationSession {

    enum class PlacementPhase {
        START_POINT,
        CHECKPOINTS,
        END_POINT,
        COMPLETED
    }

    private data class Session(
        val player: Player,
        val gameId: Int,
        val savedInventory: Array<ItemStack?>,
        val savedArmor: Array<ItemStack?>,
        var phase: PlacementPhase = PlacementPhase.START_POINT,
        var startPoint: Location? = null,
        val checkpoints: MutableList<Location> = mutableListOf(),
        var endPoint: Location? = null,
        val placedStands: MutableList<ArmorStand> = mutableListOf()
    )

    private val activeSessions = mutableMapOf<UUID, Session>()

    /**
     * Start a new path creation session
     */
    fun startSession(player: Player, gameId: Int) {
        if (activeSessions.containsKey(player.uniqueId)) {
            player.sendMessage("§cYou are already creating a path!")
            return
        }

        // Save current inventory
        val savedInventory = player.inventory.contents.clone()
        val savedArmor = player.inventory.armorContents.clone()

        // Clear inventory
        player.inventory.clear()
        player.inventory.armorContents = arrayOfNulls(4)

        // Give start point item
        giveStartPointItem(player)

        // Create session
        val session = Session(player, gameId, savedInventory, savedArmor)
        activeSessions[player.uniqueId] = session

        // Send instructions
        sendPhaseInstructions(player, PlacementPhase.START_POINT)
    }

    /**
     * Handle placement of a waypoint
     */
    fun handlePlacement(player: Player, location: Location): Boolean {
        val session = activeSessions[player.uniqueId] ?: return false

        when (session.phase) {
            PlacementPhase.START_POINT -> {
                session.startPoint = location
                val stand = createArmorStand(location, "§a§lSTART POINT", Material.LIME_WOOL, "PathStart")
                session.placedStands.add(stand)

                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
                player.sendMessage("§aStart point placed!")

                // Move to checkpoint phase
                session.phase = PlacementPhase.CHECKPOINTS
                giveCheckpointItem(player)
                sendPhaseInstructions(player, PlacementPhase.CHECKPOINTS)
            }

            PlacementPhase.CHECKPOINTS -> {
                session.checkpoints.add(location)
                val checkpointNum = session.checkpoints.size
                val stand = createArmorStand(location, "§e§lCHECKPOINT $checkpointNum", Material.YELLOW_WOOL, "PathCheckpoint")
                session.placedStands.add(stand)

                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f)
                player.sendMessage("§aCheckpoint $checkpointNum placed! (Total: ${session.checkpoints.size})")

                // Update items to show end point option
                updateCheckpointPhaseItems(player)
            }

            PlacementPhase.END_POINT -> {
                session.endPoint = location
                val stand = createArmorStand(location, "§c§lEND POINT", Material.RED_WOOL, "PathEnd")
                session.placedStands.add(stand)

                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
                player.sendMessage("§aEnd point placed!")

                // Complete the path
                completePath(player, session)
            }

            PlacementPhase.COMPLETED -> return false
        }

        return true
    }

    /**
     * Switch from checkpoint phase to end point phase
     */
    fun switchToEndPointPhase(player: Player) {
        val session = activeSessions[player.uniqueId] ?: return

        if (session.phase != PlacementPhase.CHECKPOINTS) {
            player.sendMessage("§cYou can only place the end point after placing checkpoints!")
            return
        }

        session.phase = PlacementPhase.END_POINT
        giveEndPointItem(player)
        sendPhaseInstructions(player, PlacementPhase.END_POINT)
    }

    /**
     * Complete the path creation
     */
    private fun completePath(player: Player, session: Session) {
        val gameManager = GameRegistry.allGames[session.gameId]
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            cancelSession(player)
            return
        }

        // Remove temporary stands
        session.placedStands.forEach { it.remove() }

        // Create the actual path
        val pathId = gameManager.pathManager.createPath("New Path", session.startPoint!!, session.endPoint!!)

        // Add checkpoints
        session.checkpoints.forEach { checkpoint ->
            gameManager.pathManager.addCheckpointToPath(pathId, checkpoint)
        }

        gameManager.saveGame()

        // Restore inventory
        player.inventory.clear()
        player.inventory.contents = session.savedInventory
        player.inventory.armorContents = session.savedArmor

        activeSessions.remove(player.uniqueId)

        player.sendMessage("§a§l=================================")
        player.sendMessage("§6Path created successfully!")
        player.sendMessage("§7- Start point placed")
        player.sendMessage("§7- ${session.checkpoints.size} checkpoint(s) placed")
        player.sendMessage("§7- End point placed")
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

        // Remove temporary stands
        session.placedStands.forEach { it.remove() }

        // Restore inventory
        player.inventory.clear()
        player.inventory.contents = session.savedInventory
        player.inventory.armorContents = session.savedArmor

        player.sendMessage("§cPath creation cancelled. Your inventory has been restored.")
    }

    /**
     * Check if player is in a creation session
     */
    fun isInSession(player: Player): Boolean {
        return activeSessions.containsKey(player.uniqueId)
    }

    /**
     * Get the current phase of the session
     */
    fun getSessionPhase(player: Player): PlacementPhase? {
        return activeSessions[player.uniqueId]?.phase
    }

    private fun giveStartPointItem(player: Player) {
        player.inventory.clear()
        val item = createPlacementItem(Material.LIME_WOOL, "§a§lPlace Start Point",
            listOf("§7Right-click to place the start point", "§7where enemies will spawn"))
        player.inventory.setItem(4, item)
    }

    private fun giveCheckpointItem(player: Player) {
        player.inventory.clear()
        val checkpointItem = createPlacementItem(Material.YELLOW_WOOL, "§e§lPlace Checkpoint",
            listOf("§7Right-click to place a checkpoint", "§7Add as many as you need"))
        player.inventory.setItem(4, checkpointItem)

        val endItem = createPlacementItem(Material.RED_WOOL, "§c§lPlace End Point",
            listOf("§7Right-click to place the end point", "§7This will complete the path"))
        player.inventory.setItem(8, endItem)
    }

    private fun updateCheckpointPhaseItems(player: Player) {
        giveCheckpointItem(player)
    }

    private fun giveEndPointItem(player: Player) {
        player.inventory.clear()
        val item = createPlacementItem(Material.RED_WOOL, "§c§lPlace End Point",
            listOf("§7Right-click to place the end point", "§7where enemies reach their goal"))
        player.inventory.setItem(4, item)
    }

    private fun createPlacementItem(material: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.displayName(Component.text(name))
        meta.lore(lore.map { Component.text(it) })

        // Mark as path creation item
        meta.persistentDataContainer.set(
            TowerDefMC.createKey("pathCreationItem"),
            PersistentDataType.STRING,
            material.name
        )

        item.itemMeta = meta
        return item
    }

    private fun createArmorStand(location: Location, name: String, @Suppress("UNUSED_PARAMETER") material: Material, type: String): ArmorStand {
        return location.world.spawn(location, ArmorStand::class.java) { stand ->
            stand.isVisible = true
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.customName(Component.text(name))
            stand.isCustomNameVisible = true
            stand.persistentDataContainer.set(
                TowerDefMC.ELEMENT_TYPES,
                PersistentDataType.STRING,
                type
            )
        }
    }

    private fun sendPhaseInstructions(player: Player, phase: PlacementPhase) {
        player.sendMessage("§e§l=================================")
        when (phase) {
            PlacementPhase.START_POINT -> {
                player.sendMessage("§6Phase 1: Place Start Point")
                player.sendMessage("§7Right-click with the §aLIME WOOL §7to place")
                player.sendMessage("§7the start point where enemies spawn")
            }
            PlacementPhase.CHECKPOINTS -> {
                player.sendMessage("§6Phase 2: Place Checkpoints")
                player.sendMessage("§7Right-click with §eYELLOW WOOL §7to add checkpoints")
                player.sendMessage("§7Right-click with §cRED WOOL §7when ready for end point")
            }
            PlacementPhase.END_POINT -> {
                player.sendMessage("§6Phase 3: Place End Point")
                player.sendMessage("§7Right-click with the §cRED WOOL §7to place")
                player.sendMessage("§7the end point where enemies reach their goal")
            }
            PlacementPhase.COMPLETED -> {}
        }
        player.sendMessage("§e§l=================================")
    }
}
