package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.updateHealthBar
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

// A separate class that implements Listener
object EnemyHealthListener : Listener {

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity !is LivingEntity) return
        val enemy = event.entity as LivingEntity

        // Check for your custom enemy type here (e.g., based on custom tags)
        if (!enemy.persistentDataContainer.has(TowerDefMC.ENEMY_TYPES, PersistentDataType.STRING)) return

        val damage = event.finalDamage

        val newHealth = enemy.health - damage

        val healthBar = enemy.getNearbyEntities(1.0, 2.0, 1.0)
            .filterIsInstance<TextDisplay>()
            .firstOrNull {
                it.passengers.contains(enemy)
                it.persistentDataContainer.get(TowerDefMC.HEALTH_OWNER_UUID, PersistentDataType.STRING) == enemy.uniqueId.toString()
            }
        if (healthBar != null) {
            // Update the text component with the new health
            updateHealthBar(enemy, healthBar, newHealth)
        }
    }
}