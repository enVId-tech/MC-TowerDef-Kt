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
    // The result is a Double (seconds * 1000.0), which converts to a Long.
    val delayMs = (delaySeconds * 1000.0).toLong()

    val nextReadyTime = currentTime + delayMs

    if (!enemy.isDead) {
        enemy.noDamageTicks = 0
        val damage = tower.persistentDataContainer.getOrDefault(
            TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, 5.0
        )

        // Award cash to the tower owner based on damage dealt
        val gameId = GameInstanceTracker.getGameId(tower)
        val towerOwnerUUID = tower.persistentDataContainer.get(
            TowerDefMC.TOWER_OWNER_KEY, PersistentDataType.STRING
        )

        if (gameId != null && towerOwnerUUID != null) {
            try {
                val ownerUUID = java.util.UUID.fromString(towerOwnerUUID)

                // Calculate actual damage that will be dealt (capped by remaining health)
                val actualDamage = min(damage, enemy.health)

                // Award cash equal to the actual damage dealt (rounded to nearest int)
                val cashReward = actualDamage.toInt()
                PlayerStatsManager.awardCash(gameId, ownerUUID, cashReward)

                // Record damage in stats
                PlayerStatsManager.recordDamage(gameId, ownerUUID, actualDamage)
            } catch (e: IllegalArgumentException) {
                // Invalid UUID format, skip cash reward
            }
        }

        // Apply damage directly to health instead of using damage() to avoid event issues
        enemy.health = max(0.0, enemy.health - damage)
    }

    tower.persistentDataContainer.set(TowerDefMC.READY_TIME, PersistentDataType.LONG, nextReadyTime)
}