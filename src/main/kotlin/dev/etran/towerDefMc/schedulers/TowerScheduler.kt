package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.damageEnemy
import dev.etran.towerDefMc.utils.getClosestMobToTower
import dev.etran.towerDefMc.utils.towerTurnToTarget
import dev.etran.towerDefMc.utils.DebugLogger
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType

object TowerScheduler {
    private var lastDebugTime = 0L
    private const val DEBUG_INTERVAL = 5000L // Log every 5 seconds

    fun checkAndHandleTowers(world: World) {
        val currentTime = System.currentTimeMillis()
        var towerCount = 0
        var attackingTowers = 0

        // Only get the entities in the world
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer

            // Only proceed if entity actually has the key
            if (!container.has(TowerDefMC.TOWER_TYPES, PersistentDataType.STRING)) return@forEach
            val towerId = container.get(TowerDefMC.TOWER_TYPES, PersistentDataType.STRING) ?: return@forEach

            towerCount++

            // Checks each instance of the global identifier and runs code accordingly
            when (towerId) {
                "Basic_Tower_1" -> {
                    val range = entity.persistentDataContainer.getOrDefault(
                        TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, 20.0
                    )
                    val targetEntity = getClosestMobToTower(world, entity as Entity, range)
                    if (targetEntity != null) {
                        attackingTowers++
                        // Tower AI is already disabled by EntityAIDisabler when placed
                        // No need to call setAI(false) every tick
                        entity.isInvulnerable = true
                        towerTurnToTarget(entity, targetEntity)
                        if (!targetEntity.isDead) {
                            damageEnemy(entity, targetEntity as LivingEntity)
                        }
                    }
                }
            }
        }

        // Periodic debug logging
        if (currentTime - lastDebugTime > DEBUG_INTERVAL && towerCount > 0) {
            TowerDefMC.instance.logger.info("[TOWER SCHEDULER] Active towers: $towerCount, Currently attacking: $attackingTowers")
            lastDebugTime = currentTime
        }
    }
}