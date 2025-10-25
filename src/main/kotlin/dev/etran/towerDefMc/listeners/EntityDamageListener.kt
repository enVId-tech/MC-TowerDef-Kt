package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.persistence.PersistentDataType

class EntityDamageListener : Listener {
    @EventHandler
    fun whenEntityDamage(event: EntityDamageEvent) {
        if (event.isCancelled) return
        val gameElementSpawn = event.entity

        val gameElementId = gameElementSpawn.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING)

        if (gameElementId == null) return

        when (gameElementId) {
            "Enemy" ->

                return
            "Tower" ->
                return
        }
    }
}