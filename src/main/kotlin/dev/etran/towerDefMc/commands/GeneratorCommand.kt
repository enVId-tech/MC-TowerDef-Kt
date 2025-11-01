package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.menus.generators.EnemyGeneratorMenu
import dev.etran.towerDefMc.menus.generators.TowerGeneratorMenu
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

object GeneratorCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be executed by players!")
            return false
        }

        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /tdgenerator <tower|enemy> <entity_type>")
            sender.sendMessage("§7Example: /tdgenerator tower ZOMBIE")
            return false
        }

        if (args.size < 2) {
            sender.sendMessage("§cUsage: /tdgenerator <tower|enemy> <entity_type>")
            sender.sendMessage("§7Please specify an entity type (e.g., ZOMBIE, SKELETON, etc.)")
            return false
        }

        // Parse entity type
        val entityType = try {
            EntityType.valueOf(args[1].uppercase())
        } catch (_: IllegalArgumentException) {
            sender.sendMessage("§cInvalid entity type: ${args[1]}")
            sender.sendMessage("§7Use tab completion to see available types")
            return false
        }

        when (args[0].lowercase()) {
            "tower" -> {
                TowerGeneratorMenu(sender, entityType).open()
            }

            "enemy" -> {
                EnemyGeneratorMenu(sender, entityType).open()
            }

            else -> {
                sender.sendMessage("§cInvalid type. Use: tower or enemy")
                return false
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> {
                listOf("tower", "enemy").filter { it.startsWith(args[0].lowercase()) }
            }
            2 -> {
                // Return all living entity types that have spawn eggs or can be spawned
                EntityType.entries
                    .filter { it.isAlive && it.isSpawnable }
                    .map { it.name }
                    .filter { it.startsWith(args[1].uppercase()) }
                    .sorted()
            }
            else -> emptyList()
        }
    }
}
