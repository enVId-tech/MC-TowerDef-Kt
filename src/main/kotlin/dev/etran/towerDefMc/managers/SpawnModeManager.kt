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
        START_POINT,
        END_POINT,
        CHECKPOINT
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

    private val activeSessions = mutableMapOf<UUID, SpawnModeSession>()

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
        meta.lore(listOf(
            Component.text("§7Right-click to place a ${getSpawnTypeName(spawnType).lowercase()}"),
            Component.text("§7Type anything in chat to exit")
        ))

        // Mark as spawn item
        val pdc = meta.persistentDataContainer
        pdc.set(
            NamespacedKey(TowerDefMC.instance, "spawn_item"),
            PersistentDataType.STRING,
            spawnType.name
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
}
