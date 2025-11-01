package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.GameInstanceTracker
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType
import kotlin.math.max
import kotlin.math.min

fun damageEnemy(tower: LivingEntity, enemy: LivingEntity) {
    val currentTime = System.currentTimeMillis()

    // READY_TIME is still a LONG (timestamp in milliseconds)
    val readyTime = tower.persistentDataContainer.get(TowerDefMC.READY_TIME, PersistentDataType.LONG) ?: 0L

    if (currentTime < readyTime) {
        return
    }

    // Cooldown passed

    // Retrieve ATTACK_WAIT_TIME as DOUBLE (seconds)
    val delaySeconds = tower.persistentDataContainer.get(TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE) ?: 1.0

    // Convert seconds (Double) to milliseconds (Long)
    val delayMs = (delaySeconds * 1000.0).toLong()

    val nextReadyTime = currentTime + delayMs

    if (!enemy.isDead) {
        val damage = tower.persistentDataContainer.getOrDefault(
            TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, 5.0
        )

        // Get custom health from persistent data
        val currentCustomHealth = enemy.persistentDataContainer.get(
            TowerDefMC.createKey("custom_health"),
            PersistentDataType.DOUBLE
        )

        // If custom health is null, this enemy wasn't properly initialized - remove it
        if (currentCustomHealth == null) {
            println("Warning: Enemy ${enemy.uniqueId} had no custom health, removing it")

            // Clean up health bar
            cleanUpEnemyHealthBar(enemy)

            // Unregister from game tracker
            GameInstanceTracker.unregisterEntity(enemy)

            // Remove the entity
            enemy.remove()

            // Don't set ready time since we're removing the enemy, just return
            return
        }

        // Award cash to the tower owner based on damage dealt
        val gameId = GameInstanceTracker.getGameId(tower)
        val towerOwnerUUID = tower.persistentDataContainer.get(
            TowerDefMC.TOWER_OWNER_KEY, PersistentDataType.STRING
        )

        if (gameId != null && towerOwnerUUID != null) {
            try {
                val ownerUUID = java.util.UUID.fromString(towerOwnerUUID)

                // Calculate actual damage that will be dealt (capped by remaining custom health)
                val actualDamage = min(damage, currentCustomHealth)

                // Award cash equal to the actual damage dealt (rounded to nearest int)
                val cashReward = actualDamage.toInt()
                PlayerStatsManager.awardCash(gameId, ownerUUID, cashReward)

                // Record damage in stats
                PlayerStatsManager.recordDamage(gameId, ownerUUID, actualDamage)
            } catch (e: IllegalArgumentException) {
                // Invalid UUID format, skip cash reward
            }
        }

        // Apply damage to custom health ONLY (no vanilla damage)
        val newCustomHealth = max(0.0, currentCustomHealth - damage)
        enemy.persistentDataContainer.set(
            TowerDefMC.createKey("custom_health"),
            PersistentDataType.DOUBLE,
            newCustomHealth
        )

        // Update health bar with custom health - check passengers first since health bar is attached
        val healthBar = enemy.passengers.filterIsInstance<org.bukkit.entity.TextDisplay>()
            .firstOrNull { display ->
                display.persistentDataContainer.get(
                    TowerDefMC.HEALTH_OWNER_UUID,
                    PersistentDataType.STRING
                ) == enemy.uniqueId.toString()
            }

        if (healthBar != null) {
            val maxCustomHealth = enemy.persistentDataContainer.get(
                TowerDefMC.createKey("custom_max_health"),
                PersistentDataType.DOUBLE
            ) ?: 20.0
            updateHealthBar(enemy, healthBar, newCustomHealth, maxCustomHealth)
        }

        // If custom health reaches 0 or below, kill the enemy
        if (newCustomHealth <= 0.0) {
            // Clean up health bar
            cleanUpEnemyHealthBar(enemy)

            // Unregister from game tracker BEFORE removing
            GameInstanceTracker.unregisterEntity(enemy)

            // Track the kill
            if (gameId != null) {
                PlayerStatsManager.getAllPlayerStats(gameId).keys.forEach { playerUUID ->
                    PlayerStatsManager.recordKill(gameId, playerUUID)
                }
            }

            // Remove the entity (this is the only way the enemy dies - custom health reaching 0)
            enemy.remove()
        }
    }

    tower.persistentDataContainer.set(TowerDefMC.READY_TIME, PersistentDataType.LONG, nextReadyTime)
}