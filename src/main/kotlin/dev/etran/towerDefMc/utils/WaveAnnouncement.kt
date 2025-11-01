package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.EnemySpawnCommand
import dev.etran.towerDefMc.data.WaveData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID

object WaveAnnouncement {
    private lateinit var plugin: TowerDefMC

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
    }

    /**
     * Display wave start announcement to all players in the game
     */
    fun announceWaveStart(gameId: Int, waveNumber: Int, waveData: WaveData, totalWaves: Int, playerUUIDs: Set<UUID>) {
        val players = playerUUIDs.mapNotNull { Bukkit.getPlayer(it) }

        // Display title
        players.forEach { player ->
            // Wave title
            val title = Title.title(
                Component.text("WAVE $waveNumber", NamedTextColor.RED, TextDecoration.BOLD),
                Component.text(waveData.name, NamedTextColor.YELLOW),
                Title.Times.times(
                    Duration.ofMillis(500), Duration.ofMillis(2500), Duration.ofMillis(500)
                )
            )
            player.showTitle(title)

            // Wave start sound
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f)

            // Delayed warning sound
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.2f)
            }, 10L)
        }

        // Display wave details in chat after a brief delay
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            displayWaveDetails(waveNumber, waveData, totalWaves, players)
        }, 40L) // 2 second delay
    }

    /**
     * Display detailed wave information in chat
     */
    private fun displayWaveDetails(waveNumber: Int, waveData: WaveData, totalWaves: Int, players: List<Player>) {
        players.forEach { player ->
            // Header
            player.sendMessage("")
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            player.sendMessage("§c§l⚔ WAVE $waveNumber / $totalWaves §8- §e${waveData.name}")
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            player.sendMessage("")

            // Wave details
            if (waveData.cashGiven > 0) {
                player.sendMessage("  §6💰 Reward: §e${waveData.cashGiven} cash")
            }

            if (waveData.waveHealth != null) {
                player.sendMessage("  §c❤ Enemy Health: §f${String.format("%.1f", waveData.waveHealth)}")
            }

            // Analyze enemies in the wave
            val enemyCounts = mutableMapOf<String, Int>()
            var totalEnemies = 0

            waveData.sequence.forEach { command ->
                if (command is EnemySpawnCommand) {
                    command.enemies.forEach { (enemyType, count) ->
                        enemyCounts[enemyType] = enemyCounts.getOrDefault(enemyType, 0) + count
                        totalEnemies += count
                    }
                }
            }

            if (totalEnemies > 0) {
                player.sendMessage("")
                player.sendMessage("  §7👹 Enemies:")
                enemyCounts.forEach { (enemyType, count) ->
                    val displayName = formatEnemyName(enemyType)
                    player.sendMessage("    §8▪ §f$displayName §7x$count")
                }
                player.sendMessage("")
                player.sendMessage("  §7Total Enemies: §e$totalEnemies")
            }

            // Wave timing info
            if (waveData.minTime > 0 || waveData.maxTime < 999) {
                player.sendMessage("")
                if (waveData.minTime > 0 && waveData.maxTime < 999) {
                    player.sendMessage(
                        "  §7⏱ Duration: §f${String.format("%.1f", waveData.minTime)}s - ${
                            String.format(
                                "%.1f", waveData.maxTime
                            )
                        }s"
                    )
                } else if (waveData.maxTime < 999) {
                    player.sendMessage("  §7⏱ Max Duration: §f${String.format("%.1f", waveData.maxTime)}s")
                }
            }

            player.sendMessage("")
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            player.sendMessage("§e§l⚡ Prepare your defenses!")
            player.sendMessage("")
        }
    }

    /**
     * Display wave completion announcement
     */
    fun announceWaveComplete(gameId: Int, waveNumber: Int, cashReward: Int, playerUUIDs: Set<UUID>) {
        val players = playerUUIDs.mapNotNull { Bukkit.getPlayer(it) }

        players.forEach { player ->
            // Wave complete title
            val title = Title.title(
                Component.text("WAVE COMPLETE!", NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.text("+ $cashReward cash", NamedTextColor.GOLD),
                Title.Times.times(
                    Duration.ofMillis(300), Duration.ofMillis(1500), Duration.ofMillis(300)
                )
            )
            player.showTitle(title)

            // Success sound
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f)
        }

        // Display completion message in chat
        players.forEach { player ->
            player.sendMessage("")
            player.sendMessage("§a§l✓ Wave $waveNumber completed! §8+§e$cashReward cash")
            player.sendMessage("")
        }
    }

    /**
     * Display preparation announcement between waves
     */
    fun announcePreparation(playerUUIDs: Set<UUID>, secondsUntilNext: Int) {
        val players = playerUUIDs.mapNotNull { Bukkit.getPlayer(it) }

        players.forEach { player ->
            // Preparation subtitle
            val title = Title.title(
                Component.text(""),
                Component.text("Next wave in ${secondsUntilNext}s...", NamedTextColor.GRAY),
                Title.Times.times(
                    Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(500)
                )
            )
            player.showTitle(title)
        }
    }

    /**
     * Format enemy type names for display
     */
    private fun formatEnemyName(enemyType: String): String {
        return when (enemyType.uppercase()) {
            "ZOMBIE" -> "§2Zombie"
            "SKELETON" -> "§7Skeleton"
            "SPIDER" -> "§8Spider"
            "CREEPER" -> "§aCreeper"
            "ENDERMAN" -> "§5Enderman"
            "WITCH" -> "§dWitch"
            "BLAZE" -> "§6Blaze"
            "SLIME" -> "§aSlime"
            else -> "§f${enemyType.lowercase().replaceFirstChar { it.uppercase() }}"
        }
    }
}
