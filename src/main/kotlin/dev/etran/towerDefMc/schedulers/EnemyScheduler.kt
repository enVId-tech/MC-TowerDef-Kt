package dev.etran.towerDefMc.schedulers

import org.bukkit.World
import org.bukkit.entity.LivingEntity

object EnemyScheduler {
    fun checkAndHandleEnemies(world: World) {
        // Only get the entities in the world
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer
        }
    }
}