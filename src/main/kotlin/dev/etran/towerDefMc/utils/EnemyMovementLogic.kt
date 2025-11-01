package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameInstanceTracker
import dev.etran.towerDefMc.managers.WaypointManager
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType
import kotlin.math.ceil

fun applyEnemyMovementLogic(entity: Entity, waypointManager: WaypointManager, gameId: Int) {
    // Ensure mob never targets players (reset any targeting that might have occurred)
    // But don't disable awareness as that prevents pathfinding
    if (entity is Mob) {
        entity.setTarget(null)
    }

    val container = entity.persistentDataContainer
    val currentTargetId = container.get(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER) ?: 1
    var targetCheckpoint = waypointManager.checkpoints[currentTargetId]

    // Target Switch Logic (Continuous Flow)
    if (targetCheckpoint != null) {
        // Check if the enemy is within the "switch radius" (4 blocks squared = 2 blocks radius)
        val switchRangeSq = 1.0 * 1.0
        val distanceSq = entity.location.distanceSquared(targetCheckpoint.location)

        if (distanceSq <= switchRangeSq) {
            val nextId = currentTargetId + 1

            // Check if current checkpoint is the EndPoint
            val isEndPoint = targetCheckpoint.persistentDataContainer.get(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
            ) == "EndPoint" || targetCheckpoint.persistentDataContainer.get(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
            ) == "PathEnd"

            // Check if next checkpoint exists
            val hasNextCheckpoint = waypointManager.checkpoints.containsKey(nextId)

            // Delete the enemy if current checkpoint is marked as EndPoint/PathEnd AND there's no next checkpoint
            if (isEndPoint && !hasNextCheckpoint) {
                // Clean up health bar first
                cleanUpEnemyHealthBar(entity)

                // Trigger game damage - enemy reached the end
                val enemyGameId = GameInstanceTracker.getGameId(entity)
                if (enemyGameId != null) {
                    // Deal damage based on current health of the enemy
                    val damage = if (entity is org.bukkit.entity.LivingEntity) {
                        ceil(entity.health).toInt()
                    } else {
                        1
                    }
                    GameRegistry.activeGames[enemyGameId]?.onHealthLost(damage)
                }

                // Unregister the entity BEFORE removing it
                GameInstanceTracker.unregisterEntity(entity)

                // Remove the entity
                entity.remove()
                return
            }

            // If we didn't delete the enemy, move to next checkpoint
            if (hasNextCheckpoint) {
                // Update the TARGET_CHECKPOINT_ID to the next checkpoint
                container.set(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, nextId)

                // Immediately update targetCheckpoint object to the new location for continuous pathing
                targetCheckpoint = waypointManager.checkpoints[nextId]
            }
        }
    } else {
        // The current target ID is invalid (e.g., checkpoint was destroyed).
        // Skip it and increment the ID to find the next valid checkpoint.
        val nextPotentialId = currentTargetId + 1

        if (!waypointManager.checkpoints.containsKey(nextPotentialId)) {
            // No more valid checkpoints available after the current missing one, assume end of path.
            cleanUpEnemyHealthBar(entity)
            GameInstanceTracker.unregisterEntity(entity)
            entity.remove()
        } else {
            container.set(
                TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, currentTargetId + 1
            )
        }

        // Skip pathfinding logic for this tick since the target was updated.
        return
    }

    // Ensures mob is always moving to the updated target
    if (targetCheckpoint != null && entity is Mob) {
        setMobTargetLocation(entity, targetCheckpoint.location, 2.0)
    }
}