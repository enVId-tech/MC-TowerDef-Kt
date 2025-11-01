package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object StartGame : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /startgame <gameId> [player1] [player2] ...")
            sender.sendMessage("§7Example: /startgame 1")
            sender.sendMessage("§7Example: /startgame 1 Steve Alex")
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

        if (gameManager.isGameRunning) {
            sender.sendMessage("§cGame $gameId is already running!")
            return true
        }

        // Collect players to add
        val playersToAdd = mutableListOf<Player>()

        // If no specific players mentioned, and sender is a player, add them
        if (args.size == 1 && sender is Player) {
            playersToAdd.add(sender)
        } else if (args.size > 1) {
            // Add specified players
            for (i in 1 until args.size) {
                val player = Bukkit.getPlayer(args[i])
                if (player != null) {
                    playersToAdd.add(player)
                } else {
                    sender.sendMessage("§eWarning: Player '${args[i]}' not found or not online, skipping...")
                }
            }
        }

        // Validate that we have at least one player
        if (playersToAdd.isEmpty()) {
            sender.sendMessage("§cNo valid players to start the game with!")
            sender.sendMessage("§7Usage: /startgame <gameId> [player1] [player2] ...")
            return true
        }

        // Check if any player is already in another game
        val conflicts = mutableListOf<String>()
        for (player in playersToAdd) {
            val existingGame = GameRegistry.getGameByPlayer(player.uniqueId)
            if (existingGame != null) {
                conflicts.add("${player.name} (already in game ${existingGame.gameId})")
            }
        }

        if (conflicts.isNotEmpty()) {
            sender.sendMessage("§cCannot start game - some players are already in other games:")
            conflicts.forEach { sender.sendMessage("§c  - $it") }
            return true
        }

        // Add all players to the game
        val playerUUIDs = playersToAdd.map { it.uniqueId }
        playerUUIDs.forEach { gameManager.addPlayer(it) }

        // Start the game
        gameManager.startGame(playerUUIDs)

        // Send confirmation messages
        sender.sendMessage("§aGame $gameId (${gameManager.config.name}) has been started!")
        sender.sendMessage("§aPlayers: ${playersToAdd.joinToString(", ") { it.name }}")

        playersToAdd.forEach { player ->
            player.sendMessage("§a§lGame Started!")
            player.sendMessage("§aYou are now playing in game $gameId (${gameManager.config.name})")
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
            else -> {
                // Tab complete online player names for all subsequent arguments
                Bukkit.getOnlinePlayers().map { it.name }.filter {
                    it.startsWith(args[args.size - 1], ignoreCase = true)
                }
            }
        }
    }
}

