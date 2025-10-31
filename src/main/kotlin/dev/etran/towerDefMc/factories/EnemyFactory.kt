package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.createHealthBar
import net.kyori.adventure.util.TriState
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Enemy
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object EnemyFactory {
    fun newBasicEnemy(amount: Int = 1): ItemStack {
        val enemySpawn = ItemStack(Material.REDSTONE_BLOCK, amount)

        // Get the current item metadata (which is mutable)
        val meta = enemySpawn.itemMeta ?: return enemySpawn // Fallback if meta cannot be retrieved

        // Add identifier for the end rod to make sure it is a spawn object and not just an end rod
        meta.persistentDataContainer.set(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Enemy 1")

        enemySpawn.itemMeta = meta

        return enemySpawn
    }

    fun enemyPlace(event: PlayerInteractEvent) {
        event.isCancelled = true

        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val player = event.player

        val world = location.world
        val entity = world.spawnEntity(location, EntityType.ZOMBIE) as Zombie

        val scale = entity.getAttribute(Attribute.SCALE)

        // Base value is a multiplier from the normal value, 2 is double size
        if (scale != null) scale.baseValue = 1.5
        entity.setAI(false)
        entity.setAdult()
        entity.isInvulnerable = false
        entity.fireTicks = 0
        entity.noDamageTicks = 0
        entity.visualFire = TriState.TRUE
        entity.isPersistent = true
        entity.isSilent = true
        entity.isCollidable = false
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Enemy")
        entity.persistentDataContainer.set(TowerDefMC.ENEMY_TYPES, PersistentDataType.STRING, "Basic_Enemy_1")
        entity.persistentDataContainer.set(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, 1)

        createHealthBar(entity)

        // Take away 1 from the user if they aren't in creative or spectator mode.
        if (player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }

    fun enemyPlace(enemyType: String, location: Location) {
        // Get enemy configuration from registry
        val enemyConfig = dev.etran.towerDefMc.registries.EnemyRegistry.getEnemy(enemyType)

        if (enemyConfig == null) {
            // Fallback to basic enemy if type not found
            println("Warning: Enemy type '$enemyType' not found in registry, using default")
        }

        val world = location.world
        val entity = world.spawnEntity(location, EntityType.ZOMBIE) as Zombie

        val scale = entity.getAttribute(Attribute.SCALE)
        val maxHealth = entity.getAttribute(Attribute.MAX_HEALTH)
        val speed = entity.getAttribute(Attribute.MOVEMENT_SPEED)

        // Apply enemy-specific attributes from config
        if (enemyConfig != null) {
            if (scale != null) scale.baseValue = 1.5
            if (maxHealth != null) maxHealth.baseValue = enemyConfig.health
            if (speed != null) speed.baseValue = enemyConfig.speed * 0.1 // Scale to Minecraft values
            entity.health = enemyConfig.health
        } else {
            // Default values
            if (scale != null) scale.baseValue = 1.5
            if (maxHealth != null) maxHealth.baseValue = 20.0
            entity.health = 20.0
        }

        entity.setAI(false)
        entity.setAdult()
        entity.isInvulnerable = false
        entity.fireTicks = 0
        entity.noDamageTicks = 0
        entity.visualFire = TriState.TRUE
        entity.isPersistent = true
        entity.isSilent = true
        entity.isCollidable = false
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Enemy")
        entity.persistentDataContainer.set(TowerDefMC.ENEMY_TYPES, PersistentDataType.STRING, enemyType)
        entity.persistentDataContainer.set(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, 1)

        createHealthBar(entity)
    }
}