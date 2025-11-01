package dev.etran.towerDefMc.data

import org.bukkit.entity.EntityType

/**
 * Represents enemy generator configuration data
 */
data class EnemyGeneratorData(
    val spawnEggType: EntityType,
    val displayName: String = "Custom Enemy",
    val health: Double = 20.0,
    val speed: Double = 1.0,
    val defenseMultiplier: Double = 1.0, // Damage reduction multiplier
    val canBeStunned: Boolean = true,
    val canStunTowers: Boolean = false,
    val stunDuration: Double = 0.0, // seconds
    // Mob properties
    val isBaby: Boolean = false,
    val size: Double = 1.0,
    val customModelData: Int? = null
) {
    fun toItemMetaString(): String {
        return "$spawnEggType|$displayName|$health|$speed|$defenseMultiplier|$canBeStunned|$canStunTowers|$stunDuration|$isBaby|$size|${customModelData ?: ""}"
    }

    companion object {
        fun fromItemMetaString(data: String): EnemyGeneratorData? {
            return try {
                val parts = data.split("|")
                if (parts.size < 10) return null

                EnemyGeneratorData(
                    spawnEggType = EntityType.valueOf(parts[0]),
                    displayName = parts[1],
                    health = parts[2].toDouble(),
                    speed = parts[3].toDouble(),
                    defenseMultiplier = parts[4].toDouble(),
                    canBeStunned = parts[5].toBoolean(),
                    canStunTowers = parts[6].toBoolean(),
                    stunDuration = parts[7].toDouble(),
                    isBaby = parts[8].toBoolean(),
                    size = parts[9].toDouble(),
                    customModelData = parts.getOrNull(10)?.toIntOrNull()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

