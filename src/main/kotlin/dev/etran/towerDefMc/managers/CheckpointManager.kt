package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.SortedMap
import java.util.TreeMap

object CheckpointManager {
    var checkpoints: SortedMap<Int, Entity> = TreeMap<Int, Entity>()
    var standsAreVisible: Boolean = true
    val effectType = PotionEffectType.GLOWING
    val duration = Int.MAX_VALUE
    val amplifier = 255

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
        val entitiesInOrder: List<Entity> = checkpoints.values.toList()

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

        checkpoints = reIndexedCheckpoints

        return entitiesInOrder
    }

    /**
     * Clears existing checkpoints
     */
    fun clearAllCheckpoints(worlds: MutableList<World>): Boolean {
        try {
            checkpoints.clear()

            worlds.flatMap { world -> world.entities }.filterIsInstance<ArmorStand>().filter {
                it.persistentDataContainer.has(
                    TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER
                ) || it.persistentDataContainer.has(TowerDefMC.STARTPOINT_ID, PersistentDataType.INTEGER)
            }.forEach { it.remove() }
            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
    }

    /**
     * Toggle stand visibility
     */
    fun toggleStandVisibility(): Boolean {
        try {
            standsAreVisible = !standsAreVisible
            Bukkit.getWorlds().forEach { world ->
                world.entities.filterIsInstance<ArmorStand>().filter { entity ->
                    entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING) != null
                }.forEach { entity ->
                    if (standsAreVisible) {
                        entity.isInvisible = false
                        entity.addPotionEffect(PotionEffect(effectType, duration, amplifier))
                    } else {
                        entity.isInvisible = true
                        entity.removePotionEffect(effectType)
                    }
                }
            }

            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
    }
}