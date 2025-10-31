package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.entity.ArmorStand
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
    fun getEntitiesInGame(gameId: Int): Set<UUID> {
        return gameToEntities[gameId]?.toSet() ?: emptySet()
    }

    /**
     * Get all living entities in a specific game
     */
    fun getLivingEntitiesInGame(gameId: Int): List<LivingEntity> {
        val entityUUIDs = gameToEntities[gameId] ?: return emptyList()
        return entityUUIDs.mapNotNull { uuid ->
            plugin.server.worlds.asSequence()
                .flatMap { it.entities }
                .firstOrNull { it.uniqueId == uuid } as? LivingEntity
        }
    }

    /**
     * Clear all entities for a specific game (when game ends)
     */
    fun clearGame(gameId: Int) {
        gameToEntities[gameId]?.forEach { entityUUID ->
            entityToGame.remove(entityUUID)
        }
        gameToEntities.remove(gameId)
    }

    /**
     * Clear all tracked entities
     */
    fun clearAll() {
        entityToGame.clear()
        gameToEntities.clear()
    }
}

