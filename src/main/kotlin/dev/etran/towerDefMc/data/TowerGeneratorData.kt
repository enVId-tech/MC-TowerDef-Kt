package dev.etran.towerDefMc.data

import org.bukkit.Material
import org.bukkit.entity.EntityType

/**
 * Represents tower generator configuration data
 */
data class TowerGeneratorData(
    val spawnEggType: EntityType,
    val displayName: String = "Custom Tower",
    val cost: Int = 100,
    val damage: Double = 5.0,
    val damageInterval: Double = 1.0, // seconds between attacks
    val range: Double = 5.0,
    val upgradePath: String = "none",
    // Mob properties
    val isBaby: Boolean = false,
    val size: Double = 1.0,
    val customModelData: Int? = null,
    val itemMaterial: Material = Material.ARROW
) {
    fun toItemMetaString(): String {
        return "$spawnEggType|$displayName|$cost|$damage|$damageInterval|$range|$upgradePath|$isBaby|$size|${customModelData ?: ""}|${itemMaterial.name}"
    }

    companion object {
        fun fromItemMetaString(data: String): TowerGeneratorData? {
            return try {
                val parts = data.split("|")
                if (parts.size < 9) return null

                TowerGeneratorData(
                    spawnEggType = EntityType.valueOf(parts[0]),
                    displayName = parts[1],
                    cost = parts[2].toInt(),
                    damage = parts[3].toDouble(),
                    damageInterval = parts[4].toDouble(),
                    range = parts[5].toDouble(),
                    upgradePath = parts[6],
                    isBaby = parts[7].toBoolean(),
                    size = parts[8].toDouble(),
                    customModelData = parts.getOrNull(9)?.toIntOrNull(),
                    itemMaterial = parts.getOrNull(10)?.let {
                        try { Material.valueOf(it) } catch (_: Exception) { Material.ARROW }
                    } ?: Material.ARROW
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
