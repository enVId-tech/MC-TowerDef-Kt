package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**
 * Manages spawn point placement mode for players
 */
object SpawnModeManager {

    enum class SpawnType {
        START_POINT, END_POINT, CHECKPOINT
    }

    private data class SpawnModeSession(
        val player: Player,
        val gameId: Int,
        val spawnType: SpawnType,
        val savedInventory: Array<ItemStack?>,
        val savedArmor: Array<ItemStack?>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SpawnModeSession) return false
            return player == other.player && gameId == other.gameId && spawnType == other.spawnType
        }

        override fun hashCode(): Int {
            return Objects.hash(player, gameId, spawnType)
        }
    }

    data class SpawnableSurfaceSession(
        val player: Player,
        val gameId: Int,
        val surfaceId: Int,
        val highlightTaskId: Int
    )

    private data class ChatInputSession(
        val player: Player,
        val callback: (String) -> Unit
    )

    private val activeSessions = mutableMapOf<UUID, SpawnModeSession>()
    private val activeSurfaceSessions = mutableMapOf<UUID, SpawnableSurfaceSession>()
    private val chatInputSessions = mutableMapOf<UUID, ChatInputSession>()
    private var nextTaskId = 0

    /**
     * Start spawn mode for a player
     */
    fun startSpawnMode(player: Player, gameId: Int, spawnType: SpawnType) {
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
        val spawnItem = createSpawnItem(spawnType)
        player.inventory.setItem(4, spawnItem) // Middle slot

        // Create session
        val session = SpawnModeSession(player, gameId, spawnType, savedInventory, savedArmor)
        activeSessions[player.uniqueId] = session

        // Send instructions
        player.sendMessage("§e§l=================================")
        player.sendMessage("§6Spawn Mode - ${getSpawnTypeName(spawnType)}")
        player.sendMessage("§e=================================")
        player.sendMessage("§aRight-click with the item to place a ${getSpawnTypeName(spawnType).lowercase()}")
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
    fun placeSpawn(player: Player, location: Location): Boolean {
        val session = activeSessions[player.uniqueId] ?: return false

        // Save spawn location to game config
        val gameManager = GameRegistry.allGames[session.gameId]
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            return false
        }

        // Store location based on spawn type
        when (session.spawnType) {
            SpawnType.START_POINT -> {
                gameManager.setStartPoint(location)
                player.sendMessage("§aStart point set at: §e${formatLocation(location)}")
            }

            SpawnType.END_POINT -> {
                gameManager.setEndPoint(location)
                player.sendMessage("§aEnd point set at: §e${formatLocation(location)}")
            }

            SpawnType.CHECKPOINT -> {
                gameManager.addCheckpoint(location)
                player.sendMessage("§aCheckpoint added at: §e${formatLocation(location)}")
            }
        }

        // Play effects
        player.world.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
        player.world.spawnParticle(Particle.HAPPY_VILLAGER, location, 20, 0.5, 0.5, 0.5)

        return true
    }

    private fun createSpawnItem(spawnType: SpawnType): ItemStack {
        val material = when (spawnType) {
            SpawnType.START_POINT -> Material.GREEN_WOOL
            SpawnType.END_POINT -> Material.RED_WOOL
            SpawnType.CHECKPOINT -> Material.YELLOW_WOOL
        }

        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.displayName(Component.text("§6${getSpawnTypeName(spawnType)} Placer"))
        meta.lore(
            listOf(
                Component.text("§7Right-click to place a ${getSpawnTypeName(spawnType).lowercase()}"),
                Component.text("§7Type anything in chat to exit")
            )
        )

        // Mark as spawn item
        val pdc = meta.persistentDataContainer
        pdc.set(
            NamespacedKey(TowerDefMC.instance, "spawn_item"), PersistentDataType.STRING, spawnType.name
        )

        item.itemMeta = meta
        return item
    }

    private fun getSpawnTypeName(spawnType: SpawnType): String {
        return when (spawnType) {
            SpawnType.START_POINT -> "Start Point"
            SpawnType.END_POINT -> "End Point"
            SpawnType.CHECKPOINT -> "Checkpoint"
        }
    }

    private fun formatLocation(loc: Location): String {
        return "${loc.world?.name} (${loc.blockX}, ${loc.blockY}, ${loc.blockZ})"
    }

    /**
     * Start spawnable surface mode for a player
     */
    fun startSpawnableSurfaceMode(player: Player, gameId: Int, surfaceId: Int) {
        if (activeSurfaceSessions.containsKey(player.uniqueId)) {
            player.sendMessage("§cYou are already editing a spawnable surface!")
            return
        }

        val gameManager = GameRegistry.allGames[gameId]
        val surface = gameManager?.spawnableSurfaceManager?.getSurface(surfaceId)

        if (gameManager == null || surface == null) {
            player.sendMessage("§cError: Surface not found!")
            return
        }

        // Start particle highlight task
        val taskId = nextTaskId++
        val task = TowerDefMC.instance.server.scheduler.runTaskTimer(TowerDefMC.instance, Runnable {
            // Highlight all spawnable surface blocks
            surface.locations.forEach { location ->
                player.world.spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    location.clone().add(0.5, 1.0, 0.5),
                    2,
                    0.3, 0.3, 0.3,
                    0.0
                )
            }
        }, 0L, 10L) // Every 0.5 seconds

        // Create session
        val session = SpawnableSurfaceSession(player, gameId, surfaceId, taskId)
        activeSurfaceSessions[player.uniqueId] = session

        player.sendMessage("§aEditing spawnable surface: ${surface.name}")
        player.sendMessage("§7All placed blocks are highlighted with green particles")
    }

    /**
     * End spawnable surface mode
     */
    fun endSpawnableSurfaceMode(player: Player) {
        val session = activeSurfaceSessions.remove(player.uniqueId) ?: return

        // Cancel highlight task
        TowerDefMC.instance.server.scheduler.cancelTask(session.highlightTaskId)

        player.sendMessage("§aFinished editing spawnable surface")
    }

    /**
     * Check if player is in spawnable surface mode
     */
    fun isInSpawnableSurfaceMode(player: Player): Boolean {
        return activeSurfaceSessions.containsKey(player.uniqueId)
    }

    /**
     * Get the surface session for a player
     */
    fun getSpawnableSurfaceSession(player: Player): SpawnableSurfaceSession? {
        return activeSurfaceSessions[player.uniqueId]
    }

    /**
     * Handle spawnable surface block placement
     */
    fun placeSpawnableSurfaceBlock(player: Player, location: Location): Boolean {
        val session = activeSurfaceSessions[player.uniqueId] ?: return false

        val gameManager = GameRegistry.allGames[session.gameId]
        val surface = gameManager?.spawnableSurfaceManager?.getSurface(session.surfaceId)

        if (gameManager == null || surface == null) {
            player.sendMessage("§cError: Surface not found!")
            return false
        }

        // Check if block already exists at this location
        val blockExists = surface.locations.any { loc ->
            loc.world == location.world &&
            loc.blockX == location.blockX &&
            loc.blockY == location.blockY &&
            loc.blockZ == location.blockZ
        }

        if (blockExists) {
            player.sendMessage("§cA block already exists at this location!")
            return false
        }

        // Add location
        gameManager.spawnableSurfaceManager.addLocation(session.surfaceId, location)
        gameManager.saveGame()

        player.sendMessage("§aPlaced spawnable surface block")
        player.world.playSound(location, Sound.BLOCK_STONE_PLACE, 1f, 1f)
        player.world.spawnParticle(Particle.HAPPY_VILLAGER, location.clone().add(0.5, 1.0, 0.5), 10)

        return true
    }

    /**
     * Handle spawnable surface block removal
     */
    fun removeSpawnableSurfaceBlock(player: Player, location: Location): Boolean {
        val session = activeSurfaceSessions[player.uniqueId] ?: return false

        val gameManager = GameRegistry.allGames[session.gameId]
        val surface = gameManager?.spawnableSurfaceManager?.getSurface(session.surfaceId)

        if (gameManager == null || surface == null) {
            player.sendMessage("§cError: Surface not found!")
            return false
        }

        // Remove location
        val removed = gameManager.spawnableSurfaceManager.removeLocation(session.surfaceId, location)

        if (removed) {
            gameManager.saveGame()
            player.sendMessage("§aRemoved spawnable surface block")
            player.world.playSound(location, Sound.BLOCK_STONE_BREAK, 1f, 1f)
            player.world.spawnParticle(Particle.CLOUD, location.clone().add(0.5, 1.0, 0.5), 10)
        } else {
            player.sendMessage("§cNo spawnable surface block at this location!")
        }

        return removed
    }

    /**
     * Start chat input mode for a player
     */
    fun startChatInput(player: Player, callback: (String) -> Unit) {
        chatInputSessions[player.uniqueId] = ChatInputSession(player, callback)
    }

    /**
     * Handle chat input
     */
    fun handleChatInput(player: Player, message: String): Boolean {
        val session = chatInputSessions.remove(player.uniqueId) ?: return false
        session.callback(message)
        return true
    }

    /**
     * Check if player is in chat input mode
     */
    fun isInChatInputMode(player: Player): Boolean {
        return chatInputSessions.containsKey(player.uniqueId)
    }
}
