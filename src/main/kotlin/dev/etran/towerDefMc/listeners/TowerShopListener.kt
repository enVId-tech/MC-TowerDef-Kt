package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.menus.shop.TowerShopMenu
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Handles interactions with Tower Shop villagers/traders
 */
class TowerShopListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked
        val player = event.player

        // Check if the entity is a shop villager
        if (entity.type != EntityType.VILLAGER && entity.type != EntityType.WANDERING_TRADER) {
            return
        }

        val isShopEntity = entity.persistentDataContainer.has(
            TowerDefMC.createKey("tower_shop"), PersistentDataType.STRING
        )

        if (!isShopEntity) return

        event.isCancelled = true

        // Get the game ID from the shop entity
        val gameId = entity.persistentDataContainer.get(
            TowerDefMC.GAME_ID_KEY, PersistentDataType.INTEGER
        )

        if (gameId == null) {
            player.sendMessage("§cThis shop is not configured properly!")
            return
        }

        // Check if player is in the game
        val playerGame = GameRegistry.getGameByPlayer(player.uniqueId)
        if (playerGame == null || playerGame.gameId != gameId) {
            player.sendMessage("§cYou must be in game §e#${gameId} §cto use this shop!")
            return
        }

        // Open the shop menu
        TowerShopMenu(player, gameId).open()
    }
}
