package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.data.SerializableLocation
import dev.etran.towerDefMc.data.SerializableSpawnableSurfaceData
import dev.etran.towerDefMc.data.SpawnableSurfaceData
import dev.etran.towerDefMc.utils.DebugLogger
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material

/**
 * Manages spawnable surfaces for tower placement
 */
class SpawnableSurfaceManager {
    private val surfaces = mutableMapOf<Int, SpawnableSurfaceData>()
    private var nextSurfaceId = 1

    /**
     * Create a new spawnable surface
     */
    fun createSurface(name: String, material: Material): Int {
        val surfaceId = nextSurfaceId++
        val surface = SpawnableSurfaceData(surfaceId, name, material)
        surfaces[surfaceId] = surface

        DebugLogger.logGame("Created new spawnable surface $surfaceId: '$name' with material $material")
        return surfaceId
    }

    /**
     * Add a location to a spawnable surface
     */
    fun addLocation(surfaceId: Int, location: Location): Boolean {
        val surface = surfaces[surfaceId] ?: return false
        surface.locations.add(location)

        // Place the invisible barrier block at this location
        location.block.type = surface.material

        DebugLogger.logGame("Added location to spawnable surface $surfaceId (total locations: ${surface.locations.size})")
        return true
    }

    /**
     * Remove a location from a spawnable surface
     */
    fun removeLocation(surfaceId: Int, location: Location): Boolean {
        val surface = surfaces[surfaceId] ?: return false
        val removed = surface.locations.removeIf { loc ->
            loc.world == location.world &&
            loc.blockX == location.blockX &&
            loc.blockY == location.blockY &&
            loc.blockZ == location.blockZ
        }

        if (removed) {
            // Remove the block at this location
            location.block.type = Material.AIR
            DebugLogger.logGame("Removed location from spawnable surface $surfaceId")
        }

        return removed
    }

    /**
     * Delete a spawnable surface
     */
    fun deleteSurface(surfaceId: Int): Boolean {
        val deletedSurface = surfaces.remove(surfaceId) ?: return false

        // Remove all blocks for this surface
        deletedSurface.locations.forEach { location ->
            location.block.type = Material.AIR
        }

        DebugLogger.logGame("Deleted spawnable surface $surfaceId: '${deletedSurface.name}'")
        return true
    }

    /**
     * Get all spawnable surfaces
     */
    fun getAllSurfaces(): List<SpawnableSurfaceData> {
        return surfaces.values.toList()
    }

    /**
     * Get a specific spawnable surface
     */
    fun getSurface(surfaceId: Int): SpawnableSurfaceData? {
        return surfaces[surfaceId]
    }

    /**
     * Check if a location is a valid spawnable surface
     */
    fun isValidSpawnLocation(location: Location): Boolean {
        // If no surfaces are defined, allow placement anywhere (default behavior)
        if (surfaces.isEmpty()) {
            return true
        }

        // Check if the location matches any spawnable surface
        return surfaces.values.any { surface ->
            surface.locations.any { loc ->
                loc.world == location.world &&
                loc.blockX == location.blockX &&
                loc.blockY == location.blockY &&
                loc.blockZ == location.blockZ
            }
        }
    }

    /**
     * Clear all spawnable surfaces
     */
    fun clearAllSurfaces() {
        surfaces.values.forEach { surface ->
            surface.locations.forEach { location ->
                location.block.type = Material.AIR
            }
        }
        surfaces.clear()
        nextSurfaceId = 1
    }

    /**
     * Serialize spawnable surfaces for saving
     */
    fun serializeSurfaces(): List<SerializableSpawnableSurfaceData> {
        return surfaces.values.map { surface ->
            SerializableSpawnableSurfaceData(
                id = surface.id,
                name = surface.name,
                material = surface.material.name,
                locations = surface.locations.map { loc ->
                    SerializableLocation(
                        world = loc.world.name,
                        x = loc.x,
                        y = loc.y,
                        z = loc.z,
                        yaw = loc.yaw,
                        pitch = loc.pitch
                    )
                }
            )
        }
    }

    /**
     * Load spawnable surfaces from serialized data
     */
    fun loadSurfaces(serializedSurfaces: List<SerializableSpawnableSurfaceData>) {
        clearAllSurfaces()

        serializedSurfaces.forEach { serialized ->
            val material = try {
                Material.valueOf(serialized.material)
            } catch (e: IllegalArgumentException) {
                DebugLogger.logGame("Warning: Invalid material ${serialized.material} for surface ${serialized.id}, using BARRIER")
                Material.BARRIER
            }

            val surface = SpawnableSurfaceData(
                id = serialized.id,
                name = serialized.name,
                material = material
            )

            serialized.locations.forEach { loc ->
                val world = Bukkit.getWorld(loc.world)
                if (world != null) {
                    val location = Location(world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                    surface.locations.add(location)
                    // Place the block
                    location.block.type = material
                }
            }

            surfaces[surface.id] = surface

            if (surface.id >= nextSurfaceId) {
                nextSurfaceId = surface.id + 1
            }
        }

        DebugLogger.logGame("Loaded ${serializedSurfaces.size} spawnable surfaces")
    }

    /**
     * Rename a spawnable surface
     */
    fun renameSurface(surfaceId: Int, newName: String): Boolean {
        val surface = surfaces[surfaceId] ?: return false
        surface.name = newName
        return true
    }

    /**
     * Get all locations for all spawnable surfaces (for highlighting)
     */
    fun getAllSpawnableLocations(): List<Location> {
        return surfaces.values.flatMap { it.locations }
    }
}

