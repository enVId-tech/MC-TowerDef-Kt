package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.CheckpointManager
import dev.etran.towerDefMc.utils.setMobTargetLocation
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType

object EnemyScheduler {
    fun checkAndHandleEnemies(world: World) {
        // Only get the entities in the world
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer

            // Only proceed if entity actually has the key
            if (!container.has(TowerDefMC.ENEMY_TYPES, PersistentDataType.STRING)) return@forEach
            val enemyId = container.get(TowerDefMC.ENEMY_TYPES, PersistentDataType.STRING) ?: return@forEach

            when (enemyId) {
                "Basic_Enemy_1" -> {
                    val currentTargetId =
                        container.get(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER) ?: 1
                    val targetCheckpoint = CheckpointManager.checkpoints[currentTargetId]
                    val maxCheckpointId = CheckpointManager.checkpoints.keys.lastOrNull() ?: 1

                    if (targetCheckpoint == null) {
                        val nextPotentialId = currentTargetId + 1

                        if (!CheckpointManager.checkpoints.containsKey(nextPotentialId)) {
                            /* TODO: Skip the next ID, the next checkpoint should be the EndPoint, so make a PDC to set the mob to go to the endpoint
                                If endpoint doesn't exist, delete the mob at the point, assume that it has reached an EndPoint
                            */
                            entity.remove()
                        } else {
                            container.set(
                                TowerDefMC.TARGET_CHECKPOINT_ID,
                                PersistentDataType.INTEGER,
                                currentTargetId + 1
                            )
                        }

                        return@forEach
                    }


                    // Direct distance checking instead of radius checking
                    val distanceSq = entity.location.distanceSquared(targetCheckpoint.location)
                    val arrivalRangeSq = 1.0 * 1.0

                    if (distanceSq <= arrivalRangeSq) {
                        entity.setAI(false)

                        val nextId = currentTargetId + 1

                        val isEndPoint = targetCheckpoint.persistentDataContainer.get(
                            TowerDefMC.ELEMENT_TYPES,
                            PersistentDataType.STRING
                        ) == "EndPoint"

                        if (isEndPoint) {
                            entity.remove()
                            // TODO: runGameLoss();
                            return@forEach
                        }

                        if (nextId <= maxCheckpointId) {
                            container.set(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, nextId)
                        }

                        return@forEach
                    }
                    if (entity is Mob) setMobTargetLocation(entity, targetCheckpoint.location, 2.0)
                }
            }
        }
    }
}