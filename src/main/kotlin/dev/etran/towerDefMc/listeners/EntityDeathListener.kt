package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.CheckpointManager
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.persistence.PersistentDataType

object EntityDeathListener : Listener {
    @EventHandler
    fun whenEntityDamage(event: EntityDeathEvent) {
        val gameElement = event.entity

        val gameElementId = gameElement.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING)

        val enemyUuid = event.entity.persistentDataContainer.get(TowerDefMC.HEALTH_OWNER_UUID, PersistentDataType.STRING)

        if (gameElementId == null || enemyUuid == null) return

        when (gameElementId) {
            "Checkpoint" ->
                if (gameElement is ArmorStand) CheckpointManager.remove(gameElement)
            "EndPoint" ->
                if (gameElement is ArmorStand) CheckpointManager.remove(gameElement)
        }

        gameElement.passengers.forEach { passenger ->
            if (passenger is TextDisplay &&
                passenger.persistentDataContainer.has(TowerDefMC.HEALTH_OWNER_UUID, PersistentDataType.STRING))
            passenger.remove()
        }

        event.drops.clear()
    }
}