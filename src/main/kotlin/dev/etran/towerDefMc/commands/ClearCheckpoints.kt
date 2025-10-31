package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object ClearCheckpoints : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            // Clear checkpoints for all active games
            GameRegistry.activeGames.values.forEach { game ->
                game.checkpointManager.clearAllCheckpoints(Bukkit.getWorlds(), game.startpointManager)
            }

            sender.sendMessage("§aCheckpoints cleared for all active games")
            return true
        } catch (ex: Exception) {
            sender.sendMessage("§cClear failed: ${ex.message}")
            return false
        }
    }
}