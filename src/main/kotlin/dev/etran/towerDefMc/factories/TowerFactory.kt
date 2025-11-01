package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.util.TriState
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType
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

        // TODO: Add this into a configuration file for the user to be able to make custom towers later on
        if (location.getNearbyEntities(0.5, 1.0, 0.5).count() >= 1) {
            player.sendMessage("You cannot place a tower here!")
            return
        }

        val world = location.world
        val entity = world.spawnEntity(location, EntityType.ZOMBIE) as Zombie
        val itemMD_PDC = itemHeld.itemMeta.persistentDataContainer

        entity.setAI(false)
        entity.setAdult()
        entity.isInvulnerable = true
        entity.fireTicks = 0
        entity.visualFire = TriState.FALSE
        entity.isPersistent = true
        entity.isSilent = true
        entity.isSilent = true
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
            TowerDefMC.createKey("towerLevel"),
            PersistentDataType.INTEGER,
            1
        )

        // Store the tower owner (player who placed it)
        entity.persistentDataContainer.set(
            TowerDefMC.TOWER_OWNER_KEY,
            PersistentDataType.STRING,
            player.uniqueId.toString()
        )

        // Record tower placement in player stats and store game ID
        val game = GameRegistry.getGameByPlayer(player.uniqueId)
        if (game != null) {
            PlayerStatsManager.recordTowerPlaced(game.gameId, player.uniqueId)

            // Store the game ID on the tower entity so it can be cleaned up when game ends
            entity.persistentDataContainer.set(
                TowerDefMC.createKey("tower_game_id"),
                PersistentDataType.INTEGER,
                game.gameId
            )

            // Register the tower with GameInstanceTracker so cash rewards work
            dev.etran.towerDefMc.managers.GameInstanceTracker.registerEntity(entity, game.gameId)
        }

        // Take away 1 from the user if they aren't in creative or spectator mode.
        if (player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            event.player.inventory.itemInMainHand.amount -= 1
        }

        player.sendMessage("§aTower placed! §7Sneak + Right-click to upgrade.")
    }
}