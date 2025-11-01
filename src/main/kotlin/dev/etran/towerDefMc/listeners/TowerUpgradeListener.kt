package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.TowerUpgradeConfig
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.menus.towers.TowerManagementMenu
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Handles tower upgrade interactions
 */
class TowerUpgradeListener : Listener {

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked

        // Check if the entity is a tower
        if (entity !is LivingEntity) return

        val elementType = entity.persistentDataContainer.get(
            TowerDefMC.ELEMENT_TYPES,
            PersistentDataType.STRING
        ) ?: return

        if (elementType != "Tower") return

        event.isCancelled = true

        // Get the game the player is in
        val game = GameRegistry.getGameByPlayer(player.uniqueId)
        if (game == null) {
            player.sendMessage("§cYou must be in a game to interact with towers!")
            return
        }

        // If player is sneaking, open the tower management menu
        if (!player.isSneaking) {
            val menu = TowerManagementMenu(player, entity, game.gameId)
            MenuListener.registerMenu(player, menu)
            menu.open()
            return
        }

        // Player must be sneaking to upgrade (quick upgrade without menu)
        // Get current tower level
        val currentLevel = entity.persistentDataContainer.getOrDefault(
            TowerDefMC.createKey("towerLevel"),
            PersistentDataType.INTEGER,
            1
        )

        val maxLevel = 5

        // Check if already at max level
        if (currentLevel >= maxLevel) {
            player.sendMessage("§cThis tower is already at maximum level!")
            return
        }

        // Get upgrade cost
        val upgradeCost = TowerUpgradeConfig.getUpgradeCost(currentLevel)

        // Check if player has enough cash
        val stats = PlayerStatsManager.getPlayerStats(game.gameId, player.uniqueId)
        if (stats == null) {
            player.sendMessage("§cError: Could not find your stats!")
            return
        }

        if (stats.cash < upgradeCost) {
            player.sendMessage("§cNot enough cash! Need §e$upgradeCost§c, you have §e${stats.cash}")
            return
        }

        // Upgrade the tower
        if (PlayerStatsManager.spendCash(game.gameId, player.uniqueId, upgradeCost)) {
            val newLevel = currentLevel + 1

            // Update tower level
            entity.persistentDataContainer.set(
                TowerDefMC.createKey("towerLevel"),
                PersistentDataType.INTEGER,
                newLevel
            )

            // Get base stats
            val baseDamage = entity.persistentDataContainer.getOrDefault(
                TowerDefMC.createKey("baseDamage"),
                PersistentDataType.DOUBLE,
                entity.persistentDataContainer.get(TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE) ?: 2.5
            )
            val baseRange = entity.persistentDataContainer.getOrDefault(
                TowerDefMC.createKey("baseRange"),
                PersistentDataType.DOUBLE,
                entity.persistentDataContainer.get(TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE) ?: 5.0
            )
            val baseSpeed = entity.persistentDataContainer.getOrDefault(
                TowerDefMC.createKey("baseSpeed"),
                PersistentDataType.DOUBLE,
                entity.persistentDataContainer.get(TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE) ?: 1.0
            )

            // Store base stats if this is the first upgrade
            if (currentLevel == 1) {
                entity.persistentDataContainer.set(
                    TowerDefMC.createKey("baseDamage"),
                    PersistentDataType.DOUBLE,
                    baseDamage
                )
                entity.persistentDataContainer.set(
                    TowerDefMC.createKey("baseRange"),
                    PersistentDataType.DOUBLE,
                    baseRange
                )
                entity.persistentDataContainer.set(
                    TowerDefMC.createKey("baseSpeed"),
                    PersistentDataType.DOUBLE,
                    baseSpeed
                )
            }

            // Apply multipliers
            val damageMultiplier = TowerUpgradeConfig.getDamageMultiplier(newLevel)
            val rangeMultiplier = TowerUpgradeConfig.getRangeMultiplier(newLevel)
            val speedMultiplier = TowerUpgradeConfig.getSpeedMultiplier(newLevel)

            entity.persistentDataContainer.set(
                TowerDefMC.TOWER_DMG,
                PersistentDataType.DOUBLE,
                baseDamage * damageMultiplier
            )
            entity.persistentDataContainer.set(
                TowerDefMC.TOWER_RANGE,
                PersistentDataType.DOUBLE,
                baseRange * rangeMultiplier
            )
            entity.persistentDataContainer.set(
                TowerDefMC.ATTACK_WAIT_TIME,
                PersistentDataType.DOUBLE,
                baseSpeed * speedMultiplier
            )

            // Update custom name to show level
            val towerType = entity.persistentDataContainer.get(
                TowerDefMC.TOWER_TYPES,
                PersistentDataType.STRING
            ) ?: "Tower"
            entity.customName(Component.text("§e★".repeat(newLevel) + " §6$towerType"))
            entity.isCustomNameVisible = true

            // Record upgrade
            PlayerStatsManager.recordTowerUpgraded(game.gameId, player.uniqueId)

            // Visual and sound effects
            entity.world.spawnParticle(
                Particle.HAPPY_VILLAGER,
                entity.location.add(0.0, 1.0, 0.0),
                20,
                0.5, 0.5, 0.5,
                0.1
            )
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)

            // Success message
            player.sendMessage("§a§l✓ §aTower upgraded to level §e$newLevel§a!")
            player.sendMessage("§7  Damage: §f${"+%.0f%%".format((damageMultiplier - 1) * 100)}")
            player.sendMessage("§7  Range: §f${"+%.0f%%".format((rangeMultiplier - 1) * 100)}")
            player.sendMessage("§7  Speed: §f${"+%.0f%%".format((speedMultiplier - 1) * 100)}")
            player.sendMessage("§7Cash remaining: §e${stats.cash - upgradeCost}")
        }
    }
}
