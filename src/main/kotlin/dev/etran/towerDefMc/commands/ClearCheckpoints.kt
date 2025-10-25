package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.managers.CheckpointManager.clearAllCheckpoints
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ClearCheckpoints : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val success = clearAllCheckpoints(Bukkit.getWorlds())
        if (!success) {
            sender.sendMessage("Clear failed")
            return false
        }
        sender.sendMessage("Checkpoints cleared")
        return true
    }
}