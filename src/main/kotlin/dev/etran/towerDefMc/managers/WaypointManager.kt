package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.SortedMap
import java.util.TreeMap

/**
 * Unified manager for all waypoints (start points, checkpoints, end points)
 * Combines functionality from CheckpointManager and StartpointManager
 */
class WaypointManager {
    var checkpoints: SortedMap<Int, ArmorStand> = TreeMap()
    var startpoints: SortedMap<Int, Entity> = TreeMap()
    var standsAreVisible: Boolean = true

    companion object {
        val effectType: PotionEffectType = PotionEffectType.GLOWING
        const val DURATION = Int.MAX_VALUE
        const val AMPLIFIER = 255
        lateinit var plugin: TowerDefMC

        fun initialize(plugin: TowerDefMC) {
            this.plugin = plugin
        }
    }

    // ==================== Start Point Management ====================

    /**
     * Set the start point location
     */
    fun setStartPoint(location: Location) {
        // Remove old start point if exists
        startpoints.values.firstOrNull()?.remove()
        startpoints.clear()

        // Create armor stand using the existing pattern
        val world = location.world ?: return
        val armorStand = world.spawn(location, ArmorStand::class.java) { stand ->
            stand.isVisible = standsAreVisible
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.customName(Component.text("§a§lSTART POINT"))
            stand.isCustomNameVisible = standsAreVisible

            // Set the element type so it works with existing system
            stand.persistentDataContainer.set(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "StartPoint"
            )
        }

        // Add to startpoint manager using existing method
        val startId = addStartpoint(armorStand)
        armorStand.persistentDataContainer.set(
            TowerDefMC.STARTPOINT_ID, PersistentDataType.INTEGER, startId
        )

        saveCheckpoints()
    }

    /**
     * Add a start point entity
     */
    private fun addStartpoint(entity: Entity): Int {
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
     * Remove a start point entity
     */
    fun removeStartpoint(entity: Entity): Int {
        if (entity !is ArmorStand) return -1

        val removeId = entity.persistentDataContainer.get(TowerDefMC.STARTPOINT_ID, PersistentDataType.INTEGER)
        if (removeId == null) return -1

        val removedValue = startpoints.remove(removeId)

        if (removedValue != null) {
            adjustStartpointIds()
            return removeId
        }

        return -1
    }

    /**
     * Readjust start point IDs to be consecutive
     */
    private fun adjustStartpointIds(): List<Entity> {
        val entitiesInOrder: List<Entity> = startpoints.values.toList()
        val reIndexedStartpoints: SortedMap<Int, Entity> = TreeMap()

        entitiesInOrder.forEachIndexed { index, entity ->
            val newId = index + 1
            entity.persistentDataContainer.set(
                TowerDefMC.STARTPOINT_ID, PersistentDataType.INTEGER, newId
            )
            reIndexedStartpoints[newId] = entity
        }

        startpoints = reIndexedStartpoints
        return entitiesInOrder
    }

    // ==================== Checkpoint Management ====================

    /**
     * Add a checkpoint location
     */
    fun addCheckpoint(location: Location) {
        // Create armor stand for checkpoint
        val world = location.world ?: return
        val armorStand = world.spawn(location, ArmorStand::class.java) { stand ->
            stand.isVisible = standsAreVisible
            stand.setGravity(false)
            stand.isInvulnerable = true

            // Set the element type so it works with existing system
            stand.persistentDataContainer.set(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Checkpoint"
            )
        }

        // Add to checkpoint manager using existing method
        val checkpointId = add(armorStand)
        armorStand.persistentDataContainer.set(
            TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, checkpointId
        )
        armorStand.customName(Component.text("§e§lCHECKPOINT #$checkpointId"))

        saveCheckpoints()
    }

    /**
     * Add a checkpoint armor stand entity
     */
    fun add(entity: ArmorStand): Int {
        var smallestAvailableId = 1

        for (id in checkpoints.keys) {
            if (id == smallestAvailableId) {
                smallestAvailableId++
            } else {
                break
            }
        }

        checkpoints[smallestAvailableId] = entity
        saveCheckpoints()
        return smallestAvailableId
    }

    /**
     * Remove a checkpoint entity
     */
    fun remove(entity: ArmorStand): Int {
        val removeId = entity.persistentDataContainer.get(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER)
        if (removeId == null) return -1

        val removedValue = checkpoints.remove(removeId)

        if (removedValue != null) {
            adjustArmorStandIds()
            saveCheckpoints()
            return removeId
        }

        saveCheckpoints()
        return -1
    }

    /**
     * Readjust checkpoint IDs to be consecutive
     */
    fun adjustArmorStandIds(): List<ArmorStand> {
        val entitiesInOrder: List<ArmorStand> = checkpoints.values.toList()
        val reIndexedCheckpoints: SortedMap<Int, ArmorStand> = TreeMap()

        entitiesInOrder.forEachIndexed { index, entity: ArmorStand ->
            val newId = index + 1
            entity.persistentDataContainer.set(
                TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, newId
            )
            reIndexedCheckpoints[newId] = entity
        }

        checkpoints = reIndexedCheckpoints
        return entitiesInOrder
    }

    // ==================== End Point Management ====================

    /**
     * Set the end point location
     */
    fun setEndPoint(location: Location) {
        // Remove old end point if exists (stored as special checkpoint)
        val existingEndpoint = checkpoints.values.lastOrNull()
        if (existingEndpoint != null && existingEndpoint.persistentDataContainer.get(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
            ) == "EndPoint"
        ) {
            remove(existingEndpoint)
        }

        // Create armor stand for end point
        val world = location.world ?: return
        val armorStand = world.spawn(location, ArmorStand::class.java) { stand ->
            stand.isVisible = standsAreVisible
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.customName(Component.text("§c§lEND POINT"))
            stand.isCustomNameVisible = true

            // Set the element type so it works with existing system
            stand.persistentDataContainer.set(
                TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "EndPoint"
            )
        }

        // Add to checkpoint manager using existing method
        val checkpointId = add(armorStand)
        armorStand.persistentDataContainer.set(
            TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, checkpointId
        )

        saveCheckpoints()
    }

    // ==================== General Waypoint Management ====================

    /**
     * Clear all waypoints (checkpoints only, not the armor stands in the world)
     * This is used to reset waypoints between waves/spawns
     */
    fun clearAllWaypoints() {
        checkpoints.clear()
    }

    /**
     * Clear all waypoints (checkpoints, start points, end points) and remove armor stands
     */
    fun clearAllWaypoints(worlds: MutableList<World>): Boolean {
        try {
            checkpoints.clear()
            startpoints.clear()
            saveCheckpoints()

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
     * Toggle visibility of all waypoint stands
     */
    fun toggleStandVisibility(): Boolean {
        try {
            standsAreVisible = !standsAreVisible

            Bukkit.getWorlds().forEach { world ->
                world.entities.filterIsInstance<ArmorStand>().filter { entity ->
                    val elementType =
                        entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING)
                    // Only toggle visibility for Checkpoint, StartPoint, and EndPoint
                    // NOT for PathStart, PathCheckpoint, or PathEnd (those are managed by PathManager)
                    elementType == "Checkpoint" || elementType == "StartPoint" || elementType == "EndPoint"
                }.forEach { entity ->
                    if (standsAreVisible) {
                        entity.isInvisible = false
                        entity.isCustomNameVisible = true
                        entity.addPotionEffect(PotionEffect(effectType, DURATION, AMPLIFIER))
                    } else {
                        entity.isInvisible = true
                        entity.isCustomNameVisible = false
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

    /**
     * Save waypoints to a YAML file
     */
    fun saveCheckpoints() {
        val file = File(plugin.dataFolder, "checkpoints.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        // Convert the current list of ArmorStands into a list of location maps
        val locationData = checkpoints.entries.map { (index, armorStand) ->
            val loc = armorStand.location
            mapOf(
                "world" to loc.world.name,
                "x" to loc.x,
                "y" to loc.y,
                "z" to loc.z,
                "index" to index,
                "isEndpoint" to armorStand.persistentDataContainer.get(
                    TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
                ).equals("EndPoint"),
                "isCheckpoint" to armorStand.persistentDataContainer.get(
                    TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
                ).equals("Checkpoint"),
                "checkpointId" to armorStand.persistentDataContainer.get(
                    TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER
                )
            )
        }

        config.set("checkpoints", locationData)

        try {
            config.save(file)
        } catch (err: Exception) {
            err.printStackTrace()
            plugin.logger.severe { "Could not save checkpoints.yml: ${err.message}" }
        }
    }
}
