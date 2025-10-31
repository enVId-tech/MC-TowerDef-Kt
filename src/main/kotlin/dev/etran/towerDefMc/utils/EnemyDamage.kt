package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType

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
        enemy.damage(
            tower.persistentDataContainer.getOrDefault(
                TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, 5.0
            )
        )
    }

    tower.persistentDataContainer.set(TowerDefMC.READY_TIME, PersistentDataType.LONG, nextReadyTime)
}