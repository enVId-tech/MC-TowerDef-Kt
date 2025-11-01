package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.cleanUpEnemyHealthBar
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.UUID

/**
 * Tracks which game instance each entity belongs to
 * This allows multiple games to run simultaneously with isolated entities
 */
object GameInstanceTracker {
    private lateinit var plugin: TowerDefMC

    // Maps entity UUID to game ID
    private val entityToGame: MutableMap<UUID, Int> = mutableMapOf()

    // Maps game ID to all entities in that game
    private val gameToEntities: MutableMap<Int, MutableSet<UUID>> = mutableMapOf()

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
    }

    /**
     * Register an entity as belonging to a specific game
     */
    fun registerEntity(entity: Entity, gameId: Int) {
        val entityUUID = entity.uniqueId

        // Remove from old game if already registered
        entityToGame[entityUUID]?.let { oldGameId ->
            gameToEntities[oldGameId]?.remove(entityUUID)
        }

        // Add to new game
        entityToGame[entityUUID] = gameId
        gameToEntities.getOrPut(gameId) { mutableSetOf() }.add(entityUUID)
    }

    /**
     * Unregister an entity (e.g., when it dies or is removed)
     */
    fun unregisterEntity(entity: Entity) {
        val entityUUID = entity.uniqueId
        entityToGame[entityUUID]?.let { gameId ->
            gameToEntities[gameId]?.remove(entityUUID)
        }
        entityToGame.remove(entityUUID)
    }

    /**
     * Get the game ID that an entity belongs to
     */
    fun getGameId(entity: Entity): Int? {
        return entityToGame[entity.uniqueId]
    }

    /**
     * Get all entities in a specific game
     */
    @Suppress("unused")
    fun getEntitiesInGame(gameId: Int): Set<UUID> {
        return gameToEntities[gameId]?.toSet() ?: emptySet()
    }

    /**
     * Get all living entities in a specific game
     */
    @Suppress("unused")
    fun getLivingEntitiesInGame(gameId: Int): List<LivingEntity> {
        val entityUUIDs = gameToEntities[gameId] ?: return emptyList()
        return entityUUIDs.mapNotNull { uuid ->
            plugin.server.worlds.asSequence().flatMap { it.entities }
                .firstOrNull { it.uniqueId == uuid } as? LivingEntity
        }.filter { entity ->
            // Only count entities that are actually alive and not dead
            !entity.isDead && entity.health > 0
        }
    }

    /**
     * Clear all entities for a specific game (when game ends)
     */
    fun clearGame(gameId: Int) {
        val entityUUIDs = gameToEntities[gameId] ?: emptySet()

        // Actually remove the entities from the world
        entityUUIDs.forEach { entityUUID ->
            plugin.server.worlds.asSequence().flatMap { it.entities }.firstOrNull { it.uniqueId == entityUUID }
                ?.let { entity ->
                    // Clean up health bar if it's an enemy
                    if (entity is LivingEntity) {
                        cleanUpEnemyHealthBar(entity)
                    }
                    // Remove the entity
                    entity.remove()
                }
            entityToGame.remove(entityUUID)
        }
        gameToEntities.remove(gameId)

        // Also remove all towers that belong to this game
        plugin.server.worlds.forEach { world ->
            world.entities.filterIsInstance<org.bukkit.entity.LivingEntity>().forEach { entity ->
                val towerGameId = entity.persistentDataContainer.get(
                    TowerDefMC.createKey("tower_game_id"), org.bukkit.persistence.PersistentDataType.INTEGER
                )
                if (towerGameId == gameId) {
                    entity.remove()
                }
            }
        }

        // Remove all tower items from players' inventories for this game
        plugin.server.onlinePlayers.forEach { player ->
            player.inventory.contents.forEachIndexed { index, item ->
                if (item != null && item.type != org.bukkit.Material.AIR) {
                    // Check if this is a tower item
                    val isTowerItem = item.itemMeta?.persistentDataContainer?.has(
                        TowerDefMC.TOWER_RANGE, org.bukkit.persistence.PersistentDataType.DOUBLE
                    ) == true

                    if (isTowerItem) {
                        // Remove the tower item from inventory
                        player.inventory.setItem(index, null)
                    }
                }
            }
            player.updateInventory()
        }
    }

    /**
     * Clear all tracked entities
     */
    @Suppress("unused")
    fun clearAll() {
        entityToGame.clear()
        gameToEntities.clear()
    }
}
