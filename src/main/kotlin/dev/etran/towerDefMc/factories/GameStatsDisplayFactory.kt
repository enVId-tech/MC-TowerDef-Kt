package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameManager
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Factory for creating game stats display armor stands
 */
object GameStatsDisplayFactory {

    /**
     * Creates a game stats display item
     */
    fun createGameStatsItem(amount: Int = 1): ItemStack {
        val item = ItemStack(Material.LECTERN, amount)
        val meta = item.itemMeta ?: return item

        meta.displayName(Component.text("§b§lGame Stats Display"))
        meta.lore(listOf(
            Component.text("§7Place this to view game statistics"),
            Component.text("§7Shows: Health, Wave, Players, etc."),
            Component.text("§e"),
            Component.text("§eRight-click to place")
        ))

        meta.persistentDataContainer.set(
            TowerDefMC.GAME_ITEMS,
            PersistentDataType.STRING,
            "Game_Stats_Display"
        )

        item.itemMeta = meta
        return item
    }

    /**
     * Places a game stats display armor stand at a location
     */
    fun placeGameStatsDisplay(location: Location, gameId: Int): ArmorStand {
        val world = location.world
        val armorStand = world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand

        armorStand.isVisible = false
        armorStand.setGravity(false)
        armorStand.isInvulnerable = true
        armorStand.isCustomNameVisible = true
        armorStand.persistentDataContainer.set(
            TowerDefMC.ELEMENT_TYPES,
            PersistentDataType.STRING,
            "GameStatsDisplay"
        )
        armorStand.persistentDataContainer.set(
            TowerDefMC.GAME_ID_KEY,
            PersistentDataType.INTEGER,
            gameId
        )

        // Start updating the stats display
        startGameStatsUpdateTask(armorStand, gameId)

        return armorStand
    }

    /**
     * Starts a task to continuously update the game stats display
     */
    private fun startGameStatsUpdateTask(armorStand: ArmorStand, gameId: Int) {
        val taskId = Bukkit.getScheduler().runTaskTimer(TowerDefMC.instance, Runnable {
            if (!armorStand.isValid || armorStand.isDead) {
                return@Runnable
            }

            val game = GameRegistry.allGames[gameId]
            if (game != null) {
                val displayText = buildGameStatsDisplay(game)
                armorStand.customName(Component.text(displayText))
            }
        }, 0L, 20L) // Update every second

        // Store task ID to cancel later if needed
        armorStand.persistentDataContainer.set(
            TowerDefMC.createKey("updateTaskId"),
            PersistentDataType.INTEGER,
            taskId.taskId
        )
    }

    /**
     * Builds the display text for game stats
     */
    private fun buildGameStatsDisplay(game: GameManager): String {
        val allPlayerStats = PlayerStatsManager.getAllPlayerStats(game.gameId)
        val playerCount = allPlayerStats.size

        // Calculate total stats
        val totalKills = allPlayerStats.values.sumOf { it.kills }
        val totalTowers = allPlayerStats.values.sumOf { it.towersPlaced }
        val totalDamage = allPlayerStats.values.sumOf { it.damageDealt }

        // Get active path count
        val activePaths = game.pathManager.getAllPaths().count { it.isVisible }

        return buildString {
            append("§b§l╔════════════════════╗\n")
            append("§b§l║  §6§lGAME STATISTICS  §b§l║\n")
            append("§b§l╠════════════════════╣\n")
            append("§b§l║ §e${game.config.name}\n")
            append("§b§l║ §7Wave: §f${game.waveManager.currentWave}§7/§f${game.config.waves.size}\n")
            append("§b§l║ §7Health: §c${game.currentHealth}§7/§f${game.config.maxHealth}\n")
            append("§b§l║ §7Players: §a$playerCount\n")
            append("§b§l║ §7Active Paths: §e$activePaths\n")
            append("§b§l╠════════════════════╣\n")
            append("§b§l║ §6§lTEAM STATS\n")
            append("§b§l║ §7Total Kills: §f$totalKills\n")
            append("§b§l║ §7Total Towers: §f$totalTowers\n")
            append("§b§l║ §7Total Damage: §f${"%.0f".format(totalDamage)}\n")
            append("§b§l╚════════════════════╝")
        }
    }

    /**
     * Updates all game stats displays for a specific game
     */
    fun updateAllGameStatsDisplays(gameId: Int) {
        Bukkit.getWorlds().forEach { world ->
            world.entities.filterIsInstance<ArmorStand>().forEach { stand ->
                val elementType = stand.persistentDataContainer.get(
                    TowerDefMC.ELEMENT_TYPES,
                    PersistentDataType.STRING
                )
                val standGameId = stand.persistentDataContainer.get(
                    TowerDefMC.GAME_ID_KEY,
                    PersistentDataType.INTEGER
                )

                if (elementType == "GameStatsDisplay" && standGameId == gameId) {
                    val game = GameRegistry.allGames[gameId]
                    if (game != null) {
                        val displayText = buildGameStatsDisplay(game)
                        stand.customName(Component.text(displayText))
                    }
                }
            }
        }
    }
}
