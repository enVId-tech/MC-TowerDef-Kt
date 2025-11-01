package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Prevents tower defense enemies from targeting and attacking players
 */
class EnemyTargetListener : Listener {

    /**
     * Prevent enemies from targeting players
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityTarget(event: EntityTargetEvent) {
        val entity = event.entity

        // Check if this is a tower defense enemy
        if (entity !is Mob) return

        val isEnemy = entity.persistentDataContainer.has(
            TowerDefMC.ENEMY_TYPES,
            PersistentDataType.STRING
        )

        if (!isEnemy) return

        // If the enemy is trying to target a player, cancel it
        if (event.target is Player) {
            event.isCancelled = true
            entity.setTarget(null)
        }
    }

    /**
     * Prevent enemies from damaging players
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val victim = event.entity

        // Check if damager is a tower defense enemy
        if (damager !is Mob) return

        val isEnemy = damager.persistentDataContainer.has(
            TowerDefMC.ENEMY_TYPES,
            PersistentDataType.STRING
        )

        if (!isEnemy) return

        // If the enemy is trying to damage a player, cancel it
        if (victim is Player) {
            event.isCancelled = true
            // Also clear the target to be safe
            damager.setTarget(null)
        }
    }
}
