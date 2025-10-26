package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.applyEnemyMovementLogic
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType

object EnemyScheduler {
    fun checkAndHandleEnemies(world: World) {
        // Only get the entities in the world
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer

            // Only proceed if entity actually has the key
            if (!container.has(TowerDefMC.ENEMY_TYPES, PersistentDataType.STRING)) return@forEach
            entity.isCollidable = false

            applyEnemyMovementLogic(entity)
        }
    }
}