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

    fun remove(entity: Entity): Int {
        if (entity !is ArmorStand) return -1

        val removeId = entity.persistentDataContainer.get(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER)

        if (removeId == null) return -1

        checkpoint.remove(removeId)

        return removeId
    }

    fun adjustArmorStandIds(): MutableList<Int> {
        var intIds: MutableList<Int> = mutableListOf()

        for (i in 1..checkpoint.keys.size) {
            for (j in 1..checkpoint.keys.last()) {

            }
            if (i !in checkpoint) {

            }
        }

        return intIds
    }
}