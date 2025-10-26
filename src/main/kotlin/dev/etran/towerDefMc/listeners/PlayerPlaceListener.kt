package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.factories.CheckpointFactory
import dev.etran.towerDefMc.factories.EndpointFactory
import dev.etran.towerDefMc.factories.EnemyFactory
import dev.etran.towerDefMc.factories.StartPointFactory
import dev.etran.towerDefMc.factories.TowerFactory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

object PlayerPlaceListener : Listener {
    @EventHandler
    fun onPlayerPlace(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val gameElementSpawn = event.player.inventory.itemInMainHand

        gameElementSpawn.itemMeta?.let { meta: ItemMeta ->
            // Retrieve the name from the PDC
            val name = meta.persistentDataContainer.get(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING)

            if (name == null) return@let // Exit the 'let' block if tag is missing

            // Run functions specific to their unique identifiers
            when (name) {
                "Tower 1" -> TowerFactory.towerPlace(event)
                "Checkpoint" -> CheckpointFactory.checkPointPlace(event)
                "EndPoint" -> EndpointFactory.endPointPlace(event)
                "StartPoint" -> StartPointFactory.startPointPlace(event)
                "Enemy 1" -> EnemyFactory.enemyPlace(event)
                else -> event.player.sendMessage("This game element doesn't exist.")
            }

            return
        }
    }
}