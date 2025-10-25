package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.getClosestMobToTower
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
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
                    val targetPlayer = getClosestMobToTower(world, entity as Entity, 30.0)
                    if (targetPlayer != null) {
                        entity.setAI(false)
                        entity.isInvulnerable = true

                        // Math for the rotation of the tower towards the player (soon to be enemy)
                        val maxPitch = 40f

                        val playerEye = targetPlayer.location
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