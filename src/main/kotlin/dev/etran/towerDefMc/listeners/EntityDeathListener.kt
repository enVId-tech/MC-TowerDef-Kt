package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.CheckpointManager
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.persistence.PersistentDataType

class EntityDeathListener : Listener {
    @EventHandler
    fun whenEntityDamage(event: EntityDeathEvent) {
        val gameElement = event.entity

        val gameElementId = gameElement.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING)

        if (gameElementId == null) return
        when (gameElementId) {
            "Checkpoint" ->
                if (gameElement is ArmorStand) CheckpointManager.remove(gameElement)
            "EndPoint" ->
                if (gameElement is ArmorStand) CheckpointManager.remove(gameElement)
        }

        event.drops.clear()
    }
}