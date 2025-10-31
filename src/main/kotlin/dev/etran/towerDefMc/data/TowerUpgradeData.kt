package dev.etran.towerDefMc.data

/**
 * Represents tower upgrade information
 */
data class TowerUpgradeData(
    val level: Int = 1,
    val maxLevel: Int = 5,
    val damageMultiplier: Double = 1.0,
    val rangeMultiplier: Double = 1.0,
    val speedMultiplier: Double = 1.0
)

/**
 * Defines upgrade cost and stat increases per level
 */
object TowerUpgradeConfig {
    const val BASE_UPGRADE_COST = 100
    const val DAMAGE_INCREASE_PER_LEVEL = 0.25  // 25% increase per level
    const val RANGE_INCREASE_PER_LEVEL = 0.15   // 15% increase per level
    const val SPEED_INCREASE_PER_LEVEL = 0.20   // 20% increase per level

    fun getUpgradeCost(currentLevel: Int): Int {
        return BASE_UPGRADE_COST * currentLevel
    }

    fun getDamageMultiplier(level: Int): Double {
        return 1.0 + (DAMAGE_INCREASE_PER_LEVEL * (level - 1))
    }

    fun getRangeMultiplier(level: Int): Double {
        return 1.0 + (RANGE_INCREASE_PER_LEVEL * (level - 1))
    }

    fun getSpeedMultiplier(level: Int): Double {
        return 1.0 + (SPEED_INCREASE_PER_LEVEL * (level - 1))
    }
}

