package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.EnemyFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GiveEnemy : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be executed by the player!")
            return false
        }

        if (args.size > 1) {
            sender.sendMessage("Command usage: /givetenemy <amount>")
            return false
        }

        if (args.size == 1 && args[0].toIntOrNull() == null) {
            sender.sendMessage("The first argument must be a valid positive integer.")
            return false
        }

        val amount = args[0].toInt()
        val player = sender.player ?: return false

        if (amount <= 0) {
            sender.sendMessage("Amount must be greater than zero")
            return false
        }

        // Generate enemy stack
        val enemy: ItemStack = EnemyFactory.newBasicEnemy(amount)

        // Add items to inventory, stores whatever cannot be put in inventory
        val leftovers = player.inventory.addItem(enemy)

        // Throws out remaining items in leftovers
        leftovers.values.forEach { enemyItemStack ->
            player.world.dropItemNaturally(
                player.location,
                enemyItemStack
            )
        }

        return true
    }
}