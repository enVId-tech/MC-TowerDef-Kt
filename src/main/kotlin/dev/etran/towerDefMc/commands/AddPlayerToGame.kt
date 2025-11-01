package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object AddPlayerToGame : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /addplayer <gameId> <playerName>")
            sender.sendMessage("§7Example: /addplayer 1 Steve")
            sender.sendMessage("§7Available games: ${GameRegistry.allGames.keys.joinToString(", ")}")
            return true
        }

        val gameId = args[0].toIntOrNull()
        if (gameId == null) {
            sender.sendMessage("§cInvalid game ID!")
            return true
        }

        val gameManager = GameRegistry.allGames[gameId]
        if (gameManager == null) {
            sender.sendMessage("§cGame $gameId not found!")
            sender.sendMessage("§7Available games: ${GameRegistry.allGames.keys.joinToString(", ")}")
            return true
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage("§cPlayer '${args[1]}' not found or not online!")
            return true
        }

        // Check if player is already in ANY game
        val existingGame = GameRegistry.getGameByPlayer(targetPlayer.uniqueId)
        if (existingGame != null) {
            sender.sendMessage("§cPlayer ${targetPlayer.name} is already in game ${existingGame.gameId}!")
            sender.sendMessage("§7They must leave that game first before joining another.")
            return true
        }

        // Add player to the game
        gameManager.addPlayer(targetPlayer.uniqueId)

        sender.sendMessage("§aSuccessfully added ${targetPlayer.name} to game $gameId (${gameManager.config.name})")
        targetPlayer.sendMessage("§aYou have been added to game $gameId (${gameManager.config.name})!")

        if (gameManager.isGameRunning) {
            targetPlayer.sendMessage("§7The game is currently running. You can place towers and participate!")
        } else {
            targetPlayer.sendMessage("§7The game has not started yet.")
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
