package dev.etran.towerDefMc.data

import org.bukkit.Location
import org.bukkit.Material

/**
 * Represents a spawnable surface configuration
 */
data class SpawnableSurfaceData(
    val id: Int,
    var name: String,
    var material: Material,
    val locations: MutableList<Location> = mutableListOf()
)

/**
 * Serializable version for saving to config
 */
data class SerializableSpawnableSurfaceData(
    val id: Int,
    val name: String,
    val material: String,
    val locations: List<SerializableLocation>
)

