package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.EnemyFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// Defines the command as a singleton object, ensuring only one instance exists.
object GiveEnemy : CommandExecutor {
    // The core function executed when the command is run.
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Checks if the command sender is a player, as this command requires player context.
        if (sender !is Player) {
            sender.sendMessage("This command can only be executed by the player!")
            return false
        }

        // Checks for the correct number of arguments.
        if (args.size > 1) {
            sender.sendMessage("Command usage: /givetenemy <amount>")
            return false
        }

        // Checks if the first argument (if present) is a valid integer.
        // TODO: Fix when the user doesn't input proper arguments
        if (args.size == 1 && args[0].toIntOrNull() == null) {
            sender.sendMessage("The first argument must be a valid positive integer.")
            return false
        }

        // Parses the argument to get the amount, defaulting to 0 for simplicity if no argument is provided.
        // Note: The preceding checks ensure args[0] exists and is a number if args.size == 1.
        val amount = args.getOrElse(0) { "1" }.toInt()
        val player = sender.player ?: return false

        // Ensures the requested amount is a positive number.
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

        // Returns true to indicate the command was executed successfully.
        return true
    }
}