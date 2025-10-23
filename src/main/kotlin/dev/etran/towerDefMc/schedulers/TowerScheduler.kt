package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType


object TowerScheduler {
    fun checkAndHandleTowers(world: World) {
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer

            // Only proceed if entity actually has the key
            if (!container.has(TowerDefMC.TOWER_KEY, PersistentDataType.STRING)) return@forEach
            val towerId = container.get(TowerDefMC.TOWER_KEY, PersistentDataType.STRING) ?: return@forEach

            when (towerId) {
                "Basic_Tower_1" -> {
                    val targetPlayer = entity.getNearbyEntities(30.0, 30.0, 30.0)
                        .filterIsInstance<Player>()
                        .minByOrNull { entity.location.distanceSquared(it.location) }

                    if (targetPlayer != null) {
                        entity.setAI(false)
                        entity.isInvulnerable = true
                        val playerLoc = targetPlayer.eyeLocation
                        val entityLoc = entity.location.clone()
                        val directionVec = playerLoc.toVector().subtract(entityLoc.toVector()).normalize()

                        entityLoc.direction = directionVec
                        entity.teleport(entityLoc)
                    }
                }
            }
        }
    }
}