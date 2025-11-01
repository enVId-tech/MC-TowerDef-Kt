package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import net.kyori.adventure.title.Title
import java.time.Duration

/**
 * Manages the player HUD display showing cash and stats
 */
object PlayerHUDManager {
    private var taskId: Int? = null

    /**
     * Start the HUD update task
     */
    fun startHUDTask() {
        taskId = Bukkit.getScheduler().runTaskTimer(TowerDefMC.instance, Runnable {
            updateAllPlayerHUDs()
        }, 0L, 10L).taskId // Update every 0.5 seconds
    }

    /**
     * Stop the HUD update task
     */
    fun stopHUDTask() {
        taskId?.let { Bukkit.getScheduler().cancelTask(it) }
    }

    /**
     * Update HUD for all players in active games
     */
    private fun updateAllPlayerHUDs() {
        Bukkit.getOnlinePlayers().forEach { player ->
            updatePlayerHUD(player)
        }
    }

    /**
     * Update HUD for a specific player
     */
    fun updatePlayerHUD(player: Player) {
        val game = GameRegistry.getGameByPlayer(player.uniqueId) ?: return
        val stats = PlayerStatsManager.getPlayerStats(game.gameId, player.uniqueId) ?: return

        // Create action bar message with cash and key stats
        val message = Component.text().append(Component.text("💰 ", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("${stats.cash}", NamedTextColor.YELLOW))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY)).append(Component.text("⚔ ", NamedTextColor.RED))
            .append(Component.text("${stats.kills}", NamedTextColor.WHITE))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY)).append(Component.text("🗼 ", NamedTextColor.AQUA))
            .append(Component.text("${stats.towersPlaced}", NamedTextColor.WHITE))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY)).append(Component.text("📊 ", NamedTextColor.GREEN))
            .append(Component.text("W${stats.wavesCompleted}", NamedTextColor.WHITE)).build()

        player.sendActionBar(message)

        // Also display game info as subtitle periodically (every 10 seconds / 200 ticks)
        if ((Bukkit.getCurrentTick() % 200).toLong() == 0L) {
//            displayGameInfo(player, game)
        }
    }

    /**
     * Display game information to a player using title/subtitle
     */
    private fun displayGameInfo(player: Player, game: GameManager) {
        val allPlayerStats = PlayerStatsManager.getAllPlayerStats(game.gameId)
        val playerCount = allPlayerStats.size
        val activePaths = game.pathManager.getAllPaths().count { it.isVisible }

        val subtitle = Component.text().append(Component.text("❤ ", NamedTextColor.RED))
            .append(Component.text("${game.currentHealth}/${game.config.maxHealth}", NamedTextColor.WHITE))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY)).append(Component.text("🌊 ", NamedTextColor.AQUA))
            .append(Component.text("${game.waveManager.currentWave}/${game.config.waves.size}", NamedTextColor.WHITE))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY)).append(Component.text("👥 ", NamedTextColor.GREEN))
            .append(Component.text("$playerCount", NamedTextColor.WHITE))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY)).append(Component.text("🛤 ", NamedTextColor.YELLOW))
            .append(Component.text("$activePaths", NamedTextColor.WHITE)).build()

        val title = Title.title(
            Component.empty(), subtitle, Title.Times.times(
                Duration.ofMillis(250), Duration.ofSeconds(3), Duration.ofMillis(500)
            )
        )

        player.showTitle(title)
    }

    /**
     * Send a game status update to all players in a game
     */
    fun broadcastGameStatus(gameId: Int) {
        val game = GameRegistry.allGames[gameId] ?: return
        val allPlayerStats = PlayerStatsManager.getAllPlayerStats(gameId)

        allPlayerStats.keys.forEach { playerUUID ->
            val player = Bukkit.getPlayer(playerUUID)
            if (player != null && player.isOnline) {
//                displayGameInfo(player, game)
            }
        }
    }
}
