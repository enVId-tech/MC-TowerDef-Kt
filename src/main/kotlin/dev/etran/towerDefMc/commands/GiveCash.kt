package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object GiveCash : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size < 3) {
            sender.sendMessage("§cUsage: /givecash <gameId> <playerName> <amount>")
            sender.sendMessage("§7Example: /givecash 1 Steve 500")
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
            return true
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage("§cPlayer '${args[1]}' not found!")
            return true
        }

        val amount = args[2].toIntOrNull()
        if (amount == null || amount <= 0) {
            sender.sendMessage("§cInvalid amount! Must be a positive number.")
            return true
        }

        // Add cash to player
        PlayerStatsManager.awardCash(gameId, targetPlayer.uniqueId, amount)

        sender.sendMessage("§aGave §e$amount cash §ato ${targetPlayer.name} in game $gameId")
        targetPlayer.sendMessage("§aYou received §e$amount cash §afrom an admin!")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> GameRegistry.allGames.keys.map { it.toString() }.filter { it.startsWith(args[0], ignoreCase = true) }

            2 -> Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }

            3 -> listOf("100", "500", "1000", "5000").filter { it.startsWith(args[2]) }

            else -> emptyList()
        }
    }
}

