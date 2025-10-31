package dev.etran.towerDefMc.menus.waves

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class EnemiesSelection(
    player: Player
) : CustomMenu(player, 54, "Tower Defense - Add Enemies") {

    private var currentPage: Int = 0
    private val selectedEnemies: MutableMap<String, Int> = mutableMapOf()
    private var selectedEnemyForCount: String? = null

    // TODO: This should be loaded from a registry/config in the future
    private val availableEnemies: List<EnemyType> = listOf(
        EnemyType("Basic_Enemy_1", Material.ZOMBIE_HEAD, "Basic Zombie", listOf("Standard enemy", "Medium speed", "Low health")),
        EnemyType("Fast_Enemy", Material.LEATHER_BOOTS, "Fast Runner", listOf("Fast enemy", "High speed", "Low health")),
        EnemyType("Tank_Enemy", Material.IRON_CHESTPLATE, "Tank", listOf("Slow enemy", "Low speed", "High health")),
        EnemyType("Boss_Enemy", Material.WITHER_SKELETON_SKULL, "Boss", listOf("Boss enemy", "Medium speed", "Very high health"))
    )

    private val enemiesPerPage = 36

    override fun setMenuItems() {
        // Clear inventory
        inventory.clear()

        // Display enemies for current page (slots 0-35)
        val startIndex = currentPage * enemiesPerPage
        val endIndex = minOf(startIndex + enemiesPerPage, availableEnemies.size)

        for (i in startIndex until endIndex) {
            val slotIndex = i - startIndex
            val enemy = availableEnemies[i]
            val count = selectedEnemies[enemy.id] ?: 0

            val lore = mutableListOf<String>()
            lore.addAll(enemy.description)
            lore.add("")
            lore.add("§7Selected: §e${count}x")
            lore.add("§7Click to select amount")

            inventory.setItem(
                slotIndex,
                createMenuItem(enemy.icon, "§f${enemy.displayName}", lore)
            )
        }

        // Separator row (slots 36-44)
        for (i in 36..44) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ", listOf()))
        }

        // Bottom row controls (slots 45-53)
        // Left side: Configuration
        inventory.setItem(
            45,
            createRenamableItem(
                Material.CLOCK,
                "Spawn Interval: {VALUE}s",
                listOf(
                    "Time between each enemy spawn",
                    "Current: {VALUE} seconds"
                ),
                "1.0"
            )
        )

        inventory.setItem(
            46,
            createMenuItem(
                Material.BARRIER,
                "§cCancel",
                listOf("Return without adding enemies")
            )
        )

        inventory.setItem(
            47,
            createMenuItem(
                Material.EMERALD_BLOCK,
                "§aConfirm",
                listOf(
                    "Add selected enemies to wave",
                    "Total enemies: ${selectedEnemies.values.sum()}"
                )
            )
        )

        // Right side: Pagination
        val totalPages = (availableEnemies.size + enemiesPerPage - 1) / enemiesPerPage

        if (currentPage > 0) {
            inventory.setItem(
                51,
                createMenuItem(
                    Material.RED_CONCRETE,
                    "§cBack Page",
                    listOf("Page ${currentPage} of $totalPages")
                )
            )
        }

        if (currentPage < totalPages - 1) {
            inventory.setItem(
                53,
                createMenuItem(
                    Material.GREEN_CONCRETE,
                    "§aNext Page",
                    listOf("Page ${currentPage + 2} of $totalPages")
                )
            )
        }
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val slot = event.slot

        when (slot) {
            in 0..35 -> handleEnemyClick(slot)
            46 -> handleCancel()
            47 -> handleConfirm()
            51 -> handleBackPage()
            53 -> handleNextPage()
            // Slot 45 is a renamable item for spawn interval, handled by CustomMenu
        }
    }

    private fun handleEnemyClick(slot: Int) {
        val enemyIndex = currentPage * enemiesPerPage + slot
        if (enemyIndex >= availableEnemies.size) return

        val enemy = availableEnemies[enemyIndex]
        selectedEnemyForCount = enemy.id

        // Open number selector menu
        player.closeInventory()
        val numberSelector = NumberSelector(player, enemy.displayName) { amount ->
            if (amount > 0) {
                selectedEnemies[enemy.id] = amount
            } else {
                selectedEnemies.remove(enemy.id)
            }
            // Reopen this menu
            this.open()
        }
        numberSelector.open()
    }

    private fun handleCancel() {
        player.closeInventory()
        player.sendMessage("§cEnemy selection cancelled")
        // TODO: Return to ModifyWave menu
    }

    private fun handleConfirm() {
        if (selectedEnemies.isEmpty()) {
            player.sendMessage("§cYou must select at least one enemy!")
            return
        }

        // TODO: Get spawn interval from renamable item
        // TODO: Create EnemySpawnCommand and pass back to ModifyWave

        player.closeInventory()
        player.sendMessage("§aAdded ${selectedEnemies.values.sum()} enemies to wave!")
        // TODO: Return to ModifyWave menu with selected enemies
    }

    private fun handleBackPage() {
        if (currentPage > 0) {
            currentPage--
            setMenuItems()
        }
    }

    private fun handleNextPage() {
        val totalPages = (availableEnemies.size + enemiesPerPage - 1) / enemiesPerPage
        if (currentPage < totalPages - 1) {
            currentPage++
            setMenuItems()
        }
    }

    // Helper data class for enemy types
    private data class EnemyType(
        val id: String,
        val icon: Material,
        val displayName: String,
        val description: List<String>
    )

    // Simple number selector menu
    private class NumberSelector(
        player: Player,
        private val itemName: String,
        private val onSelect: (Int) -> Unit
    ) : CustomMenu(player, 27, "Select Amount - $itemName") {

        override fun setMenuItems() {
            val amounts = listOf(1, 5, 10, 25, 50, 100)

            for ((index, amount) in amounts.withIndex()) {
                val slot = 10 + index
                if (slot < 17) {
                    inventory.setItem(
                        slot,
                        createMenuItem(
                            Material.SLIME_BALL,
                            "§a$amount",
                            listOf("§7Select $amount enemies")
                        )
                    )
                }
            }

            inventory.setItem(
                18,
                createRenamableItem(
                    Material.PAPER,
                    "Custom: {VALUE}",
                    listOf(
                        "Enter a custom amount",
                        "Current: {VALUE}"
                    ),
                    "1"
                )
            )

            inventory.setItem(
                22,
                createMenuItem(
                    Material.BARRIER,
                    "§cRemove/Cancel",
                    listOf("Set amount to 0 or cancel")
                )
            )
        }

        override fun handleClick(event: InventoryClickEvent) {
            event.isCancelled = true

            when (event.slot) {
                10 -> { onSelect(1); player.closeInventory() }
                11 -> { onSelect(5); player.closeInventory() }
                12 -> { onSelect(10); player.closeInventory() }
                13 -> { onSelect(25); player.closeInventory() }
                14 -> { onSelect(50); player.closeInventory() }
                15 -> { onSelect(100); player.closeInventory() }
                18 -> {
                    // Custom amount from renamable item
                    val item = event.currentItem
                    val meta = item?.itemMeta
                    if (meta != null) {
                        val pdc = meta.persistentDataContainer
                        val customValue = pdc.get(TowerDefMC.TITLE_KEY, org.bukkit.persistence.PersistentDataType.STRING)
                        val amount = customValue?.toIntOrNull() ?: 1
                        onSelect(amount)
                        player.closeInventory()
                    }
                }
                22 -> { onSelect(0); player.closeInventory() }
            }
        }
    }
}