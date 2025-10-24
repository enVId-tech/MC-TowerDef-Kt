package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.persistence.PersistentDataType

class FireproofListener : Listener {
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING) == null) return

        if (event.cause == EntityDamageEvent.DamageCause.FIRE ||
            event.cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
            event.cause == EntityDamageEvent.DamageCause.LAVA ||
            event.cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {

            event.isCancelled = true

            event.entity.fireTicks = 0
        }
    }
}