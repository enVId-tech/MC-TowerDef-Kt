package dev.etran.towerDefMc.menus.generators

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.TowerGeneratorData
import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class TowerGeneratorMenu(player: Player, private val spawnEggType: EntityType) : CustomMenu(player, 54, "Tower Generator") {

    private var displayName: String = "Custom Tower"
    private var cost: Int = 100
    private var damage: Double = 5.0
    private var damageInterval: Double = 1.0
    private var range: Double = 5.0
    private var upgradePath: String = "none"
    private var isBaby: Boolean = false
    private var entitySize: Double = 1.0

    override fun setMenuItems() {
        // Display the spawn egg being configured (slot 4)
        val spawnEggMaterial = Material.getMaterial("${spawnEggType.name}_SPAWN_EGG") ?: Material.EGG
        inventory.setItem(
            4, createMenuItem(
                spawnEggMaterial, "§6§lTower Spawn Egg", listOf("§7Entity Type: §e${spawnEggType.name}")
            )
        )

        // Tower Properties
        inventory.setItem(
            10, createRenamableItem(
                Material.GOLD_INGOT, "§e§lCost: {VALUE}", listOf("§7Click to change cost"), cost.toString()
            )
        )

        inventory.setItem(
            11, createRenamableItem(
                Material.IRON_SWORD, "§c§lDamage: {VALUE}", listOf("§7Click to change damage"), damage.toString()
            )
        )

        inventory.setItem(
            12, createRenamableItem(
                Material.CLOCK,
                "§b§lAttack Interval: {VALUE}s",
                listOf("§7Time between attacks", "§7Click to change"),
                damageInterval.toString()
            )
        )

        inventory.setItem(
            13, createRenamableItem(
                Material.BOW, "§a§lRange: {VALUE}", listOf("§7Click to change range"), range.toString()
            )
        )

        inventory.setItem(
            14, createRenamableItem(
                Material.ENCHANTED_BOOK,
                "§d§lUpgrade Path: {VALUE}",
                listOf("§7Tower upgrade path ID", "§7Click to change"),
                upgradePath
            )
        )

        inventory.setItem(
            15, createRenamableItem(
                Material.NAME_TAG, "§f§lDisplay Name: {VALUE}", listOf("§7Click to change name"), displayName
            )
        )

        // Mob Properties
        inventory.setItem(
            28, createMenuItem(
                if (isBaby) Material.LIME_DYE else Material.GRAY_DYE,
                "§e§lBaby Mode: " + if (isBaby) "§aEnabled" else "§cDisabled",
                listOf("§7Click to toggle")
            )
        )

        inventory.setItem(
            29, createRenamableItem(
                Material.SLIME_BALL,
                "§e§lSize: {VALUE}",
                listOf("§7Entity scale multiplier", "§7Click to change"),
                entitySize.toString()
            )
        )

        // Separator
        for (i in 36..44) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        // Action buttons
        inventory.setItem(
            45, createMenuItem(
                Material.BARRIER, "§c§lCancel", listOf("§7Close without saving")
            )
        )

        inventory.setItem(
            49, createMenuItem(
                Material.LIME_CONCRETE, "§a§lGenerate Tower Item", listOf(
                    "§7Creates a tower spawn item", "§7with the configured properties", "", "§eClick to generate!"
                )
            )
        )

        inventory.setItem(
            53, createMenuItem(
                Material.BOOK, "§e§lHelp", listOf(
                    "§7Run /tdgenerator tower <type>",
                    "§7to configure tower properties",
                    "§7Example: /tdgenerator tower IRON_GOLEM"
                )
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return

        when (event.slot) {
            10 -> { // Cost
                player.closeInventory()
                player.sendMessage("§eEnter the tower cost (or 'cancel'):")
                awaitNumberInput(player, "cost") { value ->
                    cost = value.toInt()
                    setMenuItems()
                    open()
                }
            }

            11 -> { // Damage
                player.closeInventory()
                player.sendMessage("§eEnter the tower damage (or 'cancel'):")
                awaitNumberInput(player, "damage") { value ->
                    damage = value.toDouble()
                    setMenuItems()
                    open()
                }
            }

            12 -> { // Attack Interval
                player.closeInventory()
                player.sendMessage("§eEnter the attack interval in seconds (or 'cancel'):")
                awaitNumberInput(player, "interval") { value ->
                    damageInterval = value.toDouble()
                    setMenuItems()
                    open()
                }
            }

            13 -> { // Range
                player.closeInventory()
                player.sendMessage("§eEnter the tower range (or 'cancel'):")
                awaitNumberInput(player, "range") { value ->
                    range = value.toDouble()
                    setMenuItems()
                    open()
                }
            }

            14 -> { // Upgrade Path
                player.closeInventory()
                player.sendMessage("§eEnter the upgrade path ID (or 'cancel'):")
                awaitStringInput(player, "upgradePath") { value ->
                    upgradePath = value
                    setMenuItems()
                    open()
                }
            }

            15 -> { // Display Name
                player.closeInventory()
                player.sendMessage("§eEnter the display name (or 'cancel'):")
                awaitStringInput(player, "displayName") { value ->
                    displayName = value
                    setMenuItems()
                    open()
                }
            }

            28 -> { // Baby toggle
                isBaby = !isBaby
                setMenuItems()
            }

            29 -> { // Size
                player.closeInventory()
                player.sendMessage("§eEnter the entity size multiplier (or 'cancel'):")
                awaitNumberInput(player, "size") { value ->
                    entitySize = value.toDouble()
                    setMenuItems()
                    open()
                }
            }

            45 -> { // Cancel
                player.closeInventory()
                player.sendMessage("§cTower generation cancelled.")
            }

            49 -> { // Generate
                generateTower(player)
            }
        }
    }

    private fun generateTower(player: Player) {
        val data = TowerGeneratorData(
            spawnEggType = spawnEggType,
            displayName = displayName,
            cost = cost,
            damage = damage,
            damageInterval = damageInterval,
            range = range,
            upgradePath = upgradePath,
            isBaby = isBaby,
            size = entitySize
        )

        // Create the tower item
        val spawnEggMaterial = Material.getMaterial("${spawnEggType.name}_SPAWN_EGG") ?: Material.ZOMBIE_SPAWN_EGG
        val towerItem = ItemStack(spawnEggMaterial, 1)
        val meta = towerItem.itemMeta

        meta.displayName(Component.text("§6$displayName"))
        meta.lore(
            listOf(
                Component.text("§7Cost: §e$cost"),
                Component.text("§7Damage: §c$damage"),
                Component.text("§7Attack Interval: §b${damageInterval}s"),
                Component.text("§7Range: §a$range"),
                Component.text("§7Upgrade Path: §d$upgradePath"),
                Component.text(""),
                Component.text("§7Baby: ${if (isBaby) "§aYes" else "§cNo"}"),
                Component.text("§7Size: §e$entitySize")
            )
        )

        // Store tower data in PDC
        meta.persistentDataContainer.set(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Generated_Tower")
        meta.persistentDataContainer.set(
            TowerDefMC.createKey("tower_generator_data"), PersistentDataType.STRING, data.toItemMetaString()
        )
        meta.persistentDataContainer.set(TowerDefMC.TOWER_RANGE, PersistentDataType.DOUBLE, range)
        meta.persistentDataContainer.set(TowerDefMC.TOWER_DMG, PersistentDataType.DOUBLE, damage)
        meta.persistentDataContainer.set(TowerDefMC.ATTACK_WAIT_TIME, PersistentDataType.DOUBLE, damageInterval)

        towerItem.itemMeta = meta

        // Give to player
        val leftover = player.inventory.addItem(towerItem)
        if (leftover.isNotEmpty()) {
            player.world.dropItemNaturally(player.location, towerItem)
        }

        player.closeInventory()
        player.sendMessage("§a§lTower generated successfully!")
        player.sendMessage("§7${displayName} §ahas been added to your inventory.")
    }

    private fun awaitNumberInput(
        player: Player,
        @Suppress("UNUSED_PARAMETER") key: String,
        @Suppress("UNUSED_PARAMETER") callback: (String) -> Unit
    ) {
        // This is a simplified version - you'd need to integrate with MenuListener
        // For now, we'll use a basic approach
        player.sendMessage("§7(Type a number in chat)")
    }

    private fun awaitStringInput(
        player: Player,
        @Suppress("UNUSED_PARAMETER") key: String,
        @Suppress("UNUSED_PARAMETER") callback: (String) -> Unit
    ) {
        // This is a simplified version - you'd need to integrate with MenuListener
        player.sendMessage("§7(Type in chat)")
    }
}
