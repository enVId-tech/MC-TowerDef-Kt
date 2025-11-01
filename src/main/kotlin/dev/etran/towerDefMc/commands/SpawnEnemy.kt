package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.factories.EnemyFactory
import dev.etran.towerDefMc.managers.GameInstanceTracker
import dev.etran.towerDefMc.registries.EnemyRegistry
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object SpawnEnemy : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be used by players!")
            return true
        }

        if (args.size < 2) {
            sender.sendMessage("§cUsage: /spawnenemy <gameId> <enemyType> [amount]")
            sender.sendMessage("§7Available enemy types: ${EnemyRegistry.getAllEnemies().joinToString(", ") { it.id }}")
            return true
        }

        val gameId = args[0].toIntOrNull()
        if (gameId == null) {
            sender.sendMessage("§cInvalid game ID!")
            return true
        }

        val gameManager = GameRegistry.allGames[gameId]
        if (gameManager == null) {
            sender.sendMessage("§cGame $gameId not found!")
            return true
        }

        val enemyType = args[1]
        val enemy = EnemyRegistry.getEnemy(enemyType)
        if (enemy == null) {
            sender.sendMessage("§cEnemy type '$enemyType' not found!")
            sender.sendMessage("§7Available: ${EnemyRegistry.getAllEnemies().joinToString(", ") { it.id }}")
            return true
        }

        val amount = if (args.size >= 3) args[2].toIntOrNull() ?: 1 else 1
        if (amount <= 0 || amount > 100) {
            sender.sendMessage("§cAmount must be between 1 and 100!")
            return true
        }

        // Get spawn location - use a path start point or player location
        val randomPath = gameManager.pathManager.getRandomPath()
        val spawnLocation = randomPath?.startPoint ?: sender.location

        // Spawn the enemies
        repeat(amount) {
            val entity = EnemyFactory.enemyPlace(
                enemyType, spawnLocation.clone().add(
                    Math.random() * 2 - 1, 0.0, Math.random() * 2 - 1
                )
            )
            if (entity != null) {
                GameInstanceTracker.registerEntity(entity, gameId)
            }
        }

        sender.sendMessage("§aSpawned §e$amount §a${enemyType}(s) in game $gameId")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> GameRegistry.allGames.keys.map { it.toString() }.filter { it.startsWith(args[0], ignoreCase = true) }

            2 -> EnemyRegistry.getAllEnemies().map { it.id }.filter { it.startsWith(args[1], ignoreCase = true) }

            3 -> listOf("1", "5", "10", "25", "50").filter { it.startsWith(args[2]) }

            else -> emptyList()
        }
    }
}
