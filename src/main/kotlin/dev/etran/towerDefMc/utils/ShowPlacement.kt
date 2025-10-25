package dev.etran.towerDefMc.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.BoundingBox

fun visualizeTempParticles(plugin: JavaPlugin, centerLocation: Location, x: Double, z: Double, floorY: Int, durationTicks: Long) {

    val minX = (centerLocation.x - x)
    val maxX = (centerLocation.x + x)
    val minZ = (centerLocation.z - z)
    val maxZ = (centerLocation.z + z)

    val world = centerLocation.world

    val particleTask = object : BukkitRunnable() {
        override fun run() {
            // Loop through the X and Z range
            var currentX = minX
            while (currentX <= maxX) {
                var currentZ = minZ
                while (currentZ <= maxZ) {
                    world.spawnParticle(
                        Particle.FLAME,
                        currentX,
                        floorY + 0.05, // Slightly above the floor to ensure visibility
                        currentZ,
                        1, // Amount
                        0.0, 0.0, 0.0,
                        0.0
                    )
                    currentZ += 0.5
                }
                currentX += 0.5
            }
        }
    }

    particleTask.runTaskTimer(plugin, 0L, 5L)

    object : BukkitRunnable() {
        override fun run() {
            particleTask.cancel()
        }
    }.runTaskLater(plugin, durationTicks)
}