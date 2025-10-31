package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.TaskUtility
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import org.bukkit.Material

object PlayerHoldListener : Listener {
    private val activeMouseTasks = mutableMapOf<UUID, BukkitTask>()
    private val TOWER_RANGE_KEY = TowerDefMC.TOWER_RANGE
    private val particleTickRate: Long = 5L

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val playerId = player.uniqueId
        val isTaskRunning = activeMouseTasks.containsKey(playerId)

        val newItem = player.inventory.getItem(event.newSlot)
        val towerRange: Double? =
            newItem?.itemMeta?.persistentDataContainer?.get(TOWER_RANGE_KEY, PersistentDataType.DOUBLE)
        val towerMaterial: Material? = newItem?.type

        val isTowerItem = towerRange != null && towerMaterial != null

        if (isTowerItem && !isTaskRunning) {
            // Case A: Switching TO a tower item -> START the preview task
            TaskUtility.startTargetHighlightTask(
                player, activeMouseTasks, particleTickRate, towerRange, towerMaterial
            )
        } else if (!isTowerItem && isTaskRunning) {
            // Case B: Switching AWAY from a tower item -> STOP the preview task
            TaskUtility.stopTargetHighlightTask(player, activeMouseTasks)
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val itemInHand = event.itemInHand

        // Check if the item placed was a tower (by checking for the range tag)
        val isTowerItem =
            itemInHand.itemMeta?.persistentDataContainer?.has(TOWER_RANGE_KEY, PersistentDataType.DOUBLE) ?: false

        if (isTowerItem && activeMouseTasks.containsKey(player.uniqueId)) {
            // If the player successfully placed a tower block while the task was running, stop it.
            TaskUtility.stopTargetHighlightTask(player, activeMouseTasks)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        TaskUtility.stopTargetHighlightTask(event.player, activeMouseTasks)
    }
}