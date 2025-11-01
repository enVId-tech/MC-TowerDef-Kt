package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.EnemyGeneratorData
import dev.etran.towerDefMc.data.TowerGeneratorData
import dev.etran.towerDefMc.factories.EnemyFactory
import dev.etran.towerDefMc.factories.StatsTrackerFactory
import dev.etran.towerDefMc.factories.TowerFactory
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.DebugLogger
import dev.etran.towerDefMc.utils.EntityAIDisabler
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

object PlayerPlaceListener : Listener {
    @EventHandler
    fun onPlayerPlace(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val gameElementSpawn = event.player.inventory.itemInMainHand

        gameElementSpawn.itemMeta?.let { meta: ItemMeta ->
            // Retrieve the name from the PDC
            val name = meta.persistentDataContainer.get(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING)

            if (name == null) return@let // Exit the 'let' block if tag is missing

            // Get the game the player is currently in
            val game = GameRegistry.getGameByPlayer(event.player.uniqueId)

            if (game == null) {
                DebugLogger.logGame("Player ${event.player.name} tried to place $name but is not in a game")
                event.player.sendMessage("You must be in a game to place game elements.")
                return
            }

            DebugLogger.logGame("Player ${event.player.name} placing $name in game ${game.gameId}")

            // Run functions specific to their unique identifiers
            when (name) {
                "Tower 1" -> TowerFactory.towerPlace(event)
                "Enemy 1" -> EnemyFactory.enemyPlace(event)
                "Stats_Tracker" -> StatsTrackerFactory.placeStatsTracker(event)
                "Generated_Enemy" -> {
                    // Handle custom generated enemy with AI completely disabled
                    val generatorDataString = meta.persistentDataContainer.get(
                        TowerDefMC.createKey("enemy_generator_data"), PersistentDataType.STRING
                    )
                    if (generatorDataString != null) {
                        val generatorData = EnemyGeneratorData.fromItemMetaString(generatorDataString)
                        if (generatorData != null) {
                            placeGeneratedEnemy(event, generatorData)
                        } else {
                            event.player.sendMessage("§cError: Invalid enemy data!")
                        }
                    }
                }
                "Generated_Tower" -> {
                    // Handle custom generated tower with AI completely disabled
                    val generatorDataString = meta.persistentDataContainer.get(
                        TowerDefMC.createKey("tower_generator_data"), PersistentDataType.STRING
                    )
                    if (generatorDataString != null) {
                        val generatorData = TowerGeneratorData.fromItemMetaString(generatorDataString)
                        if (generatorData != null) {
                            placeGeneratedTower(event, generatorData)
                        } else {
                            event.player.sendMessage("§cError: Invalid tower data!")
                        }
                    }
                }
                else -> {
                    DebugLogger.logGame("Unknown game element: $name")
                    event.player.sendMessage("This game element doesn't exist.")
                }
            }

            return
        }
    }

    private fun placeGeneratedEnemy(event: PlayerInteractEvent, data: EnemyGeneratorData) {
        event.isCancelled = true
        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val player = event.player

        val world = location.world
        val entity = world.spawnEntity(location, data.spawnEggType) as? org.bukkit.entity.LivingEntity

        if (entity == null) {
            player.sendMessage("§cFailed to spawn enemy!")
            return
        }

        // Apply attributes
        entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.baseValue = 1000.0
        entity.health = 1000.0
        entity.getAttribute(org.bukkit.attribute.Attribute.SCALE)?.baseValue = data.size
        entity.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED)?.baseValue = data.speed * 0.1

        // COMPREHENSIVE AI DISABLING - Critical for all mob types
        EntityAIDisabler.disableAllAI(entity)

        entity.isInvulnerable = false
        entity.fireTicks = 0
        entity.noDamageTicks = 0
        entity.visualFire = net.kyori.adventure.util.TriState.TRUE
        entity.isPersistent = true

        // Set baby mode if configured
        if (data.isBaby && entity is org.bukkit.entity.Ageable) {
            entity.setBaby()
        }

        // Store enemy data
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Enemy")
        entity.persistentDataContainer.set(TowerDefMC.ENEMY_TYPES, PersistentDataType.STRING, "Generated_Enemy")
        entity.persistentDataContainer.set(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, 1)
        entity.persistentDataContainer.set(TowerDefMC.createKey("custom_health"), PersistentDataType.DOUBLE, data.health)
        entity.persistentDataContainer.set(TowerDefMC.createKey("custom_max_health"), PersistentDataType.DOUBLE, data.health)

        dev.etran.towerDefMc.utils.createHealthBar(entity)

        player.sendMessage("§aGenerated enemy placed!")
        if (player.gameMode != org.bukkit.GameMode.CREATIVE && player.gameMode != org.bukkit.GameMode.SPECTATOR) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }

    private fun placeGeneratedTower(event: PlayerInteractEvent, data: TowerGeneratorData) {
        event.isCancelled = true
        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val player = event.player

        // Check if player is in an active game
        val game = GameRegistry.getGameByPlayer(player.uniqueId)
        if (game == null || !game.isGameRunning) {
            player.sendMessage("§cYou must be in an active game to place towers.")
            return
        }

        // Check spawnable surface
        if (!game.spawnableSurfaceManager.isValidSpawnLocation(block.location)) {
            player.sendMessage("§cYou can only place towers on designated spawnable surfaces!")
            return
        }

        // Check for nearby towers
        if (location.getNearbyEntities(1.0, 1.0, 1.0).any { entity ->
            entity.persistentDataContainer.get(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING) == "Tower"
        }) {
            player.sendMessage("§cYou cannot place a tower here! Too close to another tower.")
            return
        }

        val world = location.world
        val entity = world.spawnEntity(location, data.spawnEggType) as? org.bukkit.entity.LivingEntity

        if (entity == null) {
            player.sendMessage("§cFailed to spawn tower!")
            return
        }

        // Apply attributes
        entity.getAttribute(org.bukkit.attribute.Attribute.SCALE)?.baseValue = data.size

        // COMPREHENSIVE AI DISABLING - Critical for all mob types
        EntityAIDisabler.disableAllAI(entity)

        entity.isInvulnerable = true
        entity.fireTicks = 0
        entity.visualFire = net.kyori.adventure.util.TriState.FALSE
        entity.isPersistent = true

        // Set baby mode if configured
        if (data.isBaby && entity is org.bukkit.entity.Ageable) {
            entity.setBaby()
        }

        // Store tower data
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Tower")
        entity.persistentDataContainer.set(TowerDefMC.TOWER_TYPES, PersistentDataType.STRING, "Generated_Tower")
        entity.persistentDataContainer.set(TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, data.range)
        entity.persistentDataContainer.set(TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, data.damage)
        entity.persistentDataContainer.set(TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE, data.damageInterval)
        entity.persistentDataContainer.set(TowerDefMC.createKey("towerLevel"), PersistentDataType.INTEGER, 1)
        entity.persistentDataContainer.set(TowerDefMC.TOWER_OWNER_KEY, PersistentDataType.STRING, player.uniqueId.toString())
        entity.persistentDataContainer.set(TowerDefMC.createKey("tower_game_id"), PersistentDataType.INTEGER, game.gameId)

        // Register with game
        dev.etran.towerDefMc.managers.GameInstanceTracker.registerEntity(entity, game.gameId)

        player.sendMessage("§aGenerated tower placed! §7Sneak + Right-click to upgrade.")
        if (player.gameMode != org.bukkit.GameMode.CREATIVE && player.gameMode != org.bukkit.GameMode.SPECTATOR) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}