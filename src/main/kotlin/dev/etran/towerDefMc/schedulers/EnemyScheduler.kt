package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.findCheckpointById
import dev.etran.towerDefMc.utils.findMaxCheckpoint
import dev.etran.towerDefMc.utils.setMobTargetLocation
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType

object EnemyScheduler {
    fun checkAndHandleEnemies(world: World) {
        // Only get the entities in the world
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer

            // Only proceed if entity actually has the key
            if (!container.has(TowerDefMC.ENEMY_KEY, PersistentDataType.STRING)) return@forEach
            val enemyId = container.get(TowerDefMC.ENEMY_KEY, PersistentDataType.STRING) ?: return@forEach

            when (enemyId) {
                "Basic_Enemy_1" -> {
                    val currentTargetId = container.get(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER) ?: 1
                    val targetCheckpoint = findCheckpointById(world, currentTargetId) ?: return@forEach

                    // Direct distance checking instead of radius checking
                    val distanceSq = entity.location.distanceSquared(targetCheckpoint.location)
                    val arrivalRangeSq = 1.0 * 1.0

                    if (distanceSq <= arrivalRangeSq) {
                        entity.setAI(false)

                        val nextId = currentTargetId + 1

                        val maxCheckpoint = findMaxCheckpoint(world)

                        if (targetCheckpoint.persistentDataContainer.get(TowerDefMC.GAME_ELEMENT_KEY, PersistentDataType.STRING) == "EndPoint") {
                            entity.damage(entity.health)
                        }

                        if (currentTargetId < maxCheckpoint) {
                            container.set(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, nextId)
                            // TODO: Handle endpoint ids properly
                        }

                        return@forEach
                    }
                    if (entity is Mob) setMobTargetLocation(entity, targetCheckpoint.location, 2.5)
                }
            }
        }
    }
}