package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType
import kotlin.math.atan2
import kotlin.math.sqrt

fun getClosestMobToTower(world: World, tower: Entity, blocksDistance: Double): Entity? {
    return world.entities.filter { entity ->
        val enemyTag =
            entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("Enemy")
        // Distance squared function is more efficient than distance function
        val distance = tower.location.distanceSquared(entity.location) < blocksDistance * blocksDistance
        enemyTag && distance
    }.minByOrNull {
        tower.location.distanceSquared(it.location)
    }
}

fun getClosestMobToEnd(world: World, tower: Entity, blocksDistance: Double): Entity? {
    val endPoint = world.entities.filter { entity ->
        entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("EndPoint")
    }.get(1)

    return world.entities.filter { entity ->
        val enemyTag =
            entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("Enemy")
        // Distance squared function is more efficient than distance function
        val distance = tower.location.distanceSquared(entity.location) < blocksDistance * blocksDistance
        enemyTag && distance
    }
        // Add code to get the lowest checkpoint id
        .minByOrNull { tower.location.distanceSquared(endPoint.location) }
}

fun getFurthestMobFromTower(world: World, tower: Entity, blocksDistance: Double): Entity? {
    return world.entities.filter { entity ->
        val enemyTag =
            entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("Enemy")
        // Distance squared function is more efficient than distance function
        val distance = tower.location.distanceSquared(entity.location) < blocksDistance * blocksDistance
        enemyTag && distance
    }.maxByOrNull {
        tower.location.distanceSquared(it.location)
    }
}

fun getClosestMobToSpawn(world: World, tower: Entity, blocksDistance: Double): Entity? {
    val spawnPoint = world.entities.filter { entity ->
        entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("StartPoint")
    }.get(1)

    return world.entities.filter { entity ->
        val enemyTag =
            entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("Enemy")
        // Distance squared function is more efficient than distance function
        val distance = tower.location.distanceSquared(entity.location) < blocksDistance * blocksDistance
        enemyTag && distance
    }
        // Add code to get the lowest checkpoint id
        .minByOrNull { tower.location.distanceSquared(spawnPoint.location) }
}

fun towerTurnToTarget(entity: LivingEntity, targetEntity: Entity): Boolean {
    try {
        // Math for the rotation of the tower towards the player (soon to be enemy)
        val maxPitch = 40f

        val playerEye = targetEntity.location
        val entityEye = entity.eyeLocation

        val dx = playerEye.x - entityEye.x
        val dy = playerEye.y - entityEye.y
        val dz = playerEye.z - entityEye.z

        val horiz = sqrt(dx * dx + dz * dz).coerceAtLeast(1e-6)

        val yaw = Math.toDegrees(atan2(-dx, dz)).toFloat()
        val pitch = -Math.toDegrees(atan2(dy, horiz)).toFloat()

        val clampedPitch = pitch.coerceIn(-maxPitch, maxPitch)

        val loc = entity.location.clone()
        loc.yaw = yaw
        loc.pitch = clampedPitch
        entity.teleport(loc)
        return true
    } catch (ex: Exception) {
        ex.printStackTrace()
        return false
    }
}