package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.StartPointFactory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.Bukkit

object GiveStartPoint : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        val usageMessage = "§cUsage: /$label [player] [amount]"
        val targetPlayer: Player
        var amount: Int = 1

        when (args.size) {
            0 -> {
                if (sender !is Player) {
                    sender.sendMessage("§cWhen running this command from the console, you must specify a player.")
                    sender.sendMessage(usageMessage)
                    return false
                }
                targetPlayer = sender
            }
            1 -> {
                val potentialAmount = args[0].toIntOrNull()

                if (potentialAmount != null) {
                    if (sender !is Player) {
                        sender.sendMessage("§cWhen specifying only the amount, the sender must be a player.")
                        sender.sendMessage(usageMessage)
                        return false
                    }
                    if (potentialAmount <= 0) {
                        sender.sendMessage("§cThe amount must be a positive number.")
                        return false
                    }
                    targetPlayer = sender
                    amount = potentialAmount
                } else {
                    val player = Bukkit.getPlayer(args[0])
                    if (player == null) {
                        sender.sendMessage("§cPlayer '${args[0]}' does not exist or is not online.")
                        return false
                    }
                    targetPlayer = player
                }
            }
            2 -> {
                val player = Bukkit.getPlayer(args[0])
                if (player == null) {
                    sender.sendMessage("§cPlayer '${args[0]}' does not exist or is not online.")
                    return false
                }
                targetPlayer = player

                try {
                    val parsedAmount = args[1].toInt()
                    if (parsedAmount <= 0) {
                        sender.sendMessage("§cThe amount must be a positive number.")
                        return false
                    }
                    amount = parsedAmount
                } catch (e: NumberFormatException) {
                    sender.sendMessage("§c'${args[1]}' is not a valid number for the amount.")
                    sender.sendMessage(usageMessage)
                    return false
                }
            }
            else -> {
                sender.sendMessage("§cToo many arguments.")
                sender.sendMessage(usageMessage)
                return false
            }
        }

        val startElement: ItemStack = StartPointFactory.newStartElement()
        startElement.amount = amount // Set the amount for the stack
        val elementName = startElement.itemMeta?.displayName() ?: "Start Point Element"

        val leftovers = targetPlayer.inventory.addItem(startElement)

        sender.sendMessage("§aGave §b$amount §a$elementName to §6${targetPlayer.name}§a.")

        if (sender != targetPlayer) {
            targetPlayer.sendMessage("§aYou received §b$amount §a$elementName!")
        }

        if (leftovers.isNotEmpty()) {
            leftovers.values.forEach { elementItemStack ->
                targetPlayer.world.dropItemNaturally(
                    targetPlayer.location,
                    elementItemStack
                )
            }
            targetPlayer.sendMessage("§eSome items couldn't fit and were dropped on the ground.")
            sender.sendMessage("§e${targetPlayer.name}'s inventory was full. Remaining items were dropped at their location.")
        }

        return true
    }
}