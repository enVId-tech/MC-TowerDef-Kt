package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameInstanceTracker
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.EntityTransformEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Prevents tower defense enemies from targeting and attacking players
 * Makes enemies invincible to all damage except tower damage
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

    /**
     * Make enemies invincible to all damage except tower damage
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity

        // Check if this is a tower defense enemy
        if (entity !is LivingEntity) return

        val isEnemy = entity.persistentDataContainer.has(
            TowerDefMC.ENEMY_TYPES,
            PersistentDataType.STRING
        )

        if (!isEnemy) return

        // If this is damage from an entity, check if it's from a tower
        if (event is EntityDamageByEntityEvent) {
            val damager = event.damager

            // Check if damager is a tower
            val isTower = damager.persistentDataContainer.has(
                TowerDefMC.TOWER_TYPES,
                PersistentDataType.STRING
            )

            // Only allow damage from towers
            if (!isTower) {
                event.isCancelled = true
            }
        } else {
            // Cancel all non-entity damage (fall, drown, fire, etc.)
            event.isCancelled = true
        }
    }

    /**
     * Handle entity transformation (e.g., zombie to drowned)
     * Preserve the enemy data and path target
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityTransform(event: EntityTransformEvent) {
        val originalEntity = event.entity

        // Check if the original entity is a tower defense enemy
        if (originalEntity !is LivingEntity) return

        val isEnemy = originalEntity.persistentDataContainer.has(
            TowerDefMC.ENEMY_TYPES,
            PersistentDataType.STRING
        )

        if (!isEnemy) return

        // Get the enemy type and target checkpoint
        val enemyType = originalEntity.persistentDataContainer.get(
            TowerDefMC.ENEMY_TYPES,
            PersistentDataType.STRING
        ) ?: return

        val targetCheckpointId = originalEntity.persistentDataContainer.get(
            TowerDefMC.TARGET_CHECKPOINT_ID,
            PersistentDataType.INTEGER
        ) ?: 1

        val gameId = GameInstanceTracker.getGameId(originalEntity)

        // Transfer data to the transformed entity
        event.transformedEntities.forEach { transformedEntity ->
            if (transformedEntity is LivingEntity) {
                // Mark as enemy
                transformedEntity.persistentDataContainer.set(
                    TowerDefMC.ELEMENT_TYPES,
                    PersistentDataType.STRING,
                    "Enemy"
                )

                // Set enemy type
                transformedEntity.persistentDataContainer.set(
                    TowerDefMC.ENEMY_TYPES,
                    PersistentDataType.STRING,
                    enemyType
                )

                // Set target checkpoint
                transformedEntity.persistentDataContainer.set(
                    TowerDefMC.TARGET_CHECKPOINT_ID,
                    PersistentDataType.INTEGER,
                    targetCheckpointId
                )

                // Make non-collidable
                transformedEntity.isCollidable = false

                // Clear target to prevent attacking players
                if (transformedEntity is Mob) {
                    transformedEntity.setTarget(null)
                }

                // Re-register with game instance tracker
                if (gameId != null) {
                    // Unregister old entity
                    GameInstanceTracker.unregisterEntity(originalEntity)
                    // Register new entity
                    GameInstanceTracker.registerEntity(transformedEntity, gameId)
                }

                // Create health bar for transformed entity
                dev.etran.towerDefMc.utils.createHealthBar(transformedEntity)
            }
        }
    }
}
