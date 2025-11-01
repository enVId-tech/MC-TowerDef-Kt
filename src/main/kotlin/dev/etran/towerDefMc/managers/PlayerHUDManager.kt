package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player

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
        val message = Component.text()
            .append(Component.text("💰 ", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("${stats.cash}", NamedTextColor.YELLOW))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
            .append(Component.text("⚔ ", NamedTextColor.RED))
            .append(Component.text("${stats.kills}", NamedTextColor.WHITE))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
            .append(Component.text("🗼 ", NamedTextColor.AQUA))
            .append(Component.text("${stats.towersPlaced}", NamedTextColor.WHITE))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
            .append(Component.text("📊 ", NamedTextColor.GREEN))
            .append(Component.text("W${stats.wavesCompleted}", NamedTextColor.WHITE))
            .build()

        player.sendActionBar(message)
    }
}

