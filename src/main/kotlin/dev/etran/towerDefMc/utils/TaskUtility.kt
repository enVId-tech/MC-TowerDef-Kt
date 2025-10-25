package dev.etran.towerDefMc.utils

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import org.bukkit.block.Block
import org.bukkit.FluidCollisionMode
import org.bukkit.Particle

object TaskUtility {

    private lateinit var plugin: JavaPlugin

    /** * Initializes the TaskUtility object with the necessaryt plugin instance
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
        val maxDistance = 10.0 // Standard reach distance

        val blockHit = player.world.rayTraceBlocks(
            player.eyeLocation,
            player.location.direction,
            maxDistance,
            FluidCollisionMode.NEVER,
            true // Ignore passable blocks
        )

        return blockHit?.hitBlock
    }

    /**
     * Starts a continuous task to highlight the block the player is looking at.
     */
    fun startTargetHighlightTask(
        player: Player, activeMouseTasks: MutableMap<UUID, BukkitTask>, particleTickRate: Long
    ) {
        if (activeMouseTasks.containsKey(player.uniqueId)) {
            return
        }

        player.sendMessage("§bTarget highlighting activated.")

        val task = object : BukkitRunnable() {
            override fun run() {
                // Use the ray trace function to find the block the player is looking at
                val targetBlock = getHighlightedBlock(player)

                if (targetBlock != null) {
                    // Visualize the center of the targeted block with END_ROD particles
                    targetBlock.world.spawnParticle(
                        Particle.END_ROD,
                        // Spawn slightly above the center of the block
                        targetBlock.location.toCenterLocation().add(0.0, 0.5, 0.0), 5, // Amount
                        0.2, 0.2, 0.2, // Offset
                        0.0 // Extra (speed)
                    )
                }
            }
        }.runTaskTimer(plugin, 0L, particleTickRate)

        activeMouseTasks[player.uniqueId] = task
    }

    /** Stops the running block highlight task for a player. */
    fun stopTargetHighlightTask(player: Player, activeMouseTasks: MutableMap<UUID, BukkitTask>) {
        val task = activeMouseTasks.remove(player.uniqueId)
        task?.cancel()
        player.sendMessage("§7Target highlighting stopped.")
    }
}
