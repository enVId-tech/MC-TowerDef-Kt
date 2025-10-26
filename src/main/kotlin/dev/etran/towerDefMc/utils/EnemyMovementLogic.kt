package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.CheckpointManager
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

fun applyEnemyMovementLogic(entity: Entity) {
    val container = entity.persistentDataContainer
    val currentTargetId =
        container.get(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER) ?: 1
    var targetCheckpoint = CheckpointManager.checkpoints[currentTargetId]
    val maxCheckpointId = CheckpointManager.checkpoints.keys.lastOrNull() ?: 1

    // Target Switch Logic (Continuous Flow)
    if (targetCheckpoint != null) {
        // Check if the enemy is within the "switch radius" (4 blocks squared = 2 blocks radius)
        val switchRangeSq = 1.0 * 1.0
        val distanceSq = entity.location.distanceSquared(targetCheckpoint.location)

        if (distanceSq <= switchRangeSq) {
            val nextId = currentTargetId + 1

            // Check for EndPoint or Max Checkpoint
            val isEndPoint = targetCheckpoint.persistentDataContainer.get(
                TowerDefMC.ELEMENT_TYPES,
                PersistentDataType.STRING
            ) == "EndPoint"

            // If the current target was an EndPoint OR the next ID is out of bounds, remove the entity
            if (isEndPoint || !CheckpointManager.checkpoints.containsKey(nextId)) {
                cleanUpEnemyHealthBar(entity)
                entity.remove()
                // TODO: runGameLoss();
                return
            }

            // Update the TARGET_CHECKPOINT_ID to the next checkpoint
            container.set(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, nextId)

            // Immediately update targetCheckpoint object to the new location for continuous pathing
            targetCheckpoint = CheckpointManager.checkpoints[nextId]
        }
    } else {
        // The current target ID is invalid (e.g., checkpoint was destroyed).
        // Skip it and increment the ID to find the next valid checkpoint.
        val nextPotentialId = currentTargetId + 1

        if (!CheckpointManager.checkpoints.containsKey(nextPotentialId)) {
            // No more valid checkpoints available after the current missing one, assume end of path.
            cleanUpEnemyHealthBar(entity)
            entity.remove()
        } else {
            container.set(
                TowerDefMC.TARGET_CHECKPOINT_ID,
                PersistentDataType.INTEGER,
                currentTargetId + 1
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