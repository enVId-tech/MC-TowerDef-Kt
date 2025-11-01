package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.PlayerStatsManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID

object GameEndingSequence {
    private lateinit var plugin: TowerDefMC

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
    }

    /**
     * Display the victory sequence to all players in the game
     */
    fun displayVictorySequence(gameId: Int, gameName: String, playerUUIDs: Set<UUID>, finalWave: Int) {
        val players = playerUUIDs.mapNotNull { Bukkit.getPlayer(it) }

        // Display victory title with celebration effects
        players.forEach { player ->
            // Victory title
            val title = Title.title(
                Component.text("VICTORY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("All waves completed!", NamedTextColor.YELLOW),
                Title.Times.times(
                    Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000)
                )
            )
            player.showTitle(title)

            // Victory sounds
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)

            // Delayed celebration sound
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f)
            }, 20L)
        }

        // Display statistics after a delay
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            displayGameStatistics(gameId, gameName, finalWave, players, true)
        }, 100L) // 5 seconds delay
    }

    /**
     * Display the defeat sequence to all players in the game
     */
    fun displayDefeatSequence(
        gameId: Int, gameName: String, playerUUIDs: Set<UUID>, currentWave: Int, remainingHealth: Int
    ) {
        val players = playerUUIDs.mapNotNull { Bukkit.getPlayer(it) }

        // Display defeat title
        players.forEach { player ->
            val subtitle = if (remainingHealth <= 0) {
                Component.text("The enemies made it to the end!", NamedTextColor.RED)
            } else {
                Component.text("Game stopped", NamedTextColor.GRAY)
            }

            val title = Title.title(
                Component.text("DEFEAT", NamedTextColor.DARK_RED, TextDecoration.BOLD), subtitle, Title.Times.times(
                    Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000)
                )
            )
            player.showTitle(title)

            // Defeat sound
            player.playSound(player.location, Sound.ENTITY_WITHER_DEATH, 0.7f, 0.8f)
        }

        // Display statistics after a delay
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            displayGameStatistics(gameId, gameName, currentWave, players, false)
        }, 100L) // 5 seconds delay
    }

    /**
     * Display game statistics in chat for all players
     */
    private fun displayGameStatistics(
        gameId: Int, gameName: String, finalWave: Int, players: List<Player>, victory: Boolean
    ) {
        val allStats = PlayerStatsManager.getAllPlayerStats(gameId)

        players.forEach { player ->
            val stats = allStats[player.uniqueId]

            // Header
            player.sendMessage("")
            player.sendMessage("§8§m                                                    ")
            if (victory) {
                player.sendMessage("§6§l✦ VICTORY - GAME STATISTICS ✦")
            } else {
                player.sendMessage("§c§l✦ DEFEAT - GAME STATISTICS ✦")
            }
            player.sendMessage("§8§m                                                    ")
            player.sendMessage("")
            player.sendMessage("§7Game: §f$gameName")
            player.sendMessage("§7Waves Survived: §f$finalWave")
            player.sendMessage("")

            // Personal statistics
            if (stats != null) {
                player.sendMessage("§e§l⚔ YOUR PERFORMANCE:")
                player.sendMessage("  §7Cash Remaining: §a$${stats.cash}")
                player.sendMessage("  §7Enemies Killed: §e${stats.kills}")
                player.sendMessage("  §7Damage Dealt: §c${String.format("%.1f", stats.damageDealt)}")
                player.sendMessage("  §7Towers Placed: §b${stats.towersPlaced}")
                player.sendMessage("  §7Towers Upgraded: §d${stats.towersUpgraded}")
                player.sendMessage("  §7Waves Completed: §6${stats.wavesCompleted}")
            }

            // Team statistics if multiple players
            if (players.size > 1) {
                player.sendMessage("")
                player.sendMessage("§6§l⚔ TEAM PERFORMANCE:")

                val teamKills = allStats.values.sumOf { it.kills }
                val teamDamage = allStats.values.sumOf { it.damageDealt }
                val teamTowers = allStats.values.sumOf { it.towersPlaced }
                val teamUpgrades = allStats.values.sumOf { it.towersUpgraded }

                player.sendMessage("  §7Total Kills: §e$teamKills")
                player.sendMessage("  §7Total Damage: §c${String.format("%.1f", teamDamage)}")
                player.sendMessage("  §7Total Towers: §b$teamTowers")
                player.sendMessage("  §7Total Upgrades: §d$teamUpgrades")

                // Top performers
                player.sendMessage("")
                player.sendMessage("§6§l★ TOP PERFORMERS:")

                // Most kills
                val topKiller = allStats.maxByOrNull { it.value.kills }
                if (topKiller != null && topKiller.value.kills > 0) {
                    val killerName = Bukkit.getPlayer(topKiller.key)?.name ?: "Unknown"
                    player.sendMessage("  §7Most Kills: §e$killerName §7(${topKiller.value.kills})")
                }

                // Most damage
                val topDamager = allStats.maxByOrNull { it.value.damageDealt }
                if (topDamager != null && topDamager.value.damageDealt > 0) {
                    val damagerName = Bukkit.getPlayer(topDamager.key)?.name ?: "Unknown"
                    player.sendMessage(
                        "  §7Most Damage: §c$damagerName §7(${
                            String.format(
                                "%.1f", topDamager.value.damageDealt
                            )
                        })"
                    )
                }

                // Most towers
                val topBuilder = allStats.maxByOrNull { it.value.towersPlaced }
                if (topBuilder != null && topBuilder.value.towersPlaced > 0) {
                    val builderName = Bukkit.getPlayer(topBuilder.key)?.name ?: "Unknown"
                    player.sendMessage("  §7Master Builder: §b$builderName §7(${topBuilder.value.towersPlaced} towers)")
                }
            }

            player.sendMessage("")
            player.sendMessage("§8§m                                                    ")
            player.sendMessage("")

            // Victory/Defeat message
            if (victory) {
                player.sendMessage("§a§l✓ Congratulations on your victory!")
            } else {
                player.sendMessage("§c§l✗ Better luck next time!")
            }
        }

        // Play final sound after stats display
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            players.forEach { player ->
                if (victory) {
                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f)
                } else {
                    player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f)
                }
            }
        }, 20L)
    }
}

