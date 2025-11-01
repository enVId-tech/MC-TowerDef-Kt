package dev.etran.towerDefMc.data

import org.bukkit.Location

/**
 * Represents a single path with start point, checkpoints, and end point
 */
data class PathData(
    val id: Int,
    var name: String,
    var startPoint: Location,
    val checkpoints: MutableList<Location>,
    var endPoint: Location,
    var isVisible: Boolean = true
)

/**
 * Serializable version for saving to config
 */
data class SerializablePathData(
    val id: Int,
    val name: String,
    val startPoint: SerializableLocation,
    val checkpoints: List<SerializableLocation>,
    val endPoint: SerializableLocation,
    val isVisible: Boolean
)

data class SerializableLocation(
    val world: String, val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f
)

