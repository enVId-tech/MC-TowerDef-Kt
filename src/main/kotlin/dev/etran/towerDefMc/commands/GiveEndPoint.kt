package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.EndpointFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// Defines the command as a singleton object, ensuring only one instance exists.
object GiveEndPoint : CommandExecutor {
    // The core function executed when the command is run.
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Checks if the command sender is a player, as this command requires player context.
        if (sender !is Player) {
            sender.sendMessage("This command can only be executed by the player!")
            return false
        }

        // Checks for the correct number of arguments (0 or 1).
        if (args.size > 1) {
            sender.sendMessage("Command usage: /giveendpoint [amount]")
            return false
        }

        // Parses the amount argument, defaulting to 1 if not provided or if invalid.
        // This addresses the TODO by safely handling all argument input scenarios.
        val amount: Int = args.getOrNull(0)?.toIntOrNull() ?: 1

        // If the argument was provided but was invalid (e.g., a letter), toIntOrNull()
        // would return null and the amount would be 1. We must explicitly check
        // if the original argument was present but invalid to display an error.
        if (args.isNotEmpty() && args[0].toIntOrNull() == null) {
            sender.sendMessage("The argument must be a valid positive integer.")
            return false
        }

        val player = sender.player ?: return false

        // Ensures the requested amount is a positive number.
        if (amount <= 0) {
            sender.sendMessage("Amount must be greater than zero")
            return false
        }

        // Generate endpoint stack
        val endpoint: ItemStack = EndpointFactory.newEndElement(amount) // Pass the amount here

        // Add items to inventory, stores whatever cannot be put in inventory
        val leftovers = player.inventory.addItem(endpoint)

        // Throws out remaining items in leftovers
        leftovers.values.forEach { endpointItemStack ->
            player.world.dropItemNaturally(
                player.location,
                endpointItemStack
            )
        }

        // Returns true to indicate the command was executed successfully.
        return true
    }
}