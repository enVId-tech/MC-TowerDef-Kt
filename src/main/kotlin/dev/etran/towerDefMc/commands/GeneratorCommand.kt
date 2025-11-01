package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.menus.generators.EnemyGeneratorMenu
import dev.etran.towerDefMc.menus.generators.TowerGeneratorMenu
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object GeneratorCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be executed by players!")
            return false
        }

        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /tdgenerator <tower|enemy>")
            sender.sendMessage("§7Hold a spawn egg and run the command to configure properties")
            return false
        }

        when (args[0].lowercase()) {
            "tower" -> {
                val heldItem = sender.inventory.itemInMainHand
                if (!heldItem.type.name.endsWith("_SPAWN_EGG")) {
                    sender.sendMessage("§cYou must be holding a spawn egg!")
                    sender.sendMessage("§7Hold the spawn egg you want to use for the tower")
                    return false
                }

                TowerGeneratorMenu(sender).open()
            }
            "enemy" -> {
                val heldItem = sender.inventory.itemInMainHand
                if (!heldItem.type.name.endsWith("_SPAWN_EGG")) {
                    sender.sendMessage("§cYou must be holding a spawn egg!")
                    sender.sendMessage("§7Hold the spawn egg you want to use for the enemy")
                    return false
                }

                EnemyGeneratorMenu(sender).open()
            }
            else -> {
                sender.sendMessage("§cInvalid type. Use: tower or enemy")
                return false
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            return listOf("tower", "enemy").filter { it.startsWith(args[0].lowercase()) }
        }
        return emptyList()
    }
}

