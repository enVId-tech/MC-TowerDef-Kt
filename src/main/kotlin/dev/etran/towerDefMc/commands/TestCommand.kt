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
        if (args.size != 2) {
            sender.sendMessage("Usage: /diamonds <player> <size>")
            return false
        }

        if (sender !is Player) {
            sender.sendMessage("This command can only be run by players")
            return false
        }

        val playerName = args[0]
        val targetPlayer = Bukkit.getPlayer(playerName)
        val itemType = Material.DIAMOND
        val amount = args[1].toIntOrNull()
        if (amount == null || amount <= 0) {
            sender.sendMessage("You need to specify a valid amount")
            return false
        }
        val diamondStack = ItemStack(itemType, amount)


        val leftovers = targetPlayer?.inventory?.addItem(diamondStack)
        leftovers?.values?.forEach { leftover -> targetPlayer.world.dropItemNaturally(targetPlayer.location, leftover)
        }

        return true
    }
}