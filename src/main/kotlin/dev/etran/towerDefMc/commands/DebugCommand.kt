package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.utils.DebugLogger
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object DebugCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            // Show current debug status
            sender.sendMessage("§6§l=== Tower Defense Debug Status ===")
            val status = DebugLogger.getStatus()
            status.forEach { (category, enabled) ->
                val statusText = if (enabled) "§a✓ ON" else "§c✗ OFF"
                sender.sendMessage("  §7${category.replaceFirstChar { it.uppercase() }}: $statusText")
            }
            sender.sendMessage("")
            sender.sendMessage("§7Usage: /tddebug <category|all> [on|off|toggle]")
            sender.sendMessage("§7Categories: master, waves, enemies, towers, game, stats, paths")
            return true
        }

        val category = args[0]
        val action = args.getOrNull(1)?.lowercase()

        when (action) {
            "on" -> {
                if (category.equals("all", ignoreCase = true)) {
                    DebugLogger.masterDebug = true
                    DebugLogger.saveConfig()
                    sender.sendMessage("§a✓ All debug logging enabled!")
                } else {
                    val result = setCategoryState(category, true)
                    if (result) {
                        sender.sendMessage("§a✓ Debug logging enabled for: §e$category")
                    } else {
                        sender.sendMessage("§cInvalid category: $category")
                    }
                }
            }

            "off" -> {
                if (category.equals("all", ignoreCase = true)) {
                    DebugLogger.masterDebug = false
                    DebugLogger.saveConfig()
                    sender.sendMessage("§c✗ All debug logging disabled!")
                } else {
                    val result = setCategoryState(category, false)
                    if (result) {
                        sender.sendMessage("§c✗ Debug logging disabled for: §e$category")
                    } else {
                        sender.sendMessage("§cInvalid category: $category")
                    }
                }
            }

            "toggle", null -> {
                if (category.equals("all", ignoreCase = true)) {
                    DebugLogger.masterDebug = !DebugLogger.masterDebug
                    DebugLogger.saveConfig()
                    val status = if (DebugLogger.masterDebug) "§aenabled" else "§cdisabled"
                    sender.sendMessage("§6Master debug logging $status!")
                } else {
                    val newState = DebugLogger.toggle(category)
                    val status = if (newState) "§aenabled" else "§cdisabled"
                    sender.sendMessage("§6Debug logging for §e$category §6is now $status!")
                }
            }

            else -> {
                sender.sendMessage("§cInvalid action: $action")
                sender.sendMessage("§7Usage: /tddebug <category> [on|off|toggle]")
            }
        }

        return true
    }

    private fun setCategoryState(category: String, enabled: Boolean): Boolean {
        return when (category.lowercase()) {
            "master" -> {
                DebugLogger.masterDebug = enabled
                DebugLogger.saveConfig()
                true
            }

            "wave", "waves" -> {
                DebugLogger.waveDebug = enabled
                DebugLogger.saveConfig()
                true
            }

            "enemy", "enemies" -> {
                DebugLogger.enemyDebug = enabled
                DebugLogger.saveConfig()
                true
            }

            "tower", "towers" -> {
                DebugLogger.towerDebug = enabled
                DebugLogger.saveConfig()
                true
            }

            "game" -> {
                DebugLogger.gameDebug = enabled
                DebugLogger.saveConfig()
                true
            }

            "stats" -> {
                DebugLogger.statsDebug = enabled
                DebugLogger.saveConfig()
                true
            }

            "path", "paths" -> {
                DebugLogger.pathDebug = enabled
                DebugLogger.saveConfig()
                true
            }

            else -> false
        }
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> {
                listOf("all", "master", "waves", "enemies", "towers", "game", "stats", "paths").filter {
                    it.startsWith(
                        args[0], ignoreCase = true
                    )
                }
            }

            2 -> {
                listOf("on", "off", "toggle").filter { it.startsWith(args[1], ignoreCase = true) }
            }

            else -> emptyList()
        }
    }
}

