package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.TowerFactory
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object GiveTower : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player && args.isEmpty()) {
            sender.sendMessage("§cWhen running this command from the console, you must specify a target player.")
            sender.sendMessage("§cUsage: /$label <player> [amount]")
            return false
        }

        // Define the player who will receive the item and the amount
        val targetPlayer: Player
        var amount: Int = 1 // Default amount is 1

        val usageMessage = "§cUsage: /$label [player] [amount]"

        when (args.size) {
            0 -> {
                // Case 0: /givettower
                targetPlayer = sender as Player
                // Amount defaults to 1
            }

            1 -> {
                // Case 1: /givettower <player> OR /givettower <amount>

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
                    // If it's NOT a number, assume it's the player name, amount defaults to 1.
                    val player = Bukkit.getPlayer(args[0])
                    if (player == null) {
                        sender.sendMessage("§cPlayer '${args[0]}' does not exist or is not online.")
                        return false
                    }
                    targetPlayer = player
                }
            }

            2 -> {
                // Case 2: /givettower <player> <amount>
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

        // Generate tower stack
        val tower: ItemStack = TowerFactory.newBasicTower(amount)

        // Add items to inventory
        val leftovers = targetPlayer.inventory.addItem(tower)

        // Send confirmation messages
        val towerName = tower.itemMeta?.displayName() ?: "Basic Tower"

        sender.sendMessage("§aGave §b$amount §a$towerName to §6${targetPlayer.name}§a.")

        // Only message the target if they aren't the sender to avoid double messaging
        if (sender != targetPlayer) {
            targetPlayer.sendMessage("§aYou received §b$amount §a$towerName!")
        }


        // Throws out remaining items in leftovers
        if (leftovers.isNotEmpty()) {
            targetPlayer.sendMessage("§eSome items couldn't fit and were dropped on the ground.")
            sender.sendMessage("§e${targetPlayer.name}'s inventory was full. Remaining items were dropped at their location.")
            leftovers.values.forEach { towerItemStack ->
                targetPlayer.world.dropItemNaturally(
                    targetPlayer.location, towerItemStack
                )
            }
        }

        return true
    }
}