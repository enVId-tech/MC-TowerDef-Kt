package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import kotlin.math.atan2
import kotlin.math.sqrt


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
                        val maxPitch = 40f

                        val playerEye = targetPlayer.eyeLocation
                        val entityEye = entity.eyeLocation

                        val dx = playerEye.x - entityEye.x
                        val dy = playerEye.y - entityEye.y
                        val dz = playerEye.z - entityEye.z

                        val horiz = sqrt(dx * dx + dz * dz).coerceAtLeast(1e-6)

                        val yaw = Math.toDegrees(atan2(-dx, dz)).toFloat()
                        val pitch = Math.toDegrees(atan2(-dy, horiz)).toFloat()

                        val clampedPitch = pitch.coerceIn(-maxPitch, maxPitch)

                        val loc = entity.location.clone()
                        loc.yaw = yaw
                        loc.pitch = clampedPitch
                        entity.teleport(loc)
                    }
                }
            }
        }
    }
}