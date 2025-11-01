package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.factories.EnemyFactory
import dev.etran.towerDefMc.factories.StatsTrackerFactory
import dev.etran.towerDefMc.factories.TowerFactory
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

object PlayerPlaceListener : Listener {
    @EventHandler
    fun onPlayerPlace(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val gameElementSpawn = event.player.inventory.itemInMainHand

        gameElementSpawn.itemMeta?.let { meta: ItemMeta ->
            // Retrieve the name from the PDC
            val name = meta.persistentDataContainer.get(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING)

            if (name == null) return@let // Exit the 'let' block if tag is missing

            // Get the game the player is currently in
            val game = GameRegistry.getGameByPlayer(event.player.uniqueId)

            if (game == null) {
                event.player.sendMessage("You must be in a game to place game elements.")
                return
            }

            // Run functions specific to their unique identifiers
            when (name) {
                "Tower 1" -> TowerFactory.towerPlace(event)
                "Enemy 1" -> EnemyFactory.enemyPlace(event)
                "Stats_Tracker" -> StatsTrackerFactory.placeStatsTracker(event)
                else -> event.player.sendMessage("This game element doesn't exist.")
            }

            return
        }
    }
}