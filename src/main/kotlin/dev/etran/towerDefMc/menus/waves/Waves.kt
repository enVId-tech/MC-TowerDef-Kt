package dev.etran.towerDefMc.menus.waves

import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class Waves(
    player: Player,
    private val gameConfig: GameSaveConfig
) : CustomMenu(player, 54, "Tower Defense - Waves") {

    private var currentPage: Int = 0
    private val wavesPerPage = 36 // 4 rows of 9

    override fun setMenuItems() {
        inventory.clear()

        // Display waves for current page (slots 0-35, first 4 rows)
        val startIndex = currentPage * wavesPerPage
        val endIndex = minOf(startIndex + wavesPerPage, gameConfig.waves.size)

        for (i in startIndex until endIndex) {
            val slotIndex = i - startIndex
            val wave = gameConfig.waves[i]
            val waveNumber = i + 1

            val commandCount = wave.sequence.size
            val lore = mutableListOf<String>()
            lore.add("§7Wave Name: §f${wave.name}")
            lore.add("§7Commands: §e$commandCount")
            lore.add("")
            lore.add("§eClick to modify this wave")

            inventory.setItem(
                slotIndex,
                createMenuItem(
                    Material.PAPER,
                    "§aWave $waveNumber",
                    lore
                )
            )
        }

        // Separator row (slots 36-44, 2nd to last line)
        for (i in 36..44) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ", listOf()))
        }

        // Last line controls (slots 45-53)
        val totalPages = (gameConfig.waves.size + wavesPerPage - 1) / wavesPerPage

        // Left side - Back page button
        if (currentPage > 0) {
            inventory.setItem(
                45,
                createMenuItem(
                    Material.RED_CONCRETE,
                    "§cBack Page",
                    listOf("Go to page $currentPage")
                )
            )
        }

        // Center - Insert new wave button
        inventory.setItem(
            49,
            createRenamableItem(
                Material.EMERALD,
                "Insert Wave at Position: {VALUE}",
                listOf(
                    "Create a new wave at the specified position",
                    "Current position: {VALUE}",
                    "§7Max position: ${gameConfig.waves.size + 1}"
                ),
                "${gameConfig.waves.size + 1}"
            )
        )

        // Right side - Next page button
        if (currentPage < totalPages - 1) {
            inventory.setItem(
                53,
                createMenuItem(
                    Material.GREEN_CONCRETE,
                    "§aNext Page",
                    listOf("Go to page ${currentPage + 2}")
                )
            )
        }

        // Close/Done button
        inventory.setItem(
            47,
            createMenuItem(
                Material.BARRIER,
                "§cDone",
                listOf("Close waves menu")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val slot = event.slot

        when {
            slot in 0..35 -> handleWaveClick(slot)
            slot == 45 -> handleBackPage()
            slot == 47 -> handleDone()
            slot == 49 -> handleInsertWave(event)
            slot == 53 -> handleNextPage()
        }
    }

    private fun handleWaveClick(slot: Int) {
        val waveIndex = currentPage * wavesPerPage + slot
        if (waveIndex >= gameConfig.waves.size) return

        val waveNumber = waveIndex + 1
        player.closeInventory()

        // Open ModifyWave menu for this wave
        val modifyWaveMenu = ModifyWave(player, waveNumber, gameConfig)
        modifyWaveMenu.open()
    }

    private fun handleInsertWave(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        val positionStr = pdc.get(
            dev.etran.towerDefMc.TowerDefMC.TITLE_KEY,
            org.bukkit.persistence.PersistentDataType.STRING
        )
        val position = positionStr?.toIntOrNull() ?: (gameConfig.waves.size + 1)

        if (position < 1 || position > gameConfig.waves.size + 1) {
            player.sendMessage("§cInvalid position! Must be between 1 and ${gameConfig.waves.size + 1}")
            return
        }

        player.closeInventory()

        // Open ModifyWave for new wave at this position
        val modifyWaveMenu = ModifyWave(player, position, gameConfig, isNewWave = true)
        modifyWaveMenu.open()
    }

    private fun handleBackPage() {
        if (currentPage > 0) {
            currentPage--
            setMenuItems()
        }
    }

    private fun handleNextPage() {
        val totalPages = (gameConfig.waves.size + wavesPerPage - 1) / wavesPerPage
        if (currentPage < totalPages - 1) {
            currentPage++
            setMenuItems()
        }
    }

    private fun handleDone() {
        player.closeInventory()
        player.sendMessage("§aWaves menu closed")
        // TODO: Auto-save should happen here
    }
}