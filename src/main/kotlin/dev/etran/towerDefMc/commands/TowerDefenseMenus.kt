package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.listeners.MenuListener
import dev.etran.towerDefMc.menus.HomeMenu
import dev.etran.towerDefMc.menus.NewGame
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object TowerDefenseMenus : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be run by a player.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("Usage: /${label} [setup|upgrade]")
            return false // Returning false shows the usage from plugin.yml
        }

        when (args[0].lowercase()) {
            "menu" -> {
                val menu = HomeMenu(sender)
                MenuListener.registerMenu(sender, menu)
                menu.open()
                return true
            }
            "setup" -> {
                when (args[1].lowercase()) {
                    "game" -> {
                        val menu = NewGame(sender)
                        MenuListener.registerMenu(sender, menu)
                        menu.open()
                        return true
                    }
                }
            }
        }
        return true
    }
}