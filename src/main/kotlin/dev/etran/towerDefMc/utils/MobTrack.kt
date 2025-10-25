package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType

fun getClosestMobToTower(world: World, tower: Entity, blocksDistance: Double): Entity? {
    return world.entities
        .filter { entity ->
            val enemyTag = entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("Enemy")
            // Distance squared function is more efficient than distance function
            val distance = tower.location.distanceSquared(entity.location) < blocksDistance * blocksDistance
            enemyTag && distance
        }
        .minByOrNull {
            tower.location.distanceSquared(it.location)
        }
}

fun getClosestMobToEnd(world: World, tower: Entity, blocksDistance: Double): Entity? {
    val endPoint = world.entities
        .filter { entity ->
            entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("EndPoint")
        }.get(1)

    return world.entities
        .filter { entity ->
            val enemyTag = entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("Enemy")
            // Distance squared function is more efficient than distance function
            val distance = tower.location.distanceSquared(entity.location) < blocksDistance * blocksDistance
            enemyTag && distance
        }
        // Add code to get the lowest checkpoint id
        .minByOrNull {  tower.location.distanceSquared(endPoint.location) }
}

fun getFurthestMobFromTower(world: World, tower: Entity, blocksDistance: Double): Entity? {
    return world.entities
        .filter { entity ->
            val enemyTag = entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("Enemy")
            // Distance squared function is more efficient than distance function
            val distance = tower.location.distanceSquared(entity.location) < blocksDistance * blocksDistance
            enemyTag && distance
        }
        .maxByOrNull {
            tower.location.distanceSquared(it.location)
        }
}

fun getClosestMobToSpawn(world: World, tower: Entity, blocksDistance: Double): Entity? {
    val spawnPoint = world.entities
        .filter { entity ->
            entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("StartPoint")
        }.get(1)

    return world.entities
        .filter { entity ->
            val enemyTag = entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING).equals("Enemy")
            // Distance squared function is more efficient than distance function
            val distance = tower.location.distanceSquared(entity.location) < blocksDistance * blocksDistance
            enemyTag && distance
        }
        // Add code to get the lowest checkpoint id
        .minByOrNull {  tower.location.distanceSquared(spawnPoint.location) }
}