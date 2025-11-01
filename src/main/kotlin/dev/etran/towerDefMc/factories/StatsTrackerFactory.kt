package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Factory for creating and managing statistics tracker items
 */
object StatsTrackerFactory {

    /**
     * Creates a new stats tracker item
     */
    fun newStatsTracker(amount: Int = 1): ItemStack {
        val item = ItemStack(Material.OBSERVER, amount)
        val meta = item.itemMeta ?: return item

        meta.displayName(Component.text("§6§lStats Tracker"))
        meta.lore(
            listOf(
                Component.text("§7Place this to view your"),
                Component.text("§7in-game statistics"),
                Component.text("§e"),
                Component.text("§eRight-click to place")
            )
        )

        meta.persistentDataContainer.set(
            TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Stats_Tracker"
        )

        item.itemMeta = meta
        return item
    }

    /**
     * Handles placement of stats tracker
     */
    fun placeStatsTracker(event: PlayerInteractEvent) {
        event.isCancelled = true

        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val player = event.player

        // Check if player is in a game
        val game = GameRegistry.getGameByPlayer(player.uniqueId)
        if (game == null) {
            player.sendMessage("§cYou must be in a game to place a stats tracker!")
            return
        }

        // Check if location is clear
        if (location.getNearbyEntities(0.5, 1.0, 0.5).count() >= 1) {
            player.sendMessage("§cYou cannot place a stats tracker here!")
            return
        }

        // Create armor stand for stats display
        val world = location.world
        val armorStand = world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand

        armorStand.isVisible = false
        armorStand.setGravity(false)
        armorStand.isInvulnerable = true
        armorStand.isCustomNameVisible = true
        armorStand.persistentDataContainer.set(
            TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "StatsTracker"
        )
        armorStand.persistentDataContainer.set(
            TowerDefMC.createKey("trackerOwner"), PersistentDataType.STRING, player.uniqueId.toString()
        )
        armorStand.persistentDataContainer.set(
            TowerDefMC.GAME_ID_KEY, PersistentDataType.INTEGER, game.gameId
        )

        // Start updating the stats display
        startStatsUpdateTask(armorStand, player, game.gameId)

        // Remove item from inventory if not in creative
        if (player.gameMode != org.bukkit.GameMode.CREATIVE && player.gameMode != org.bukkit.GameMode.SPECTATOR) {
            player.inventory.itemInMainHand.amount -= 1
        }

        player.sendMessage("§aStats tracker placed! It will show your personal statistics.")
    }

    /**
     * Starts a task to continuously update the stats display
     */
    private fun startStatsUpdateTask(armorStand: ArmorStand, player: Player, gameId: Int) {
        val taskId = Bukkit.getScheduler().runTaskTimer(TowerDefMC.instance, Runnable {
            if (!armorStand.isValid || armorStand.isDead) {
                return@Runnable
            }

            val stats = PlayerStatsManager.getPlayerStats(gameId, player.uniqueId)
            if (stats != null) {
                val displayText = buildString {
                    append("§6§l${player.name}'s Stats\n")
                    append("§e§lCash: §a${stats.cash}\n")
                    append("§7Kills: §f${stats.kills}\n")
                    append("§7Towers: §f${stats.towersPlaced}\n")
                    append("§7Upgrades: §f${stats.towersUpgraded}\n")
                    append("§7Damage: §f${"%.1f".format(stats.damageDealt)}\n")
                    append("§7Waves: §f${stats.wavesCompleted}")
                }
                armorStand.customName(Component.text(displayText))
            }
        }, 0L, 20L) // Update every second

        // Store task ID to cancel later if needed
        armorStand.persistentDataContainer.set(
            TowerDefMC.createKey("updateTaskId"), PersistentDataType.INTEGER, taskId.taskId
        )
    }
}

