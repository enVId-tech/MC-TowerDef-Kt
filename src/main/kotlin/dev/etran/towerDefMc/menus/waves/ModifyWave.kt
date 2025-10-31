package dev.etran.towerDefMc.menus.waves

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.EnemySpawnCommand
import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaitCommand
import dev.etran.towerDefMc.data.WaveCommand
import dev.etran.towerDefMc.data.WaveData
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

class ModifyWave(
    player: Player,
    val waveNum: Int,
    private val gameConfig: GameSaveConfig,
    private val isNewWave: Boolean = false,
    private val gameId: Int
) : CustomMenu(player, 54, "Tower Defense - Wave $waveNum") {

    private var waveSequence: MutableList<WaveCommand> = mutableListOf()
    private var waveName: String = "Wave $waveNum"
    private var minTime: Double = 0.0
    private var maxTime: Double = 60.0
    private var waveHealth: Double? = null
    private var cashGiven: Int = 0

    init {
        // Load existing wave data if not a new wave
        if (!isNewWave && waveNum - 1 < gameConfig.waves.size) {
            val existingWave = gameConfig.waves[waveNum - 1]
            waveName = existingWave.name
            waveSequence.addAll(existingWave.sequence)
            // Load other wave properties
            minTime = existingWave.minTime
            maxTime = existingWave.maxTime
            waveHealth = existingWave.waveHealth
            cashGiven = existingWave.cashGiven
        }
    }

    override fun setMenuItems() {
        displayWaveSequence()

        // Middle row separator
        for (i in 27..35) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ", listOf()))
        }

        // Bottom 2 rows - action items
        inventory.setItem(
            36, createMenuItem(
                Material.CLOCK,
                "Add Wait Time",
                listOf("Add a wait command to the wave", "Enemies spawn after the wait")
            )
        )
        inventory.setItem(
            37, createMenuItem(
                Material.BARRIER, "Remove Wait Time", listOf("Remove the last wait command", "from the wave sequence")
            )
        )
        inventory.setItem(
            39, createMenuItem(
                Material.ZOMBIE_HEAD, "Add Enemies", listOf("Add enemy spawn commands", "to the wave sequence")
            )
        )
        inventory.setItem(
            40, createMenuItem(
                Material.BONE, "Remove Enemies", listOf("Remove the last enemy spawn", "command from the wave")
            )
        )

        inventory.setItem(
            43, createRenamableItem(
                Material.NAME_TAG, "Wave Name: {VALUE}", listOf("The name of this wave", "Current: {VALUE}"), waveName
            )
        )

        inventory.setItem(
            45, createRenamableItem(
                Material.GREEN_STAINED_GLASS,
                "Min Time: {VALUE}s",
                listOf("Minimum time for the wave", "Current: {VALUE} seconds"),
                minTime.toString()
            )
        )
        inventory.setItem(
            46, createRenamableItem(
                Material.RED_STAINED_GLASS,
                "Max Time: {VALUE}s",
                listOf("Maximum time for the wave", "Cannot be less than total wait time", "Current: {VALUE} seconds"),
                maxTime.toString()
            )
        )
        inventory.setItem(
            48, createRenamableItem(
                Material.REDSTONE_BLOCK,
                "Wave Health: {VALUE}",
                listOf("Custom health for this wave", "Leave as 'default' for normal health", "Current: {VALUE}"),
                waveHealth?.toString() ?: "default"
            )
        )
        inventory.setItem(
            49, createRenamableItem(
                Material.GOLD_INGOT,
                "Cash Given: {VALUE}",
                listOf("Cash awarded on wave completion", "Current: {VALUE}"),
                cashGiven.toString()
            )
        )

        if (!isNewWave) {
            inventory.setItem(
                51, createMenuItem(
                    Material.TNT,
                    "§cDelete Wave",
                    listOf("§4WARNING: This will delete the wave!", "This cannot be undone")
                )
            )
        }

        inventory.setItem(
            52, createMenuItem(
                Material.BARRIER, "Cancel", listOf("Return without saving", "WARNING: Changes will be lost!")
            )
        )
        inventory.setItem(
            53, createMenuItem(
                Material.EMERALD_BLOCK, "Save Wave", listOf("Save the wave configuration", "and return to waves menu")
            )
        )
    }

    private fun displayWaveSequence() {
        for (i in 0..26) {
            inventory.setItem(i, null)
        }

        for ((index, command) in waveSequence.withIndex()) {
            if (index >= 27) break

            val item = when (command) {
                is WaitCommand -> createMenuItem(
                    Material.CLOCK, "Wait: ${command.waitSeconds}s", listOf("Wait for ${command.waitSeconds} seconds")
                )

                is EnemySpawnCommand -> {
                    val enemyList = command.enemies.entries.joinToString(", ") { "${it.key}: ${it.value}x" }
                    createMenuItem(
                        Material.ZOMBIE_SPAWN_EGG,
                        "Spawn Enemies",
                        listOf("Interval: ${command.intervalSeconds}s", "Enemies: $enemyList")
                    )
                }

                else -> createMenuItem(Material.PAPER, "Unknown Command", listOf())
            }
            inventory.setItem(index, item)
        }
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        when (event.slot) {
            36 -> handleAddWaitTime()
            37 -> handleRemoveWaitTime()
            39 -> handleAddEnemies()
            40 -> handleRemoveEnemies()
            43 -> handleRenameWave(event)
            51 -> handleDeleteWave()
            52 -> handleCancel()
            53 -> handleSave(event)
        }
    }

    private fun handleAddWaitTime() {
        player.closeInventory()
        player.sendMessage("§aEnter wait time in seconds in chat, or type 'cancel'")
        // For now, add a default wait command
        waveSequence.add(WaitCommand(5.0))
        displayWaveSequence()
        open()
    }

    private fun handleRemoveWaitTime() {
        val lastWaitIndex = waveSequence.indexOfLast { it is WaitCommand }
        if (lastWaitIndex >= 0) {
            waveSequence.removeAt(lastWaitIndex)
            displayWaveSequence()
            player.sendMessage("§aRemoved last wait command")
        } else {
            player.sendMessage("§cNo wait commands to remove")
        }
    }

    private fun handleAddEnemies() {
        player.closeInventory()
        val enemiesMenu = EnemiesSelection(
            player,
            { enemies, interval ->
                // Callback from EnemiesSelection
                waveSequence.add(EnemySpawnCommand(enemies, interval))
                player.sendMessage("§aAdded enemy spawn command with ${enemies.values.sum()} enemies")
                this.open()
            },
            waveNum,
            gameId,
            gameConfig
        )
        enemiesMenu.open()
    }


    private fun handleRemoveEnemies() {
        val lastEnemyIndex = waveSequence.indexOfLast { it is EnemySpawnCommand }
        if (lastEnemyIndex >= 0) {
            waveSequence.removeAt(lastEnemyIndex)
            displayWaveSequence()
            player.sendMessage("§aRemoved last enemy spawn command")
        } else {
            player.sendMessage("§cNo enemy spawn commands to remove")
        }
    }

    private fun handleRenameWave(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        val newName = pdc.get(
            TowerDefMC.TITLE_KEY, PersistentDataType.STRING
        ) ?: waveName

        waveName = newName
    }

    private fun handleDeleteWave() {
        if (isNewWave) return

        // Remove wave from config
        val mutableWaves = gameConfig.waves.toMutableList()
        if (waveNum - 1 < mutableWaves.size) {
            mutableWaves.removeAt(waveNum - 1)

            // Update the game config
            val updatedConfig = gameConfig.copy(waves = mutableWaves)

            // Save updated config to registry
            GameRegistry.allGames.entries.find {
                it.value.config == gameConfig
            }?.let { entry ->
                GameRegistry.saveGameConfig(entry.key, updatedConfig)
            }

            player.closeInventory()
            player.sendMessage("§aWave $waveNum deleted!")

            // Return to Waves menu
            Waves(player, updatedConfig, gameId).open()
        }
    }

    private fun handleCancel() {
        player.closeInventory()
        player.sendMessage("§cWave modification cancelled")
        // Return to Waves menu
        Waves(player, gameConfig, gameId).open()
    }

    private fun handleSave(@Suppress("UNUSED_PARAMETER") event: InventoryClickEvent) {
        // Get updated values from renamable items
        updateValuesFromInventory()

        val totalWaitTime = waveSequence.filterIsInstance<WaitCommand>().sumOf { it.waitSeconds }

        if (maxTime < totalWaitTime) {
            player.sendMessage("§cMax time cannot be less than total wait time (${totalWaitTime}s)")
            return
        }

        if (waveSequence.isEmpty()) {
            player.sendMessage("§cWave must have at least one command!")
            return
        }

        // Create the wave data
        val waveData = WaveData(
            name = waveName,
            sequence = waveSequence.toList(),
            minTime = minTime,
            maxTime = maxTime,
            waveHealth = waveHealth,
            cashGiven = cashGiven
        )

        // Update the game config
        val mutableWaves = gameConfig.waves.toMutableList()

        if (isNewWave) {
            // Insert at position
            if (waveNum - 1 <= mutableWaves.size) {
                mutableWaves.add(waveNum - 1, waveData)
            } else {
                mutableWaves.add(waveData)
            }
        } else {
            // Update existing wave
            if (waveNum - 1 < mutableWaves.size) {
                mutableWaves[waveNum - 1] = waveData
            }
        }

        // Update the gameConfig's waves directly (since it's a var property)
        gameConfig.waves = mutableWaves

        // Find the game manager and update its config, then save
        val gameManager = GameRegistry.allGames[gameId]
        if (gameManager != null) {
            gameManager.updateWaves(mutableWaves)
            player.sendMessage("§aWave $waveNum saved successfully!")
        } else {
            player.sendMessage("§cError: Could not find game to save!")
        }

        player.closeInventory()

        // Return to Waves menu with the updated config
        Waves(player, gameConfig, gameId).open()
    }

    private fun updateValuesFromInventory() {
        // Update wave name
        inventory.getItem(43)?.let { item ->
            val meta = item.itemMeta
            val pdc = meta.persistentDataContainer
            pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)?.let {
                waveName = it
            }
        }

        // Update min time
        inventory.getItem(45)?.let { item ->
            val meta = item.itemMeta
            val pdc = meta.persistentDataContainer
            pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)?.let {
                minTime = it.toDoubleOrNull() ?: minTime
            }
        }

        // Update max time
        inventory.getItem(46)?.let { item ->
            val meta = item.itemMeta
            val pdc = meta.persistentDataContainer
            pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)?.let {
                maxTime = it.toDoubleOrNull() ?: maxTime
            }
        }

        // Update wave health
        inventory.getItem(48)?.let { item ->
            val meta = item.itemMeta
            val pdc = meta.persistentDataContainer
            pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)?.let {
                waveHealth = if (it == "default") null else it.toDoubleOrNull()
            }
        }

        // Update cash given
        inventory.getItem(49)?.let { item ->
            val meta = item.itemMeta
            val pdc = meta.persistentDataContainer
            pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)?.let {
                cashGiven = it.toIntOrNull() ?: cashGiven
            }
        }
    }
}