package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import java.util.SortedMap
import java.util.TreeMap

object CheckpointManager {
    var checkpoints: SortedMap<Int, Entity> = TreeMap<Int, Entity>()

    /**
     * @param entity The armor stand entity to insert
     * @return Int The integer representing the ID of the armor stnad
     */
    fun add(entity: Entity): Int {
        if (entity !is ArmorStand) return -1

        var smallestAvailableId = 1

        for (id in checkpoints.keys) {
            if (id == smallestAvailableId) {
                smallestAvailableId++
            } else {
                break
            }
        }

        checkpoints[smallestAvailableId] = entity

        entity.world.players.forEach { player -> player.sendMessage(checkpoints.toString())}
        return smallestAvailableId
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

        val removedValue = checkpoints.remove(removeId)

        if (removedValue != null) {
            adjustArmorStandIds()
            return removeId
        }

        entity.world.players.forEach { player -> player.sendMessage(checkpoints.toString())}
        return -1
    }

    /**
     * Readjusts checkpoint list IDs to always be consecutive
     * @return A list of armors after readjustment of IDs
     */
    fun adjustArmorStandIds(): List<Entity> {
        val entitiesInOrder: List<Entity> = checkpoints.values.toList()

        val currentSize = entitiesInOrder.size
        val lastKey = checkpoints.keys.lastOrNull() ?: 0

        if (lastKey == currentSize) {
            return entitiesInOrder
        }

        val reIndexedCheckpoint: SortedMap<Int, Entity> = entitiesInOrder.mapIndexed { index, entity ->
            val newId = index + 1
            newId to entity
        }.toMap(TreeMap())

        entitiesInOrder.forEachIndexed { index, entity ->
            val newId = index + 1

            entity.persistentDataContainer.set(
                TowerDefMC.CHECKPOINT_ID,
                PersistentDataType.INTEGER,
                newId
            )

            reIndexedCheckpoint[newId] = entity
        }

        checkpoints = reIndexedCheckpoint

        return entitiesInOrder
    }
}