package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.TaskUtility
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import org.bukkit.Material // Import Material

object PlayerHoldListener : Listener {
    private val activeMouseTasks = mutableMapOf<UUID, BukkitTask>()

    // ASSUMPTION: TowerDefMC.TOWER_RANGE is the NamespacedKey for the range data
    private val TOWER_RANGE_KEY = TowerDefMC.TOWER_RANGE

    private val particleTickRate: Long = 5L // How often to check/re-draw particles

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val playerId = player.uniqueId
        val isTaskRunning = activeMouseTasks.containsKey(playerId)

        val newItem = player.inventory.getItem(event.newSlot)

        // Get the range (Double) and the material (Material)
        val towerRange: Double? = newItem?.itemMeta?.persistentDataContainer?.get(TOWER_RANGE_KEY, PersistentDataType.DOUBLE)
        val towerMaterial: Material? = newItem?.type // The material of the item itself

        // Check if the item is a tower (has a range)
        val isTowerItem = towerRange != null && towerMaterial != null // Ensure both are present

        if (isTowerItem && !isTaskRunning) {
            // Case A: Switching TO a tower item -> START the preview task
            TaskUtility.startTargetHighlightTask(
                player,
                activeMouseTasks,
                particleTickRate,
                towerRange,
                towerMaterial
            )

        } else if (!isTowerItem && isTaskRunning) {
            // Case B: Switching AWAY from a tower item -> STOP the preview task
            TaskUtility.stopTargetHighlightTask(player, activeMouseTasks)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        TaskUtility.stopTargetHighlightTask(event.player, activeMouseTasks)
    }
}