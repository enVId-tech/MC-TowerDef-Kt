package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.StatsTrackerFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object GiveStatsTracker : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command!")
            return true
        }

        val amount = if (args.isNotEmpty()) {
            args[0].toIntOrNull() ?: 1
        } else {
            1
        }

        val tracker = StatsTrackerFactory.newStatsTracker(amount)
        sender.inventory.addItem(tracker)
        sender.sendMessage("§aYou received §e$amount§a Stats Tracker(s)!")

        return true
    }
}

