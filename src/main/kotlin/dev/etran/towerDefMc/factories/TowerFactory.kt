package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.registries.TowerRegistry
import dev.etran.towerDefMc.utils.DebugLogger
import dev.etran.towerDefMc.utils.EntityAIDisabler
import net.kyori.adventure.util.TriState
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Zombie
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object TowerFactory {
    fun newBasicTower(amount: Int = 1): ItemStack {
        val towerSpawn = ItemStack(Material.END_ROD, amount)
        val meta = towerSpawn.itemMeta ?: return towerSpawn

        // ATTACK_WAIT_TIME: Stored as DOUBLE (seconds)
        meta.persistentDataContainer.set(TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE, 0.2)

        // TOWER_DMG: Stored as DOUBLE (required for enemy.damage())
        meta.persistentDataContainer.set(TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, 2.5) // Change 10L to 10.0

        meta.persistentDataContainer.set(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Tower 1")
        meta.persistentDataContainer.set(TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, 5.0)

        towerSpawn.itemMeta = meta
        return towerSpawn
    }

    fun towerPlace(event: PlayerInteractEvent) {
        event.isCancelled = true

        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val player = event.player
        val itemHeld = player.inventory.itemInMainHand

        if (itemHeld.type == Material.AIR) {
            player.sendMessage("You are not holding a tower.")
            return
        }

        if (!itemHeld.itemMeta.persistentDataContainer.has(TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE)) {
            player.sendMessage("An error occurred. Please try again.")
            return
        }

        // Check if player is in an active game
        val game = GameRegistry.getGameByPlayer(player.uniqueId)
        if (game == null || !game.isGameRunning) {
            player.sendMessage("§cYou must be in an active game to place towers.")
            return
        }

        // Check if location is a valid spawnable surface (if surfaces are defined)
        if (!game.spawnableSurfaceManager.isValidSpawnLocation(block.location)) {
            player.sendMessage("§cYou can only place towers on designated spawnable surfaces!")
            return
        }

        // Check for nearby towers (within 1 block radius to prevent overlap)
        if (location.getNearbyEntities(1.0, 1.0, 1.0).any { entity ->
            entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING) == "Tower"
        }) {
            player.sendMessage("§cYou cannot place a tower here! Too close to another tower.")
            return
        }

        // Check if location is on an enemy path (check path armor stands)
        val pathCheckLocation = block.location
        if (pathCheckLocation.getNearbyEntities(1.5, 2.0, 1.5).any { entity ->
            val elementType = entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING)
            elementType == "PathStart" || elementType == "PathEnd" || elementType == "PathCheckpoint"
        }) {
            player.sendMessage("§cYou cannot place a tower on an enemy path!")
            return
        }

        DebugLogger.logTower("Player ${player.name} placing tower at ${location.blockX}, ${location.blockY}, ${location.blockZ}")

        val world = location.world

        // Get entity type from tower data if available, otherwise default to skeleton
        val itemMD_PDC = itemHeld.itemMeta.persistentDataContainer
        val towerTypeId = itemMD_PDC.get(TowerDefMC.TOWER_TYPES, PersistentDataType.STRING)

        // Try to get entity type from registry first, then fall back to default
        val entityType = if (towerTypeId != null) {
            val towerConfig = TowerRegistry.getTower(towerTypeId)
            towerConfig?.entityType ?: EntityType.SKELETON
        } else {
            EntityType.SKELETON // Default for basic towers
        }

        val entity = world.spawnEntity(location, entityType) as? LivingEntity
        if (entity == null) {
            player.sendMessage("§cFailed to spawn tower entity!")
            return
        }

        // COMPREHENSIVE AI DISABLING - prevents all mob abilities including:
        // - Wither shooting skulls
        // - Iron golems attacking
        // - Blazes shooting fireballs
        // - And all other vanilla mob behaviors
        EntityAIDisabler.disableAllAI(entity)

        entity.isInvulnerable = true
        entity.fireTicks = 0
        entity.visualFire = TriState.FALSE
        entity.isPersistent = true

        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Tower")
        entity.persistentDataContainer.set(TowerDefMC.TOWER_TYPES, PersistentDataType.STRING, "Basic_Tower_1")
        entity.persistentDataContainer.set(
            TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, itemMD_PDC.getOrDefault(
                TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, 5.0
            )
        )
        entity.persistentDataContainer.set(
            TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, itemMD_PDC.getOrDefault(
                TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, 5.0
            )
        )
        // ATTACK_WAIT_TIME: Retrieve and set as DOUBLE (seconds)
        entity.persistentDataContainer.set(
            TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE, itemMD_PDC.getOrDefault(
                TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE, 1.0 // Use DOUBLE defaults
            )
        )

        // Initialize tower at level 1
        entity.persistentDataContainer.set(
            TowerDefMC.createKey("towerLevel"), PersistentDataType.INTEGER, 1
        )

        // Store the tower owner (player who placed it)
        entity.persistentDataContainer.set(
            TowerDefMC.TOWER_OWNER_KEY, PersistentDataType.STRING, player.uniqueId.toString()
        )

        // Record tower placement in player stats and store game ID
        // Note: game variable already declared earlier for validation
        if (game != null) {
            // Ensure player stats are initialized before recording tower placement
            if (PlayerStatsManager.getPlayerStats(game.gameId, player.uniqueId) == null) {
                PlayerStatsManager.initializePlayer(game.gameId, player.uniqueId, game.config.defaultCash)
            }

            PlayerStatsManager.recordTowerPlaced(game.gameId, player.uniqueId)

            // Store the game ID on the tower entity so it can be cleaned up when game ends
            entity.persistentDataContainer.set(
                TowerDefMC.createKey("tower_game_id"), PersistentDataType.INTEGER, game.gameId
            )

            // Register the tower with GameInstanceTracker so cash rewards work
            dev.etran.towerDefMc.managers.GameInstanceTracker.registerEntity(entity, game.gameId)

            DebugLogger.logTower("Tower placed successfully by ${player.name} in game ${game.gameId}, UUID=${entity.uniqueId}")
        } else {
            DebugLogger.logTower("Warning: Tower placed by ${player.name} but no game found")
        }

        // Take away 1 from the user if they aren't in creative or spectator mode.
        if (player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            event.player.inventory.itemInMainHand.amount -= 1
        }

        player.sendMessage("§aTower placed! §7Sneak + Right-click to upgrade.")
    }
}