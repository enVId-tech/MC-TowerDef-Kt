package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * Player-accessible stop game command (can be used with command blocks)
 * Different from the admin StopGame command which requires direct game ID
 */
object StopGamePlayer : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /stopgameplayer <gameId>")
            sender.sendMessage("§7Example: /stopgameplayer 1")
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

        if (!gameManager.isGameRunning) {
            sender.sendMessage("§cGame $gameId is not currently running!")
            return true
        }

        // End the game
        gameManager.endGame(false)

        sender.sendMessage("§aGame $gameId (${gameManager.config.name}) has been stopped!")

        // Notify all players in the game
        gameManager.activePlayers.forEach { uuid ->
            val player = org.bukkit.Bukkit.getPlayer(uuid)
            player?.sendMessage("§c§lGame Stopped!")
            player?.sendMessage("§cGame $gameId (${gameManager.config.name}) has been stopped.")
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            // Only show games that are currently running
            return GameRegistry.allGames.entries
                .filter { it.value.isGameRunning }
                .map { it.key.toString() }
                .filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}
