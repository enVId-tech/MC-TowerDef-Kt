package dev.etran.towerDefMc.menus.waves

import dev.etran.towerDefMc.data.EnemySpawnCommand
import dev.etran.towerDefMc.data.WaitCommand
import dev.etran.towerDefMc.data.WaveCommand
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ModifyWave(
    player: Player,
    val waveNum: Int
) : CustomMenu(player, 54, "Tower Defense - Wave $waveNum") {

    private var waveSequence: MutableList<WaveCommand> = mutableListOf()
    private var minTime: Double = 0.0
    private var maxTime: Double = 0.0
    private var waveHealth: Double? = null
    private var cashGiven: Int = 0

    override fun setMenuItems() {
        displayWaveSequence()

        // Middle row separator
        for (i in 27..35) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ", listOf()))
        }

        // Bottom 2 rows - action items
        inventory.setItem(36, createMenuItem(Material.CLOCK, "Add Wait Time",
            listOf("Add a wait command to the wave", "Enemies spawn after the wait")))
        inventory.setItem(37, createMenuItem(Material.BARRIER, "Remove Wait Time",
            listOf("Remove the last wait command", "from the wave sequence")))
        inventory.setItem(39, createMenuItem(Material.ZOMBIE_HEAD, "Add Enemies",
            listOf("Add enemy spawn commands", "to the wave sequence")))
        inventory.setItem(40, createMenuItem(Material.BONE, "Remove Enemies",
            listOf("Remove the last enemy spawn", "command from the wave")))
        inventory.setItem(45, createRenamableItem(Material.GREEN_STAINED_GLASS, "Min Time: {VALUE}s",
            listOf("Minimum time for the wave", "Current: {VALUE} seconds"), minTime.toString()))
        inventory.setItem(46, createRenamableItem(Material.RED_STAINED_GLASS, "Max Time: {VALUE}s",
            listOf("Maximum time for the wave", "Cannot be less than total wait time", "Current: {VALUE} seconds"), maxTime.toString()))
        inventory.setItem(48, createRenamableItem(Material.REDSTONE_BLOCK, "Wave Health: {VALUE}",
            listOf("Custom health for this wave", "Leave as 'default' for normal health", "Current: {VALUE}"), waveHealth?.toString() ?: "default"))
        inventory.setItem(49, createRenamableItem(Material.GOLD_INGOT, "Cash Given: {VALUE}",
            listOf("Cash awarded on wave completion", "Current: {VALUE}"), cashGiven.toString()))
        inventory.setItem(52, createMenuItem(Material.BARRIER, "Cancel",
            listOf("Return without saving", "WARNING: Changes will be lost!")))
        inventory.setItem(53, createMenuItem(Material.EMERALD_BLOCK, "Save Wave",
            listOf("Save the wave configuration", "and return to waves menu")))
    }

    private fun displayWaveSequence() {
        for (i in 0..26) {
            inventory.setItem(i, null)
        }

        for ((index, command) in waveSequence.withIndex()) {
            if (index >= 27) break

            val item = when (command) {
                is WaitCommand -> createMenuItem(Material.CLOCK, "Wait: ${command.waitSeconds}s",
                    listOf("Wait for ${command.waitSeconds} seconds"))
                is EnemySpawnCommand -> {
                    val enemyList = command.enemies.entries.joinToString(", ") { "${it.key}: ${it.value}x" }
                    createMenuItem(Material.ZOMBIE_SPAWN_EGG, "Spawn Enemies",
                        listOf("Interval: ${command.intervalSeconds}s", "Enemies: $enemyList"))
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
            52 -> handleCancel()
            53 -> handleSave()
        }
    }

    private fun handleAddWaitTime() {
        player.closeInventory()
        player.sendMessage("§aEnter wait time in seconds in chat, or type 'cancel'")
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
        val enemiesMenu = EnemiesSelection(player)
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

    private fun handleCancel() {
        player.closeInventory()
        player.sendMessage("§cWave modification cancelled")
    }

    private fun handleSave() {
        val totalWaitTime = waveSequence.filterIsInstance<WaitCommand>().sumOf { it.waitSeconds }

        if (maxTime < totalWaitTime) {
            player.sendMessage("§cMax time cannot be less than total wait time (${totalWaitTime}s)")
            return
        }

        player.closeInventory()
        player.sendMessage("§aWave $waveNum saved successfully!")
    }
}