package dev.etran.towerDefMc.menus.shop

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.PlayerStatsManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.registries.TowerRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class TowerShopMenu(player: Player, private val gameId: Int) : CustomMenu(player, 54, "Tower Shop") {

    private var currentPage = 0
    private val towersPerPage = 28 // 4 rows minus control slots

    override fun setMenuItems() {
        inventory.clear()

        val playerStats = PlayerStatsManager.getPlayerStats(gameId, player.uniqueId)
        val currentMoney = playerStats?.cash ?: 0

        // Get all available towers
        val allTowers = TowerRegistry.getAllTowers().values.toList()
        val startIndex = currentPage * towersPerPage
        val endIndex = minOf(startIndex + towersPerPage, allTowers.size)

        // Display towers (slots 0-27, first 4 rows minus last 2 slots)
        var slotIndex = 0
        for (i in startIndex until endIndex) {
            // Skip bottom row control slots
            if (slotIndex >= 28) break

            val tower = allTowers[i]
            val cost = (tower.damage * 10 + tower.range * 20).toInt() // Calculate cost based on stats
            val canAfford = currentMoney >= cost

            val lore = mutableListOf<String>()
            lore.add("§7${tower.displayName}")
            lore.addAll(tower.description.map { "§7$it" })
            lore.add("")
            lore.add("§7Damage: §c${tower.damage}")
            lore.add("§7Range: §a${tower.range}")
            lore.add("§7Attack Speed: §b${tower.attackSpeed}")
            lore.add("")
            lore.add("§7Cost: §e${cost}$")
            lore.add("")
            if (canAfford) {
                lore.add("§aClick to purchase!")
            } else {
                lore.add("§cYou cannot afford this!")
                lore.add("§cNeed §e${cost - currentMoney}$ §cmore")
            }

            val item = createMenuItem(
                tower.icon,
                (if (canAfford) "§a" else "§c") + tower.displayName,
                lore
            )

            // Store tower ID in the item for purchase handling
            val meta = item.itemMeta
            meta.persistentDataContainer.set(
                TowerDefMC.createKey("shop_tower_id"),
                PersistentDataType.STRING,
                tower.id
            )
            meta.persistentDataContainer.set(
                TowerDefMC.createKey("shop_tower_cost"),
                PersistentDataType.INTEGER,
                cost
            )
            item.itemMeta = meta

            inventory.setItem(slotIndex, item)
            slotIndex++
        }

        // Bottom row separator
        for (i in 45..53) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        // Exit button (slot 45)
        inventory.setItem(45, createMenuItem(
            Material.BARRIER,
            "§c§lExit Shop",
            listOf("§7Close the shop")
        ))

        // Current money display (slot 48-49)
        inventory.setItem(49, createMenuItem(
            Material.GOLD_INGOT,
            "§e§lCurrent Money: §6${currentMoney}$",
            listOf(
                "§7Your available cash",
                "§7Earn more by defeating enemies"
            )
        ))

        // Game stats (slot 51)
        val game = GameRegistry.allGames[gameId]
        if (game != null) {
            val waveInfo = game.config.waves.getOrNull(game.currentWave)
            inventory.setItem(51, createMenuItem(
                Material.BOOK,
                "§b§lGame Stats",
                listOf(
                    "§7Game ID: §e${gameId}",
                    "§7Current Wave: §e${game.currentWave + 1}",
                    "§7Wave Name: §e${waveInfo?.name ?: "N/A"}",
                    "§7Lives: §c${playerStats?.kills ?: 0}",
                    "§7Towers Placed: §a${playerStats?.towersPlaced ?: 0}"
                )
            ))
        }

        // Pagination
        val totalPages = (allTowers.size + towersPerPage - 1) / towersPerPage
        if (currentPage > 0) {
            inventory.setItem(46, createMenuItem(
                Material.ARROW,
                "§e§lPrevious Page",
                listOf("§7Page ${currentPage} of ${totalPages}")
            ))
        }
        if (currentPage < totalPages - 1) {
            inventory.setItem(52, createMenuItem(
                Material.ARROW,
                "§e§lNext Page",
                listOf("§7Page ${currentPage + 2} of ${totalPages}")
            ))
        }
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        val clickedItem = event.currentItem ?: return

        when (event.slot) {
            45 -> { // Exit
                player.closeInventory()
            }
            46 -> { // Previous page
                if (currentPage > 0) {
                    currentPage--
                    setMenuItems()
                }
            }
            52 -> { // Next page
                val allTowers = TowerRegistry.getAllTowers().values.toList()
                val totalPages = (allTowers.size + towersPerPage - 1) / towersPerPage
                if (currentPage < totalPages - 1) {
                    currentPage++
                    setMenuItems()
                }
            }
            else -> {
                // Check if it's a tower purchase
                val meta = clickedItem.itemMeta ?: return
                val towerId = meta.persistentDataContainer.get(
                    TowerDefMC.createKey("shop_tower_id"),
                    PersistentDataType.STRING
                ) ?: return
                val cost = meta.persistentDataContainer.get(
                    TowerDefMC.createKey("shop_tower_cost"),
                    PersistentDataType.INTEGER
                ) ?: return

                purchaseTower(player, towerId, cost)
            }
        }
    }

    private fun purchaseTower(player: Player, towerId: String, cost: Int) {
        val playerStats = PlayerStatsManager.getPlayerStats(gameId, player.uniqueId)
        if (playerStats == null) {
            player.sendMessage("§cError: Could not find your stats!")
            return
        }

        if (playerStats.cash < cost) {
            player.sendMessage("§cYou cannot afford this tower!")
            player.sendMessage("§cYou need §e${cost - playerStats.cash}$ §cmore.")
            return
        }

        // Deduct money
        if (!PlayerStatsManager.spendCash(gameId, player.uniqueId, cost)) {
            player.sendMessage("§cFailed to purchase tower!")
            return
        }

        // Get tower info
        val tower = TowerRegistry.getTower(towerId)
        if (tower == null) {
            player.sendMessage("§cTower not found!")
            PlayerStatsManager.awardCash(gameId, player.uniqueId, cost) // Refund
            return
        }

        // Create tower item
        val towerItem = ItemStack(Material.END_ROD, 1)
        val meta = towerItem.itemMeta

        meta.displayName(Component.text("§6${tower.displayName}"))
        meta.lore(listOf(
            Component.text("§7Damage: §c${tower.damage}"),
            Component.text("§7Range: §a${tower.range}"),
            Component.text("§7Attack Speed: §b${tower.attackSpeed}")
        ))

        // Set tower properties in PDC
        meta.persistentDataContainer.set(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Tower 1")
        meta.persistentDataContainer.set(TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, tower.range)
        meta.persistentDataContainer.set(TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, tower.damage)
        meta.persistentDataContainer.set(TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE, 1.0 / tower.attackSpeed)
        meta.persistentDataContainer.set(TowerDefMC.TOWER_TYPES, PersistentDataType.STRING, towerId)

        towerItem.itemMeta = meta

        // Give to player
        val leftover = player.inventory.addItem(towerItem)
        if (leftover.isNotEmpty()) {
            player.world.dropItemNaturally(player.location, towerItem)
        }

        player.sendMessage("§a§lPurchased! §7${tower.displayName} §afor §e${cost}$")
        player.sendMessage("§7Remaining: §e${playerStats.cash}$")

        // Refresh menu
        setMenuItems()
    }
}
