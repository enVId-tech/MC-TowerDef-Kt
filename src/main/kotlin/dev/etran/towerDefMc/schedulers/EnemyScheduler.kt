package dev.etran.towerDefMc.schedulers

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.setMobTargetLocation
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object EnemyScheduler {
    fun checkAndHandleEnemies(world: World) {
        // Only get the entities in the world
        world.getEntitiesByClass(LivingEntity::class.java).forEach { entity ->
            val container = entity.persistentDataContainer

            // Only proceed if entity actually has the key
            if (!container.has(TowerDefMC.ENEMY_KEY, PersistentDataType.STRING)) return@forEach
            val enemyId = container.get(TowerDefMC.ENEMY_KEY, PersistentDataType.STRING) ?: return@forEach

            when (enemyId) {
                "Basic_Enemy_1" -> {
                    val targetArmorStand =
                        entity.getNearbyEntities(30.0, 30.0, 30.0).filterIsInstance<ArmorStand>().filter { armorStand ->
                            val standContainer = armorStand.persistentDataContainer
                            standContainer.has(TowerDefMC.GAME_ELEMENT_KEY, PersistentDataType.STRING) && standContainer.get(
                                TowerDefMC.GAME_ELEMENT_KEY, PersistentDataType.STRING
                            ) == "CheckPoint"
                        }.minByOrNull { armorStand ->
                            entity.location.distanceSquared(armorStand.location)
                        }
                    if (targetArmorStand == null) return
                    if (entity is Mob) setMobTargetLocation(entity, targetArmorStand.location, 1.0)
                }
            }
        }
    }
}