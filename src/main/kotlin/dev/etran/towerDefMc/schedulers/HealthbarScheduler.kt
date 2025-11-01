package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC.Companion.HEALTH_OWNER_UUID
import dev.etran.towerDefMc.utils.DebugLogger
import org.bukkit.World
import org.bukkit.entity.TextDisplay
import org.bukkit.persistence.PersistentDataType

object HealthbarScheduler {
    private var lastDebugTime = 0L
    private const val DEBUG_INTERVAL = 1000L

    fun updateHealthbars(world: World) {
        val currentTime = System.currentTimeMillis()
        val towerCount = 0

        world.entities.filterIsInstance<TextDisplay>().forEach { textDisplay ->
            val ownerUUID = textDisplay.persistentDataContainer.get(
                HEALTH_OWNER_UUID, PersistentDataType.STRING
            )

            if (ownerUUID != null) {
                // Check if the owner entity still exists
                val ownerExists = world.entities.any { it.uniqueId.toString() == ownerUUID }

                // If owner doesn't exist, remove this orphaned health bar
                if (!ownerExists) {
                    textDisplay.remove()
                }
            }
        }

        if (currentTime - lastDebugTime > DEBUG_INTERVAL && towerCount > 0) {
            DebugLogger.logTower("Active towers in world: $towerCount")
            lastDebugTime = currentTime
        }
    }
}