package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.PathData
import dev.etran.towerDefMc.data.SerializableLocation
import dev.etran.towerDefMc.data.SerializablePathData
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.persistence.PersistentDataType

/**
 * Manages multiple paths for enemy movement
 */
class PathManager {
    private val paths = mutableMapOf<Int, PathData>()
    private val pathArmorStands = mutableMapOf<Int, MutableList<ArmorStand>>()
    private var nextPathId = 1
    var standsAreVisible: Boolean = true

    /**
     * Create a new path
     */
    fun createPath(name: String, startPoint: Location, endPoint: Location): Int {
        val pathId = nextPathId++
        val path = PathData(pathId, name, startPoint, mutableListOf(), endPoint, true)
        paths[pathId] = path

        // Create armor stands for visualization
        createPathVisualization(pathId)

        return pathId
    }

    /**
     * Add a checkpoint to a path
     */
    fun addCheckpointToPath(pathId: Int, checkpoint: Location): Boolean {
        val path = paths[pathId] ?: return false
        path.checkpoints.add(checkpoint)

        // Update visualization
        updatePathVisualization(pathId)

        return true
    }

    /**
     * Remove a checkpoint from a path
     */
    fun removeCheckpointFromPath(pathId: Int, checkpointIndex: Int): Boolean {
        val path = paths[pathId] ?: return false
        if (checkpointIndex < 0 || checkpointIndex >= path.checkpoints.size) return false

        path.checkpoints.removeAt(checkpointIndex)

        // Update visualization
        updatePathVisualization(pathId)

        return true
    }

    /**
     * Delete a path
     */
    fun deletePath(pathId: Int): Boolean {
        paths.remove(pathId) ?: return false

        // Remove armor stands
        pathArmorStands[pathId]?.forEach { it.remove() }
        pathArmorStands.remove(pathId)

        return true
    }

    /**
     * Get a random path that is visible/enabled
     */
    fun getRandomPath(): PathData? {
        val enabledPaths = paths.values.filter { it.isVisible }
        if (enabledPaths.isEmpty()) return null
        return enabledPaths.random()
    }

    /**
     * Get all paths
     */
    fun getAllPaths(): List<PathData> {
        return paths.values.toList()
    }

    /**
     * Get a specific path
     */
    fun getPath(pathId: Int): PathData? {
        return paths[pathId]
    }

    /**
     * Toggle path visibility
     */
    fun togglePathVisibility(pathId: Int): Boolean {
        val path = paths[pathId] ?: return false
        path.isVisible = !path.isVisible

        // Update armor stand visibility
        pathArmorStands[pathId]?.forEach { stand ->
            stand.isInvisible = !path.isVisible
        }

        return true
    }

    /**
     * Rename a path
     */
    fun renamePath(pathId: Int, newName: String): Boolean {
        val path = paths[pathId] ?: return false
        path.name = newName
        return true
    }

    /**
     * Clear all paths
     */
    fun clearAllPaths() {
        pathArmorStands.values.flatten().forEach { it.remove() }
        pathArmorStands.clear()
        paths.clear()
        nextPathId = 1
    }

    /**
     * Create armor stand visualization for a path
     */
    private fun createPathVisualization(pathId: Int) {
        val path = paths[pathId] ?: return
        val stands = mutableListOf<ArmorStand>()

        // Create start point stand
        val startStand = path.startPoint.world.spawn(path.startPoint, ArmorStand::class.java) { stand ->
            stand.isVisible = standsAreVisible && path.isVisible
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.customName(Component.text("§a§lSTART - ${path.name}"))
            stand.isCustomNameVisible = true
            stand.persistentDataContainer.set(
                TowerDefMC.ELEMENT_TYPES,
                PersistentDataType.STRING,
                "PathStart"
            )
        }
        stands.add(startStand)

        // Create checkpoint stands
        path.checkpoints.forEachIndexed { index, checkpoint ->
            val checkpointStand = checkpoint.world.spawn(checkpoint, ArmorStand::class.java) { stand ->
                stand.isVisible = standsAreVisible && path.isVisible
                stand.setGravity(false)
                stand.isInvulnerable = true
                stand.customName(Component.text("§e§lCHECKPOINT ${index + 1} - ${path.name}"))
                stand.isCustomNameVisible = true
                stand.persistentDataContainer.set(
                    TowerDefMC.ELEMENT_TYPES,
                    PersistentDataType.STRING,
                    "PathCheckpoint"
                )
            }
            stands.add(checkpointStand)
        }

        // Create end point stand
        val endStand = path.endPoint.world.spawn(path.endPoint, ArmorStand::class.java) { stand ->
            stand.isVisible = standsAreVisible && path.isVisible
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.customName(Component.text("§c§lEND - ${path.name}"))
            stand.isCustomNameVisible = true
            stand.persistentDataContainer.set(
                TowerDefMC.ELEMENT_TYPES,
                PersistentDataType.STRING,
                "PathEnd"
            )
        }
        stands.add(endStand)

        pathArmorStands[pathId] = stands
    }

    /**
     * Update visualization when path changes
     */
    private fun updatePathVisualization(pathId: Int) {
        // Remove old stands
        pathArmorStands[pathId]?.forEach { it.remove() }
        pathArmorStands.remove(pathId)

        // Create new stands
        createPathVisualization(pathId)
    }

    /**
     * Serialize paths for saving
     */
    fun serializePaths(): List<SerializablePathData> {
        return paths.values.map { path ->
            SerializablePathData(
                id = path.id,
                name = path.name,
                startPoint = toSerializable(path.startPoint),
                checkpoints = path.checkpoints.map { toSerializable(it) },
                endPoint = toSerializable(path.endPoint),
                isVisible = path.isVisible
            )
        }
    }

    /**
     * Load paths from serialized data
     */
    fun loadPaths(serializedPaths: List<SerializablePathData>) {
        clearAllPaths()

        serializedPaths.forEach { serialized ->
            val path = PathData(
                id = serialized.id,
                name = serialized.name,
                startPoint = fromSerializable(serialized.startPoint),
                checkpoints = serialized.checkpoints.map { fromSerializable(it) }.toMutableList(),
                endPoint = fromSerializable(serialized.endPoint),
                isVisible = serialized.isVisible
            )
            paths[path.id] = path

            if (path.id >= nextPathId) {
                nextPathId = path.id + 1
            }

            // Create visualization
            createPathVisualization(path.id)
        }
    }

    private fun toSerializable(location: Location): SerializableLocation {
        return SerializableLocation(
            world = location.world.name,
            x = location.x,
            y = location.y,
            z = location.z,
            yaw = location.yaw,
            pitch = location.pitch
        )
    }

    private fun fromSerializable(serializable: SerializableLocation): Location {
        val world = Bukkit.getWorld(serializable.world) ?: throw IllegalStateException("World ${serializable.world} not found")
        return Location(world, serializable.x, serializable.y, serializable.z, serializable.yaw, serializable.pitch)
    }

    /**
     * Toggle global visibility of all path stands
     */
    fun toggleGlobalStandVisibility(): Boolean {
        standsAreVisible = !standsAreVisible

        pathArmorStands.values.flatten().forEach { stand ->
            val pathId = pathArmorStands.entries.find { it.value.contains(stand) }?.key
            val path = pathId?.let { paths[it] }
            stand.isInvisible = !standsAreVisible || (path != null && !path.isVisible)
        }

        return standsAreVisible
    }
}
