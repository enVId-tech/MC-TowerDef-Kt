package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.EndpointFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GiveEndPoint : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be executed by the player!")
            return false
        }

        // TODO: Fix when the user doesn't input proper arguments
        if (args.isNotEmpty()) {
            sender.sendMessage("Command usage: /givetendpoint")
            return false
        }

        val player = sender.player ?: return false

        // Generate checkpoint stack
        val checkpoint: ItemStack = EndpointFactory.newEndElement()

        // Add items to inventory, stores whatever cannot be put in inventory
        val leftovers = player.inventory.addItem(checkpoint)

        // Throws out remaining items in leftovers
        leftovers.values.forEach { checkpointItemStack ->
            player.world.dropItemNaturally(
                player.location,
                checkpointItemStack
            )
        }

        return true
    }
}