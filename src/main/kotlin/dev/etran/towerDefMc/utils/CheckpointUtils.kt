package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.persistence.PersistentDataType

fun findCheckpointById(world: World, targetId: Int): ArmorStand? {
    return world.entities
        .filterIsInstance<ArmorStand>()
        .firstOrNull { armorStand ->
            val pdc = armorStand.persistentDataContainer

            val isCheckpoint = pdc.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING)

            val checkpointId = pdc.get(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER)

            isCheckpoint.equals("Checkpoint") && checkpointId == targetId
        }
}

fun findMaxCheckpoint(world: World): Int {
    var maxId = 0

    world.entities
        .filterIsInstance<ArmorStand>()
        .forEach { armorStand ->
            val currentId =
                armorStand.persistentDataContainer.get(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER) ?: 0

            if (currentId > maxId) {
                maxId = currentId
            }
        }

    return maxId
}