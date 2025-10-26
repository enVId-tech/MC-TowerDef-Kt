package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object ClearEnemies : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            Bukkit.getWorlds().forEach { world ->
                world.entities
                    .filter { it.persistentDataContainer.has(TowerDefMC.ENEMY_TYPES) }
                    .forEach { entity ->
                        entity.remove()
                    }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}