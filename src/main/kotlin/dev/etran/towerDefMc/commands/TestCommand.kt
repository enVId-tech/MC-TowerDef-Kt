package dev.etran.towerDefMc.commands

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class TestCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1) {
            sender.sendMessage("Usage: /spawnegg <player> <size>")
            return false
        }

        if (sender !is Player) {
            sender.sendMessage("This command can only be run by players")
            return false
        }

        val playerName = args[0]
        val player = Bukkit.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage("Player $playerName does not exist")
            return false
        }

        val spawnegg = Material.ZOMBIE_SPAWN_EGG


        return true
    }
}