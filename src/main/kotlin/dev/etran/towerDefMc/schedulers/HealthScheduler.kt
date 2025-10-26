package dev.etran.towerDefMc.schedulers

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.TextDisplay
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

object HealthScheduler {
    fun checkAndHandleHealth(world: World) {
        world.getEntitiesByClass(TextDisplay::class.java).forEach { textDisplay ->

            val ownerUUIDString =
                textDisplay.persistentDataContainer.get(TowerDefMC.HEALTH_OWNER_UUID, PersistentDataType.STRING)

            if (ownerUUIDString != null) {
                val owner = Bukkit.getEntity(UUID.fromString(ownerUUIDString)) as? LivingEntity

                if (owner != null && !owner.isDead) {
                    textDisplay.teleport(owner.location.add(0.0, owner.height + 0.5, 0.0))
                } else {
                    textDisplay.remove()
                }
            }
        }
    }
}