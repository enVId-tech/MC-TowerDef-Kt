package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import java.util.SortedMap
import java.util.TreeMap

class StartpointManager {
    var startpoints: SortedMap<Int, Entity> = TreeMap()

    /**
     * @param entity The armor stand entity to insert
     * @return Int The integer representing the ID of the armor stnad
     */
    fun add(entity: Entity): Int {
        if (entity !is ArmorStand) return -1

        var smallestAvailableId = 1

        for (id in startpoints.keys) {
            if (id == smallestAvailableId) {
                smallestAvailableId++
            } else {
                break
            }
        }

        startpoints[smallestAvailableId] = entity

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

        val removedValue = startpoints.remove(removeId)

//        entity.world.players.forEach { player -> player.sendMessage(checkpoints.toString()) }
//        entity.world.players.forEach { player -> player.sendMessage(removeId.toString()) }

        if (removedValue != null) {
            adjustArmorStandIds()
            return removeId
        }

        return -1
    }

    /**
     * Readjusts checkpoint list IDs to always be consecutive
     * @return A list of armors after readjustment of IDs
     */
    fun adjustArmorStandIds(): List<Entity> {
        val entitiesInOrder: List<Entity> = startpoints.values.toList()

        val reIndexedCheckpoints: SortedMap<Int, Entity> = TreeMap()

        entitiesInOrder.forEachIndexed { index, entity ->
            val newId = index + 1

            // Update the Persistent Data Container (PDC) on the actual entity
            entity.persistentDataContainer.set(
                TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, newId
            )

            // Populate the new map
            reIndexedCheckpoints[newId] = entity
        }

        startpoints = reIndexedCheckpoints

        return entitiesInOrder
    }
}