package dev.etran.towerDefMc.data

import java.util.UUID

/**
 * Tracks player statistics during a game session
 */
data class PlayerStats(
    val playerUUID: UUID,
    var cash: Int = 0,
    var kills: Int = 0,
    var towersPlaced: Int = 0,
    var towersUpgraded: Int = 0,
    var damageDealt: Double = 0.0,
    var wavesCompleted: Int = 0
) {
    fun addCash(amount: Int) {
        cash += amount
    }

    fun spendCash(amount: Int): Boolean {
        if (cash >= amount) {
            cash -= amount
            return true
        }
        return false
    }

    fun addKill() {
        kills++
    }

    fun addDamage(damage: Double) {
        damageDealt += damage
    }

    fun addTowerPlaced() {
        towersPlaced++
    }

    fun addTowerUpgraded() {
        towersUpgraded++
    }

    fun completeWave() {
        wavesCompleted++
    }
}

