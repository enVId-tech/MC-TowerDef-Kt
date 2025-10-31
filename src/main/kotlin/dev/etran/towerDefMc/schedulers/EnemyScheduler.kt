package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.applyEnemyMovementLogic
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType

object EnemyScheduler {
    fun checkAndHandleEnemies(world: World) {
        // Get all active games
        val activeGames = GameRegistry.activeGames

        // Only get the entities in the world that are enemies
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer

            // Only proceed if entity actually has the key
            if (!container.has(TowerDefMC.ENEMY_TYPES, PersistentDataType.STRING)) return@forEach
            entity.isCollidable = false

            // Get the game ID this enemy belongs to
            val gameId = dev.etran.towerDefMc.managers.GameInstanceTracker.getGameId(entity)

            if (gameId != null) {
                val game = activeGames[gameId]
                if (game != null) {
                    // Use the game-specific waypoint manager
                    applyEnemyMovementLogic(entity, game.waypointManager, gameId)
                } else {
                    // Game not found, remove this orphaned enemy
                    entity.remove()
                    dev.etran.towerDefMc.managers.GameInstanceTracker.unregisterEntity(entity)
                }
            }
        }
    }
}