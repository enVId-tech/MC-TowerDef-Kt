package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object LeaveGame : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Parse arguments - support both player-initiated and command block usage
        val targetPlayer: Player = when {
            // Player using command: /leavegame
            args.isEmpty() && sender is Player -> {
                sender
            }
            // Command block or admin: /leavegame <playerName>
            args.size == 1 -> {
                val player = Bukkit.getPlayer(args[0])
                if (player == null) {
                    sender.sendMessage("§cPlayer '${args[0]}' not found or not online!")
                    return true
                }
                player
            }
            else -> {
                sender.sendMessage("§cUsage: /leavegame [playerName]")
                sender.sendMessage("§7Example: /leavegame")
                sender.sendMessage("§7Example: /leavegame Steve")
                return true
            }
        }

        // Find which game the player is in
        val gameManager = GameRegistry.getGameByPlayer(targetPlayer.uniqueId)
        if (gameManager == null) {
            if (sender == targetPlayer) {
                sender.sendMessage("§cYou are not in any game!")
            } else {
                sender.sendMessage("§c${targetPlayer.name} is not in any game!")
            }
            return true
        }

        val gameId = gameManager.gameId
        val gameName = gameManager.config.name

        // Remove player from the game
        gameManager.removePlayer(targetPlayer.uniqueId)

        // Send confirmation messages
        if (sender != targetPlayer) {
            sender.sendMessage("§aSuccessfully removed ${targetPlayer.name} from game $gameId ($gameName)")
        }

        targetPlayer.sendMessage("§e§lLeft Game!")
        targetPlayer.sendMessage("§eYou have left game $gameId ($gameName)")

        // If the game is running and now has no players, optionally notify
        if (gameManager.isGameRunning && gameManager.playerCount == 0) {
            sender.sendMessage("§7Note: Game $gameId has no players remaining but is still running.")
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            // Tab complete online player names (preferably those in games)
            return Bukkit.getOnlinePlayers()
                .filter { GameRegistry.getGameByPlayer(it.uniqueId) != null }
                .map { it.name }
                .filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}
