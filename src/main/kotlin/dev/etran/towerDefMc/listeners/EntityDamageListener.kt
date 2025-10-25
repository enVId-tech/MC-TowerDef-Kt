package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.CheckpointManager
import dev.etran.towerDefMc.managers.EndpointManager
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.persistence.PersistentDataType

class EntityDamageListener : Listener {
    @EventHandler
    fun whenEntityDamage(event: EntityDamageEvent) {
        if (event.isCancelled) return
        val gameElement = event.entity

        val gameElementId = gameElement.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING)

        if (gameElementId == null) return

        when (gameElementId) {
            "Checkpoint" ->
                if (gameElement is ArmorStand &&  gameElement.isDead) CheckpointManager.remove(gameElement)
            "EndPoint" ->
                if (gameElement is ArmorStand &&  gameElement.isDead) EndpointManager.remove(gameElement)
        }
    }
}