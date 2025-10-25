package dev.etran.towerDefMc.utils

import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
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

fun getHighlightedBlock(player: org.bukkit.entity.Player): Block? {
    // 1. Define the maximum distance a player can look (usually around 4-5 blocks)
    val maxDistance = 5.0

    // 2. Perform the ray trace
    val blockHit = player.world.rayTraceBlocks(
        player.eyeLocation,
        player.location.direction,
        maxDistance, // Max distance
        FluidCollisionMode.NEVER,
        true
    )

    return blockHit?.hitBlock
}