package dev.etran.towerDefMc.registries

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object EnemyRegistry {
    private lateinit var plugin: TowerDefMC
    private val enemies: MutableMap<String, EnemyType> = mutableMapOf()

    data class EnemyType(
        val id: String,
        val displayName: String,
        val icon: Material,
        val description: List<String>,
        val health: Double,
        val speed: Double,
        val damage: Int
    )

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
        loadEnemies()
    }

    private fun loadEnemies() {
        val enemiesFile = File(plugin.dataFolder, "enemies.yml")

        // Create default enemies file if it doesn't exist
        if (!enemiesFile.exists()) {
            createDefaultEnemiesConfig(enemiesFile)
        }

        val config = YamlConfiguration.loadConfiguration(enemiesFile)

        config.getConfigurationSection("enemies")?.getKeys(false)?.forEach { key ->
            val section = config.getConfigurationSection("enemies.$key") ?: return@forEach

            val enemyType = EnemyType(
                id = key,
                displayName = section.getString("displayName") ?: key,
                icon = Material.valueOf(section.getString("icon") ?: "ZOMBIE_HEAD"),
                description = section.getStringList("description"),
                health = section.getDouble("health", 20.0),
                speed = section.getDouble("speed", 1.0),
                damage = section.getInt("damage", 1)
            )

            enemies[key] = enemyType
            plugin.logger.info("Loaded enemy type: ${enemyType.displayName}")
        }
    }

    private fun createDefaultEnemiesConfig(file: File) {
        file.parentFile?.mkdirs()

        val config = YamlConfiguration()

        // Basic Enemy
        config.set("enemies.Basic_Enemy_1.displayName", "Basic Zombie")
        config.set("enemies.Basic_Enemy_1.icon", "ZOMBIE_HEAD")
        config.set(
            "enemies.Basic_Enemy_1.description", listOf(
                "Standard enemy", "Medium speed", "Low health"
            )
        )
        config.set("enemies.Basic_Enemy_1.health", 20.0)
        config.set("enemies.Basic_Enemy_1.speed", 1.0)
        config.set("enemies.Basic_Enemy_1.damage", 1)

        // Fast Enemy
        config.set("enemies.Fast_Enemy.displayName", "Fast Runner")
        config.set("enemies.Fast_Enemy.icon", "LEATHER_BOOTS")
        config.set(
            "enemies.Fast_Enemy.description", listOf(
                "Fast enemy", "High speed", "Low health"
            )
        )
        config.set("enemies.Fast_Enemy.health", 15.0)
        config.set("enemies.Fast_Enemy.speed", 2.0)
        config.set("enemies.Fast_Enemy.damage", 1)

        // Tank Enemy
        config.set("enemies.Tank_Enemy.displayName", "Tank")
        config.set("enemies.Tank_Enemy.icon", "IRON_CHESTPLATE")
        config.set(
            "enemies.Tank_Enemy.description", listOf(
                "Slow enemy", "Low speed", "High health"
            )
        )
        config.set("enemies.Tank_Enemy.health", 100.0)
        config.set("enemies.Tank_Enemy.speed", 0.5)
        config.set("enemies.Tank_Enemy.damage", 2)

        // Boss Enemy
        config.set("enemies.Boss_Enemy.displayName", "Boss")
        config.set("enemies.Boss_Enemy.icon", "WITHER_SKELETON_SKULL")
        config.set(
            "enemies.Boss_Enemy.description", listOf(
                "Boss enemy", "Medium speed", "Very high health"
            )
        )
        config.set("enemies.Boss_Enemy.health", 500.0)
        config.set("enemies.Boss_Enemy.speed", 0.8)
        config.set("enemies.Boss_Enemy.damage", 5)

        config.save(file)
        plugin.logger.info("Created default enemies configuration")
    }

    fun getAllEnemies(): List<EnemyType> {
        return enemies.values.toList()
    }

    fun getEnemy(id: String): EnemyType? {
        return enemies[id]
    }

    @Suppress("unused")
    fun reloadEnemies() {
        enemies.clear()
        loadEnemies()
    }
}
