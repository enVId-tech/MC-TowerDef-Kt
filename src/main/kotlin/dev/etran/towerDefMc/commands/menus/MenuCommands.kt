package dev.etran.towerDefMc.commands.menus

import dev.etran.towerDefMc.menus.Home
import dev.etran.towerDefMc.menus.games.GameSelector
import dev.etran.towerDefMc.menus.games.ModifyGame
import dev.etran.towerDefMc.menus.games.DeleteGame
import dev.etran.towerDefMc.menus.games.NewGame
import dev.etran.towerDefMc.menus.waves.Waves
import dev.etran.towerDefMc.menus.enemies.DeleteEnemyMenu
import dev.etran.towerDefMc.menus.towers.DeleteTowerMenu
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object MenuCommands : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be executed by players!")
            return false
        }

        if (args.isEmpty()) {
            openHomeMenu(sender)
            return true
        }

        when (args[0].lowercase()) {
            "home" -> openHomeMenu(sender)
            "new", "newgame" -> openNewGameMenu(sender)
            "modify", "edit" -> openModifyGameMenu(sender, args)
            "delete", "remove" -> openDeleteGameMenu(sender)
            "waves" -> openWavesMenu(sender, args)
            "select", "selector" -> openGameSelector(sender)
            "deleteenemy", "deletenemies", "removeenemy" -> openDeleteEnemyMenu(sender)
            "deletetower", "deletetowers", "removetower" -> openDeleteTowerMenu(sender)
            else -> {
                sender.sendMessage("§cInvalid menu type. Use: home, new, modify, delete, waves, select, deleteenemy, deletetower")
                return false
            }
        }

        return true
    }

    private fun openHomeMenu(player: Player) {
        Home(player).open()
    }

    private fun openNewGameMenu(player: Player) {
        NewGame(player).open()
    }

    private fun openModifyGameMenu(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendMessage("§cUsage: /tdmenu modify <gameId>")
            openGameSelector(player) // Fallback to selector
            return
        }

        val gameId = args[1].toIntOrNull()
        if (gameId == null) {
            player.sendMessage("§cInvalid game ID. Must be a number.")
            return
        }

        if (!GameRegistry.allGames.containsKey(gameId)) {
            player.sendMessage("§cGame with ID $gameId not found!")
            return
        }

        ModifyGame(player, gameId).open()
    }

    private fun openDeleteGameMenu(player: Player) {
        DeleteGame(player).open()
    }

    private fun openWavesMenu(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendMessage("§cUsage: /tdmenu waves <gameId>")
            return
        }

        val gameId = args[1].toIntOrNull()
        if (gameId == null) {
            player.sendMessage("§cInvalid game ID. Must be a number.")
            return
        }

        val game = GameRegistry.allGames[gameId]
        if (game == null) {
            player.sendMessage("§cGame with ID $gameId not found!")
            return
        }

        Waves(player, game.config, gameId).open()
    }

    private fun openGameSelector(player: Player) {
        GameSelector(player).open()
    }

    private fun openDeleteEnemyMenu(player: Player) {
        DeleteEnemyMenu(player).open()
    }

    private fun openDeleteTowerMenu(player: Player) {
        DeleteTowerMenu(player).open()
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            val options = listOf("home", "new", "modify", "delete", "waves", "select", "deleteenemy", "deletetower")
            return options.filter { it.startsWith(args[0].lowercase()) }
        }

        if (args.size == 2 && (args[0].equals("modify", true) || args[0].equals("waves", true))) {
            return GameRegistry.allGames.keys.map { it.toString() }
        }

        return emptyList()
    }
}
