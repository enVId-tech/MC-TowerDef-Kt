package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object JoinGame : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Parse arguments - support both player-initiated and command block usage
        val (gameId, targetPlayer) = when {
            // Player using command: /joingame <gameId>
            args.size == 1 && sender is Player -> {
                val id = args[0].toIntOrNull()
                if (id == null) {
                    sender.sendMessage("§cInvalid game ID!")
                    return true
                }
                Pair(id, sender)
            }
            // Command block or admin: /joingame <gameId> <playerName>
            args.size == 2 -> {
                val id = args[0].toIntOrNull()
                if (id == null) {
                    sender.sendMessage("§cInvalid game ID!")
                    return true
                }
                val player = Bukkit.getPlayer(args[1])
                if (player == null) {
                    sender.sendMessage("§cPlayer '${args[1]}' not found or not online!")
                    return true
                }
                Pair(id, player)
            }
            else -> {
                sender.sendMessage("§cUsage: /joingame <gameId> [playerName]")
                sender.sendMessage("§7Example: /joingame 1")
                sender.sendMessage("§7Example: /joingame 1 Steve")
                sender.sendMessage("§7Available games: ${GameRegistry.allGames.keys.joinToString(", ")}")
                return true
            }
        }

        val gameManager = GameRegistry.allGames[gameId]
        if (gameManager == null) {
            sender.sendMessage("§cGame $gameId not found!")
            sender.sendMessage("§7Available games: ${GameRegistry.allGames.keys.joinToString(", ")}")
            return true
        }

        // Check if player is already in this game
        if (gameManager.hasPlayer(targetPlayer.uniqueId)) {
            sender.sendMessage("§c${targetPlayer.name} is already in game $gameId!")
            return true
        }

        // Check if player is already in another game
        val existingGame = GameRegistry.getGameByPlayer(targetPlayer.uniqueId)
        if (existingGame != null) {
            sender.sendMessage("§c${targetPlayer.name} is already in game ${existingGame.gameId}!")
            sender.sendMessage("§7They must leave that game first before joining another.")
            return true
        }

        // Add player to the game
        gameManager.addPlayer(targetPlayer.uniqueId)

        // Send confirmation messages
        if (sender != targetPlayer) {
            sender.sendMessage("§aSuccessfully added ${targetPlayer.name} to game $gameId (${gameManager.config.name})")
        }

        targetPlayer.sendMessage("§a§lJoined Game!")
        targetPlayer.sendMessage("§aYou have joined game $gameId (${gameManager.config.name})")

        if (gameManager.isGameRunning) {
            targetPlayer.sendMessage("§7The game is currently running. You can place towers and participate!")
        } else {
            targetPlayer.sendMessage("§7The game has not started yet. Waiting for game to begin...")
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> {
                // Tab complete game IDs
                GameRegistry.allGames.keys.map { it.toString() }.filter { it.startsWith(args[0], ignoreCase = true) }
            }
            2 -> {
                // Tab complete online player names
                Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }
            }
            else -> emptyList()
        }
    }
}

