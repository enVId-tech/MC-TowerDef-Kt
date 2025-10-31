package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**
 * Factory for creating and managing waypoint spawn mode sessions
 * Handles the spawning of start points, checkpoints, and end points
 */
object WaypointFactory {

    enum class WaypointType {
        START_POINT,
        END_POINT,
        CHECKPOINT
    }

    private data class SpawnModeSession(
        val player: Player,
        val gameId: Int,
        val waypointType: WaypointType,
        val savedInventory: Array<ItemStack?>,
        val savedArmor: Array<ItemStack?>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SpawnModeSession) return false
            return player == other.player && gameId == other.gameId && waypointType == other.waypointType
        }

        override fun hashCode(): Int {
            return Objects.hash(player, gameId, waypointType)
        }
    }

    private val activeSessions = mutableMapOf<UUID, SpawnModeSession>()

    /**
     * Start spawn mode for a player
     */
    fun startSpawnMode(player: Player, gameId: Int, waypointType: WaypointType) {
        if (activeSessions.containsKey(player.uniqueId)) {
            player.sendMessage("§cYou are already in spawn mode!")
            return
        }

        // Save current inventory
        val savedInventory = player.inventory.contents.clone()
        val savedArmor = player.inventory.armorContents.clone()

        // Clear inventory
        player.inventory.clear()
        player.inventory.armorContents = arrayOfNulls(4)

        // Give spawn item
        val spawnItem = createSpawnItem(waypointType)
        player.inventory.setItem(4, spawnItem) // Middle slot

        // Create session
        val session = SpawnModeSession(player, gameId, waypointType, savedInventory, savedArmor)
        activeSessions[player.uniqueId] = session

        // Send instructions
        player.sendMessage("§e§l=================================")
        player.sendMessage("§6Spawn Mode - ${getWaypointTypeName(waypointType)}")
        player.sendMessage("§e=================================")
        player.sendMessage("§aRight-click with the item to place a ${getWaypointTypeName(waypointType).lowercase()}")
        player.sendMessage("§aType anything in chat to exit spawn mode")
        player.sendMessage("§e=================================")
    }

    /**
     * End spawn mode and restore inventory
     */
    fun endSpawnMode(player: Player) {
        val session = activeSessions.remove(player.uniqueId) ?: return

        // Restore inventory
        player.inventory.clear()
        player.inventory.contents = session.savedInventory
        player.inventory.armorContents = session.savedArmor

        player.sendMessage("§aExited spawn mode. Your inventory has been restored.")
    }

    /**
     * Check if player is in spawn mode
     */
    fun isInSpawnMode(player: Player): Boolean {
        return activeSessions.containsKey(player.uniqueId)
    }

    /**
     * Handle spawn placement
     */
    fun placeWaypoint(player: Player, location: Location): Boolean {
        val session = activeSessions[player.uniqueId] ?: return false

        // Get the waypoint manager for this game
        val gameManager = GameRegistry.allGames[session.gameId]
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            return false
        }

        // Place waypoint based on type
        when (session.waypointType) {
            WaypointType.START_POINT -> {
                gameManager.waypointManager.setStartPoint(location)
                player.sendMessage("§aStart point set at: §e${formatLocation(location)}")
            }
            WaypointType.END_POINT -> {
                gameManager.waypointManager.setEndPoint(location)
                player.sendMessage("§aEnd point set at: §e${formatLocation(location)}")
            }
            WaypointType.CHECKPOINT -> {
                gameManager.waypointManager.addCheckpoint(location)
                player.sendMessage("§aCheckpoint added at: §e${formatLocation(location)}")
            }
        }

        // Play effects
        player.world.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
        player.world.spawnParticle(Particle.HAPPY_VILLAGER, location, 20, 0.5, 0.5, 0.5)

        return true
    }

    private fun createSpawnItem(waypointType: WaypointType): ItemStack {
        val material = when (waypointType) {
            WaypointType.START_POINT -> Material.GREEN_WOOL
            WaypointType.END_POINT -> Material.RED_WOOL
            WaypointType.CHECKPOINT -> Material.YELLOW_WOOL
        }

        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.displayName(Component.text("§6${getWaypointTypeName(waypointType)} Placer"))
        meta.lore(listOf(
            Component.text("§7Right-click to place a ${getWaypointTypeName(waypointType).lowercase()}"),
            Component.text("§7Type anything in chat to exit")
        ))

        // Mark as spawn item
        val pdc = meta.persistentDataContainer
        pdc.set(
            org.bukkit.NamespacedKey(TowerDefMC.instance, "waypoint_item"),
            PersistentDataType.STRING,
            waypointType.name
        )

        item.itemMeta = meta
        return item
    }

    private fun getWaypointTypeName(waypointType: WaypointType): String {
        return when (waypointType) {
            WaypointType.START_POINT -> "Start Point"
            WaypointType.END_POINT -> "End Point"
            WaypointType.CHECKPOINT -> "Checkpoint"
        }
    }

    private fun formatLocation(loc: Location): String {
        return "${loc.world?.name} (${loc.blockX}, ${loc.blockY}, ${loc.blockZ})"
    }
}

