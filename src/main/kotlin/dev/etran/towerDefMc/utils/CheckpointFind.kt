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

            val isCheckpoint = pdc.get(TowerDefMC.GAME_ELEMENT_KEY, PersistentDataType.STRING)

            val checkpointId = pdc.get(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER)

            isCheckpoint.equals("Checkpoint") && checkpointId == targetId
        }
}