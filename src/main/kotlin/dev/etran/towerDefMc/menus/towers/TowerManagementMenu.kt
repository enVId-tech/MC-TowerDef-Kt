package dev.etran.towerDefMc.menus.towers

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.TowerUpgradeConfig
import dev.etran.towerDefMc.managers.GameInstanceTracker
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

class TowerManagementMenu(
    player: Player, private val tower: LivingEntity, private val gameId: Int
) : CustomMenu(player, 27, "Tower Management") {

    override fun setMenuItems() {
        val game = GameRegistry.allGames[gameId]
        if (game == null) {
            player.sendMessage("§cError: Game not found!")
            player.closeInventory()
            return
        }

        // Get tower stats
        val towerLevel = tower.persistentDataContainer.getOrDefault(
            TowerDefMC.createKey("towerLevel"), PersistentDataType.INTEGER, 1
        )

        val damage = tower.persistentDataContainer.getOrDefault(
            TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, 2.5
        )

        val range = tower.persistentDataContainer.getOrDefault(
            TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, 5.0
        )

        val attackSpeed = tower.persistentDataContainer.getOrDefault(
            TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE, 1.0
        )

        // Tower info display (center top)
        inventory.setItem(
            13, createMenuItem(
                Material.ENDER_EYE, "§6§lTower Information", listOf(
                    "§7Level: §e$towerLevel",
                    "§7Damage: §c$damage",
                    "§7Range: §a$range",
                    "§7Attack Interval: §b${attackSpeed}s",
                    "",
                    "§7Sneak + Right-click to upgrade"
                )
            )
        )

        // Upgrade button
        val maxLevel = 5
        if (towerLevel < maxLevel) {
            val upgradeCost = TowerUpgradeConfig.getUpgradeCost(towerLevel)
            val playerStats = PlayerStatsManager.getPlayerStats(gameId, player.uniqueId)
            val canAfford = playerStats != null && playerStats.cash >= upgradeCost

            inventory.setItem(
                11, createMenuItem(
                    Material.EMERALD, if (canAfford) "§a§lUpgrade Tower" else "§c§lUpgrade Tower", listOf(
                        "§7Upgrade to level §e${towerLevel + 1}",
                        "§7Cost: §e${upgradeCost}$",
                        "",
                        if (canAfford) "§aClick to upgrade!" else "§cNot enough cash!"
                    )
                )
            )
        } else {
            inventory.setItem(
                11, createMenuItem(
                    Material.GRAY_DYE, "§7§lMax Level", listOf("§7This tower is at maximum level")
                )
            )
        }

        // Sell button (bottom left - slot 18)
        val sellRefundPercentage = game.config.towerSellRefundPercentage
        val baseCost = 100 // TODO: Get actual tower cost from PDC if stored
        val refundAmount = (baseCost * sellRefundPercentage) / 100

        inventory.setItem(
            18, createMenuItem(
                Material.RED_DYE, "§c§lSell Tower", listOf(
                    "§7Remove this tower and get a refund",
                    "§7Refund: §e${refundAmount}$ §7(${sellRefundPercentage}%)",
                    "",
                    "§cClick to sell tower"
                )
            )
        )

        // Close button (bottom right - slot 26)
        inventory.setItem(
            26, createMenuItem(
                Material.BARRIER, "§cClose", listOf("§7Close this menu")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return

        when (event.slot) {
            11 -> handleUpgradeClick()
            18 -> handleSellClick()
            26 -> player.closeInventory()
        }
    }

    private fun handleUpgradeClick() {
        val game = GameRegistry.allGames[gameId]
        if (game == null) {
            player.sendMessage("§cError: Game not found!")
            return
        }

        // Get current tower level
        val currentLevel = tower.persistentDataContainer.getOrDefault(
            TowerDefMC.createKey("towerLevel"), PersistentDataType.INTEGER, 1
        )

        val maxLevel = 5

        if (currentLevel >= maxLevel) {
            player.sendMessage("§cThis tower is already at maximum level!")
            return
        }

        val upgradeCost = TowerUpgradeConfig.getUpgradeCost(currentLevel)

        // Check if player has enough cash
        val stats = PlayerStatsManager.getPlayerStats(gameId, player.uniqueId)
        if (stats == null) {
            player.sendMessage("§cError: Could not find your stats!")
            return
        }

        if (stats.cash < upgradeCost) {
            player.sendMessage("§cNot enough cash! Need §e$upgradeCost§c, you have §e${stats.cash}")
            return
        }

        // Upgrade the tower
        if (PlayerStatsManager.spendCash(gameId, player.uniqueId, upgradeCost)) {
            val newLevel = currentLevel + 1

            // Update tower level
            tower.persistentDataContainer.set(
                TowerDefMC.createKey("towerLevel"), PersistentDataType.INTEGER, newLevel
            )

            // Apply stat multipliers
            val damageMultiplier = TowerUpgradeConfig.getDamageMultiplier(newLevel)
            val rangeMultiplier = TowerUpgradeConfig.getRangeMultiplier(newLevel)
            val speedMultiplier = TowerUpgradeConfig.getSpeedMultiplier(newLevel)

            // Get base stats
            val baseDamage = tower.persistentDataContainer.getOrDefault(
                TowerDefMC.createKey("baseDamage"),
                PersistentDataType.DOUBLE,
                tower.persistentDataContainer.get(TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE) ?: 2.5
            )
            val baseRange = tower.persistentDataContainer.getOrDefault(
                TowerDefMC.createKey("baseRange"),
                PersistentDataType.DOUBLE,
                tower.persistentDataContainer.get(TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE) ?: 5.0
            )
            val baseSpeed = tower.persistentDataContainer.getOrDefault(
                TowerDefMC.createKey("baseSpeed"),
                PersistentDataType.DOUBLE,
                tower.persistentDataContainer.get(TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE) ?: 1.0
            )

            // Store base stats if not already stored
            if (!tower.persistentDataContainer.has(TowerDefMC.createKey("baseDamage"), PersistentDataType.DOUBLE)) {
                tower.persistentDataContainer.set(
                    TowerDefMC.createKey("baseDamage"), PersistentDataType.DOUBLE, baseDamage
                )
            }
            if (!tower.persistentDataContainer.has(TowerDefMC.createKey("baseRange"), PersistentDataType.DOUBLE)) {
                tower.persistentDataContainer.set(
                    TowerDefMC.createKey("baseRange"), PersistentDataType.DOUBLE, baseRange
                )
            }
            if (!tower.persistentDataContainer.has(TowerDefMC.createKey("baseSpeed"), PersistentDataType.DOUBLE)) {
                tower.persistentDataContainer.set(
                    TowerDefMC.createKey("baseSpeed"), PersistentDataType.DOUBLE, baseSpeed
                )
            }

            // Apply multipliers
            tower.persistentDataContainer.set(
                TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, baseDamage * damageMultiplier
            )
            tower.persistentDataContainer.set(
                TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, baseRange * rangeMultiplier
            )
            tower.persistentDataContainer.set(
                TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE, baseSpeed / speedMultiplier
            )

            // Record upgrade in stats
            PlayerStatsManager.recordTowerUpgraded(gameId, player.uniqueId)

            player.sendMessage("§a§lTower Upgraded!")
            player.sendMessage("§7Tower is now level §e$newLevel")
            player.sendMessage("§7Cost: §e$upgradeCost$")
            player.sendMessage("§7Remaining cash: §e${stats.cash}$")

            // Refresh menu
            setMenuItems()
        }
    }

    private fun handleSellClick() {
        val game = GameRegistry.allGames[gameId]
        if (game == null) {
            player.sendMessage("§cError: Game not found!")
            return
        }

        player.closeInventory()

        // Calculate refund
        val sellRefundPercentage = game.config.towerSellRefundPercentage
        val baseCost = 100 // TODO: Get actual cost from tower PDC if stored
        val refundAmount = (baseCost * sellRefundPercentage) / 100

        // Remove the tower
        tower.remove()

        // Refund money
        PlayerStatsManager.awardCash(gameId, player.uniqueId, refundAmount)

        // Remove from game instance tracker
        GameInstanceTracker.unregisterEntity(tower)

        player.sendMessage("§a§lTower Sold!")
        player.sendMessage("§7Refund: §e${refundAmount}$ §7(${sellRefundPercentage}%)")

        val stats = PlayerStatsManager.getPlayerStats(gameId, player.uniqueId)
        if (stats != null) {
            player.sendMessage("§7Total cash: §e${stats.cash}$")
        }
    }
}

