package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.CheckpointFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// Defines the command as a singleton object, ensuring only one instance exists.
object GiveCheckpoint : CommandExecutor {
    // The core function executed when the command is run.
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Checks if the command sender is a player, as this command requires player context.
        if (sender !is Player) {
            sender.sendMessage("This command can only be executed by the player!")
            return false
        }

        // Checks for exactly one argument. The combined check is simplified below.
        if (args.size != 1) {
            sender.sendMessage("Command usage: /givecheckpoint <amount>")
            return false
        }

        // Checks if the single argument is a valid positive integer.
        // It uses toIntOrNull() to safely attempt conversion.
        val amount = args[0].toIntOrNull()

        if (amount == null || amount <= 0) {
            sender.sendMessage("The argument must be a valid positive integer.")
            return false
        }

        // The player must be present (should always be true since sender is a Player).
        val player = sender.player ?: return false

        // Generate checkpoint stack, passing the validated amount.
        val checkpoint: ItemStack = CheckpointFactory.newCheckpoint(amount)

        // Add items to inventory, stores whatever cannot be put in inventory
        val leftovers = player.inventory.addItem(checkpoint)

        // Throws out remaining items in leftovers
        leftovers.values.forEach { checkpointItemStack ->
            player.world.dropItemNaturally(
                player.location,
                checkpointItemStack
            )
        }

        // Returns true to indicate the command was executed successfully.
        return true
    }
}