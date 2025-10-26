package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.damageEnemy
import dev.etran.towerDefMc.utils.getClosestMobToTower
import dev.etran.towerDefMc.utils.towerTurnToTarget
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType
import kotlin.math.atan2
import kotlin.math.sqrt


object TowerScheduler {
    fun checkAndHandleTowers(world: World) {
        // Only get the entities in the world
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer

            // Only proceed if entity actually has the key
            if (!container.has(TowerDefMC.TOWER_TYPES, PersistentDataType.STRING)) return@forEach
            val towerId = container.get(TowerDefMC.TOWER_TYPES, PersistentDataType.STRING) ?: return@forEach

            // Checks each instance of the global identifier and runs code accordingly
            when (towerId) {
                "Basic_Tower_1" -> {
                    val range = entity.persistentDataContainer.getOrDefault(
                        TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, 20.0
                    )
                    val targetEntity = getClosestMobToTower(world, entity as Entity, range)
                    if (targetEntity != null) {
                        entity.setAI(false)
                        entity.isInvulnerable = true
                        towerTurnToTarget(entity, targetEntity)
                        if (!targetEntity.isDead) {
                            damageEnemy(entity, targetEntity as LivingEntity)
                        }
                    }
                }
            }
        }
    }
}