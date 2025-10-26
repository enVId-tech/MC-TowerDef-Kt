package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.SortedMap
import java.util.TreeMap
import kotlin.collections.mapOf

object CheckpointManager {
    var checkpoints: SortedMap<Int, ArmorStand> = TreeMap<Int, ArmorStand>()
    var standsAreVisible: Boolean = true
    val effectType = PotionEffectType.GLOWING
    val duration = Int.MAX_VALUE
    val amplifier = 255

    private lateinit var plugin: Plugin

    /**
     * Sets plugin reference
     */
    fun initialize(pluginInstance: TowerDefMC) {
        plugin = pluginInstance
    }

    /**
     * @param entity The armor stand entity to insert
     * @return Int The integer representing the ID of the armor stnad
     */
    fun add(entity: ArmorStand): Int {

        var smallestAvailableId = 1

        for (id in checkpoints.keys) {
            if (id == smallestAvailableId) {
                smallestAvailableId++
            } else {
                break
            }
        }

        checkpoints[smallestAvailableId] = entity

        saveCheckpoints()
        return smallestAvailableId
    }

    /**
     * Removes an entity from the checkpoint list
     * Automatically readjusts IDs to be consecutive if removal was successful
     * @param entity The armor stand to remove
     * @return The ID of the armor stand if removal was successful, -1 if failed
     */
    fun remove(entity: ArmorStand): Int {

        val removeId = entity.persistentDataContainer.get(TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER)
        if (removeId == null) return -1

        val removedValue = checkpoints.remove(removeId)

//        entity.world.players.forEach { player -> player.sendMessage(checkpoints.toString()) }
//        entity.world.players.forEach { player -> player.sendMessage(removeId.toString()) }

        if (removedValue != null) {
            adjustArmorStandIds()
            saveCheckpoints()
            return removeId
        }

        saveCheckpoints()

        return -1
    }

    /**
     * Readjusts checkpoint list IDs to always be consecutive
     * @return A list of armors after readjustment of IDs
     */
    fun adjustArmorStandIds(): List<ArmorStand> {
        val entitiesInOrder: List<ArmorStand> = checkpoints.values.toList()

        val reIndexedCheckpoints: SortedMap<Int, ArmorStand> = TreeMap()

        entitiesInOrder.forEachIndexed { index, entity: ArmorStand ->
            val newId = index + 1

            // Update the Persistent Data Container (PDC) on the actual entity
            entity.persistentDataContainer.set(
                TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, newId
            )

            // Populate the new map
            reIndexedCheckpoints[newId] = entity
        }

        checkpoints = reIndexedCheckpoints

        return entitiesInOrder
    }

    /**
     * Clears existing checkpoints
     */
    fun clearAllCheckpoints(worlds: MutableList<World>): Boolean {
        try {
            checkpoints.clear()
            StartpointManager.startpoints.clear()
            saveCheckpoints()

            worlds.flatMap { world -> world.entities }.filterIsInstance<ArmorStand>().filter {
                it.persistentDataContainer.has(
                    TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER
                ) || it.persistentDataContainer.has(TowerDefMC.STARTPOINT_ID, PersistentDataType.INTEGER)
            }.forEach { it.remove() }
            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
    }

    /**
     * Toggle stand visibility
     */
    fun toggleStandVisibility(): Boolean {
        try {
            standsAreVisible = !standsAreVisible
            Bukkit.getWorlds().forEach { world ->
                world.entities.filterIsInstance<ArmorStand>().filter { entity ->
                    entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING) != null
                }.forEach { entity ->
                    if (standsAreVisible) {
                        entity.isInvisible = false
                        entity.addPotionEffect(PotionEffect(effectType, duration, amplifier))
                    } else {
                        entity.isInvisible = true
                        entity.removePotionEffect(effectType)
                    }
                }
            }

            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
    }

    /**
     * Save checkpoints to a YAML file
     */
    fun saveCheckpoints() {
        val file = File(plugin.dataFolder, "checkpoints.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        // Convert the current list of ArmorStands into a list of location maps
        val locationData = checkpoints.entries.map { (index, armorStand) ->
            val loc = armorStand.location
            mapOf(
                "world" to loc.world.name,
                "x" to loc.x,
                "y" to loc.y,
                "z" to loc.z,
                "index" to index, // Use the map's key as the index
                "isEndpoint" to armorStand.persistentDataContainer.get(
                    TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
                ).equals("EndPoint"),
                "isCheckpoint" to armorStand.persistentDataContainer.get(
                    TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING
                ).equals("Checkpoint"),
                "checkpointId" to armorStand.persistentDataContainer.get(
                    TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER
                )
            )
        }

        config.set("checkpoints", locationData)

        try {
            config.save(file)
        } catch (err: Exception) {
            err.printStackTrace()
            plugin.logger.severe { "Could not save checkpoints.yml: ${err.message}" }
        }
    }

    /**
     * Load checkpoints from file
     */
    fun loadCheckpoints() {
        checkpoints.clear()

        val file = File(plugin.dataFolder, "checkpoints.yml")

        val config = YamlConfiguration.loadConfiguration(file)

        val locationData = config.getMapList("checkpoints")

        locationData.forEach { map ->
            try {
                val worldName = map["world"] as? String ?: return@forEach
                val x = map["x"] as? Double ?: return@forEach
                val y = map["y"] as? Double ?: return@forEach
                val z = map["z"] as? Double ?: return@forEach
                val index = map["index"] as? Int ?: return@forEach
                val isEndpoint = map["isEndpoint"] as? Boolean ?: return@forEach
                val isCheckpoint = map["isCheckpoint"] as? Boolean ?: return@forEach
                val checkpointId = map["checkpointId"] as? Int ?: return@forEach

                val world = plugin.server.getWorld(worldName) ?: return@forEach
                val loc = Location(world, x, y, z)

                val existingEntity = loc.getNearbyEntities(0.1, 0.1, 0.1).filterIsInstance<ArmorStand>().firstOrNull()

                if (existingEntity != null) {
                    if (isEndpoint) existingEntity.persistentDataContainer.set(
                        TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "EndPoint"
                    )
                    if (isCheckpoint) existingEntity.persistentDataContainer.set(
                        TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Checkpoint"
                    )
                    existingEntity.persistentDataContainer.set(
                        TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, checkpointId
                    )
                    checkpoints[index] = existingEntity
                } else {
                    val entity: ArmorStand = loc.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand
                    if (isEndpoint) entity.persistentDataContainer.set(
                        TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "EndPoint"
                    )
                    if (isCheckpoint) entity.persistentDataContainer.set(
                        TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Checkpoint"
                    )
                    entity.persistentDataContainer.set(
                        TowerDefMC.CHECKPOINT_ID, PersistentDataType.INTEGER, checkpointId
                    )
                    checkpoints[index] = entity
                }

            } catch (err: Exception) {
                err.printStackTrace()
                plugin.logger.warning { "Error loading checkpoint entry: ${err.message}" }
            }
        }
        plugin.logger.info("Loaded ${checkpoints.size} checkpoints from file.")
    }
}