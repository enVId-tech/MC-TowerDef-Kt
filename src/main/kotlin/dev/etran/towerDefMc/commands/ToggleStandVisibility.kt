package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.managers.CheckpointManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object ToggleStandVisibility : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return CheckpointManager.toggleStandVisibility()
    }
}