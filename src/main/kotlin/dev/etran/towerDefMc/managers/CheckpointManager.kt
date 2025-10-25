package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.world
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.persistence.PersistentDataType
import java.util.SortedMap
import java.util.TreeMap

object CheckpointManager {
    var checkpoint: SortedMap<Int, Entity> = TreeMap<Int, Entity>()

    /**
     * @param entity The armor stand entity to insert
     * @return Int The integer representing the ID of the armor stnad
     */
    fun add(entity: Entity): Int {
        if (entity !is ArmorStand) return -1

        for (i in 1..checkpoint.keys.last()) {
            if (i !in checkpoint) {
                checkpoint[i] = entity
                return i
            }
        }

        val insertId = checkpoint.keys.last() + 1

        checkpoint[insertId] = entity
        return insertId
    }

    /**
     * Removes an entity from the checkpoint list
     * Automatically readjusts IDs to be consecutive if removal was successful
     * @param entity The armor stand to remove
     * @return The ID of the armor stand if removal was successful, -1 if failed
     */
    fun remove(entity: Entity): Int {
        if (entity !is ArmorStand) return -1

        val removeId = entity.persistentDataContainer.get(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER)
        if (removeId == null) return -1

        val removedValue = checkpoint.remove(removeId)

        if (removedValue != null) {
            adjustArmorStandIds()
            return removeId
        }

        return -1
    }

    /**
     * Readjusts checkpoint list IDs to always be consecutive
     */
    fun adjustArmorStandIds(): List<Entity> {
        val entitiesInOrder: List<Entity> = checkpoint.values.toList()

        val currentSize = entitiesInOrder.size
        val lastKey = checkpoint.keys.lastOrNull() ?: 0

        if (lastKey == currentSize) {
            return entitiesInOrder
        }

        val reIndexedCheckpoint: SortedMap<Int, Entity> = entitiesInOrder.mapIndexed { index, entity ->
            val newId = index + 1
            newId to entity
        }.toMap(TreeMap())

        checkpoint = reIndexedCheckpoint

        return entitiesInOrder
    }
}