package dev.etran.towerDefMc.utils

import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object TaskUtility {

    private lateinit var plugin: JavaPlugin

    /** * Initializes the TaskUtility object with the necessary plugin instance
     * for running Bukkit scheduler tasks.
     */
    fun initialize(pluginInstance: JavaPlugin) {
        this.plugin = pluginInstance
    }

    /**
     * Finds the block the player is currently targeting using a ray trace.
     * @param player The player to check the line of sight for.
     * @return The Block the player is looking at, or null.
     */
    fun getHighlightedBlock(player: Player): Block? {
        val maxDistance = 10.0
        val blockHit = player.world.rayTraceBlocks(
            player.eyeLocation, player.location.direction, maxDistance, FluidCollisionMode.NEVER, true
        )
        return blockHit?.hitBlock
    }

    /**
     * Draws a horizontal particle circle on a block surface.
     * @param center The center location on the block surface.
     * @param radius The radius of the circle in blocks.
     * @param points The number of points to use to draw the circle (higher = smoother).
     */
    private fun drawCircleParticles(
        center: Location, radius: Double, points: Int = 40
    ) {
        val world = center.world ?: return
        val angleStep = 2.0 * PI / points

        // Spawn slightly above the block surface to ensure visibility
        val y = center.y + 0.51

        for (i in 0 until points) {
            val angle = i * angleStep
            val x = center.x + radius * cos(angle)
            val z = center.z + radius * sin(angle)

            val particleLoc = Location(world, x, y, z)
            world.spawnParticle(Particle.SOUL, particleLoc, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    /**
     * Draws a temporary, faded block texture preview using BLOCK_CRACK particles.
     * @param center The center location on the block surface.
     * @param blockData The BlockData (texture) to display.
     */
    private fun drawBlockPreview(center: Location, blockData: BlockData) {
        val world = center.world ?: return

        world.spawnParticle(
            Particle.BLOCK_CRUMBLE, center.add(0.0, 0.5, 0.0), 150, // Amount (high count for a solid look)
            0.45, 0.45, 0.45, // Offset (covers most of the block's space)
            0.0, // Speed
            blockData // The block data to use for the texture
        )
    }

    /**
     * Starts a continuous task to highlight the placement block, draw the tower range, and show the block preview.
     * @param towerRange The range of the tower to display as a particle circle.
     * @param previewMaterial The material of the tower item to use for the preview texture.
     */
    fun startTargetHighlightTask(
        player: Player,
        activeMouseTasks: MutableMap<UUID, BukkitTask>,
        particleTickRate: Long,
        towerRange: Double,
        previewMaterial: Material
    ) {
        if (activeMouseTasks.containsKey(player.uniqueId)) {
            return
        }

        player.sendMessage("§bTower placement preview active. Range: $towerRange blocks.")

        val previewBlockData = previewMaterial.createBlockData()

        val task = object : BukkitRunnable() {
            // Use red dust for "No Placement" indicator
            val redDust = Particle.DustOptions(Color.RED, 1.0f)

            override fun run() {
                val targetBlock = getHighlightedBlock(player)

                if (targetBlock != null) {
                    val centerLoc = targetBlock.location.toCenterLocation()

                    if (targetBlock.type.isSolid) {
                        // Valid placement preview
                        drawCircleParticles(
                            centerLoc.clone(), towerRange, 10 * towerRange.toInt()
                        ) // Use clone to avoid modifying centerLoc
                        drawBlockPreview(centerLoc.clone(), previewBlockData)
                    } else {
                        // Invalid placement indicator
                        targetBlock.world.spawnParticle(
                            Particle.SOUL,
                            centerLoc.add(0.0, 0.5, 0.0),
                            15 * towerRange.toInt(), // Amount increased slightly
                            0.3,
                            0.3,
                            0.3,
                            0.0, // Small offset
                            redDust // Red X effect
                        )
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, particleTickRate)

        activeMouseTasks[player.uniqueId] = task
    }

    /** Stops the running block highlight task for a player. */
    fun stopTargetHighlightTask(player: Player, activeMouseTasks: MutableMap<UUID, BukkitTask>) {
        val task = activeMouseTasks.remove(player.uniqueId)
        task?.cancel()
        player.sendMessage("§7Tower placement preview stopped.")
    }
}