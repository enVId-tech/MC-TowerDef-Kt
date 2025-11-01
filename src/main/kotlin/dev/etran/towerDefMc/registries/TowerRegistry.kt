package dev.etran.towerDefMc.registries

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import java.io.File

object TowerRegistry {
    private lateinit var plugin: TowerDefMC
    private val towers: MutableMap<String, TowerType> = mutableMapOf()

    data class TowerType(
        val id: String,
        val displayName: String,
        val entityType: EntityType,
        val icon: Material,
        val description: List<String>,
        val range: Double,
        val damage: Double,
        val attackSpeed: Double
    )

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
        loadTowers()
    }

    private fun loadTowers() {
        val towersFile = File(plugin.dataFolder, "towers.yml")

        // Create default towers file if it doesn't exist
        if (!towersFile.exists()) {
            createDefaultTowersConfig(towersFile)
        }

        val config = YamlConfiguration.loadConfiguration(towersFile)

        config.getConfigurationSection("towers")?.getKeys(false)?.forEach { key ->
            val section = config.getConfigurationSection("towers.$key") ?: return@forEach

            val towerType = TowerType(
                id = key,
                displayName = section.getString("displayName") ?: key,
                entityType = EntityType.valueOf(section.getString("entityType") ?: "SKELETON"),
                icon = Material.valueOf(section.getString("icon") ?: "BOW"),
                description = section.getStringList("description"),
                range = section.getDouble("range", 5.0),
                damage = section.getDouble("damage", 2.5),
                attackSpeed = section.getDouble("attackSpeed", 1.0)
            )

            towers[key] = towerType
            plugin.logger.info("Loaded tower type: ${towerType.displayName}")
        }
    }

    private fun createDefaultTowersConfig(file: File) {
        file.parentFile?.mkdirs()

        val config = YamlConfiguration()

        // Basic Tower
        config.set("towers.Basic_Tower_1.displayName", "Basic Archer")
        config.set("towers.Basic_Tower_1.icon", "BOW")
        config.set("towers.Basic_Tower_1.entityType", "SKELETON")
        config.set(
            "towers.Basic_Tower_1.description", listOf(
                "Standard tower", "Medium range", "Low damage"
            )
        )
        config.set("towers.Basic_Tower_1.range", 5.0)
        config.set("towers.Basic_Tower_1.damage", 2.5)
        config.set("towers.Basic_Tower_1.attackSpeed", 1.0)

        // Sniper Tower
        config.set("towers.Sniper_Tower.displayName", "Sniper Tower")
        config.set("towers.Sniper_Tower.icon", "CROSSBOW")
        config.set("towers.Sniper_Tower.entityType", "SKELETON")
        config.set(
            "towers.Sniper_Tower.description", listOf(
                "Long range tower", "High range", "Medium damage", "Slow attack speed"
            )
        )
        config.set("towers.Sniper_Tower.range", 15.0)
        config.set("towers.Sniper_Tower.damage", 10.0)
        config.set("towers.Sniper_Tower.attackSpeed", 0.3)

        // Machine Gun Tower
        config.set("towers.Machine_Gun_Tower.displayName", "Machine Gun")
        config.set("towers.Machine_Gun_Tower.icon", "IRON_SHOVEL")
        config.set("towers.Machine_Gun_Tower.entityType", "BLAZE")
        config.set(
            "towers.Machine_Gun_Tower.description", listOf(
                "Fast firing tower", "Short range", "Low damage", "Very fast attack speed"
            )
        )
        config.set("towers.Machine_Gun_Tower.range", 4.0)
        config.set("towers.Machine_Gun_Tower.damage", 1.0)
        config.set("towers.Machine_Gun_Tower.attackSpeed", 5.0)

        // Cannon Tower
        config.set("towers.Cannon_Tower.displayName", "Cannon")
        config.set("towers.Cannon_Tower.icon", "TNT")
        config.set("towers.Cannon_Tower.entityType", "GHAST")
        config.set(
            "towers.Cannon_Tower.description", listOf(
                "Heavy damage tower", "Medium range", "Very high damage", "Slow attack speed"
            )
        )
        config.set("towers.Cannon_Tower.range", 7.0)
        config.set("towers.Cannon_Tower.damage", 25.0)
        config.set("towers.Cannon_Tower.attackSpeed", 0.2)

        // Support Tower
        config.set("towers.Support_Tower.displayName", "Support Tower")
        config.set("towers.Support_Tower.icon", "ENCHANTED_BOOK")
        config.set("towers.Support_Tower.entityType", "VILLAGER")
        config.set(
            "towers.Support_Tower.description", listOf(
                "Buff tower", "Short range", "No damage", "Buffs nearby towers"
            )
        )
        config.set("towers.Support_Tower.range", 6.0)
        config.set("towers.Support_Tower.damage", 0.0)
        config.set("towers.Support_Tower.attackSpeed", 0.5)

        config.save(file)
        plugin.logger.info("Created default towers configuration file")
    }

    fun getAllTowers(): Map<String, TowerType> {
        return towers.toMap()
    }

    fun getTower(id: String): TowerType? {
        return towers[id]
    }

    fun addGeneratedTower(id: String, towerType: TowerType) {
        towers[id] = towerType
        saveTowerToFile(id, towerType)
        plugin.logger.info("Added generated tower: ${towerType.displayName}")
    }

    private fun saveTowerToFile(id: String, towerType: TowerType) {
        val towersFile = File(plugin.dataFolder, "towers.yml")
        val config = YamlConfiguration.loadConfiguration(towersFile)

        config.set("towers.$id.displayName", towerType.displayName)
        config.set("towers.$id.icon", towerType.icon.name)
        config.set("towers.$id.entityType", towerType.entityType.name)
        config.set("towers.$id.description", towerType.description)
        config.set("towers.$id.range", towerType.range)
        config.set("towers.$id.damage", towerType.damage)
        config.set("towers.$id.attackSpeed", towerType.attackSpeed)

        config.save(towersFile)
    }
}
