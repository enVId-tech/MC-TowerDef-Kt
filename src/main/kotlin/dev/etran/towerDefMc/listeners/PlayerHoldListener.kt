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

object PlayerHoldListener : Listener {
    private val activeMouseTasks = mutableMapOf<UUID, BukkitTask>()

    private val particleTickRate: Long = 5L // How often to check/re-draw particles

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val playerId = player.uniqueId

        val selectedBlock = TaskUtility.getHighlightedBlock(player)

        if (selectedBlock != null) return

        val newItem = player.inventory.getItem(event.newSlot)

        if (newItem == null) return

        val targetItem =
            newItem.itemMeta.persistentDataContainer.get(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING) != null

        val isTaskRunning = activeMouseTasks.containsKey(playerId)

        if (targetItem && !isTaskRunning) {
            // Case A: Switching TO the target item -> START highlighting task
            TaskUtility.startTargetHighlightTask(player, activeMouseTasks, particleTickRate)

        } else if (!targetItem && isTaskRunning) {
            // Case B: Switching AWAY from the target item -> STOP highlighting task
            // This handles switching to another item OR an empty slot.
            TaskUtility.stopTargetHighlightTask(player, activeMouseTasks)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        TaskUtility.stopTargetHighlightTask(event.player, activeMouseTasks)
    }
}