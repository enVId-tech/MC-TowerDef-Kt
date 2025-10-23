package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.TowerFactory
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GiveTower : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 2) {
            sender.sendMessage("Usage: /givettower <player> <size>")
            return false
        }

        if (sender !is Player) {
            sender.sendMessage("This command can only be run by players")
            return false
        }

        val playerName = args[0]
        val amount = args[1].toInt()
        val player = Bukkit.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage("Player $playerName does not exist")
            return false
        }

        if (amount <= 0) {
            sender.sendMessage("Amount must be greater than zero")
            return false
        }

        // Generate tower stack
        val tower: ItemStack = TowerFactory.newBasicTower(amount)

        // Add items to inventory, stores whatever cannot be put in inventory
        val leftovers = player.inventory.addItem(tower)

        // Throws out remaining items in leftovers
        leftovers.values.forEach { towerItemStack ->
            player.world.dropItemNaturally(
                player.location,
                towerItemStack
            )
        }

        return true
    }
}