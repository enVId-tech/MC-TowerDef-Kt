package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object NextWave : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be used by players!")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /nextwave <gameId>")
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
            return true
        }

        // Start the next wave
        gameManager.waveManager.startNextWave()
        sender.sendMessage("§aStarted wave ${gameManager.waveManager.currentWave} in game $gameId")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            return GameRegistry.allGames.keys.map { it.toString() }
                .filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}

