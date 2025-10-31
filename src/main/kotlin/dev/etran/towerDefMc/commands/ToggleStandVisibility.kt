package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object ToggleStandVisibility : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            // Toggle stand visibility for all active games
            GameRegistry.activeGames.values.forEach { game ->
                game.checkpointManager.toggleStandVisibility()
                game.startpointManager.toggleStandVisibility()
            }
            sender.sendMessage("§aStand visibility toggled for all active games")
            return true
        } catch (ex: Exception) {
            sender.sendMessage("§cToggle failed: ${ex.message}")
            return false
        }
    }
}