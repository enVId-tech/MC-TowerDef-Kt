package dev.etran.towerDefMc.menus.generators

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.EnemyGeneratorData
import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class EnemyGeneratorMenu(player: Player, private val spawnEggType: EntityType) : CustomMenu(player, 54, "Enemy Generator") {

    private var displayName: String = "Custom Enemy"
    private var health: Double = 20.0
    private var speed: Double = 1.0
    private var defenseMultiplier: Double = 1.0
    private var canBeStunned: Boolean = true
    private var canStunTowers: Boolean = false
    private var stunDuration: Double = 0.0
    private var isBaby: Boolean = false
    private var entitySize: Double = 1.0
    private var itemMaterial: Material = Material.ARROW

    override fun setMenuItems() {
        // Display the item material being used (slot 4) - clickable to change
        val displayItem = ItemStack(itemMaterial)
        val displayMeta = displayItem.itemMeta
        displayMeta.displayName(Component.text("§6§lItem Representation"))
        displayMeta.lore(listOf(
            Component.text("§7Current: §e${itemMaterial.name}"),
            Component.text("§7Entity Type: §e${spawnEggType.name}"),
            Component.text(""),
            Component.text("§eClick to change item material!")
        ))
        displayMeta.addEnchant(Enchantment.UNBREAKING, 1, true)
        displayMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        displayItem.itemMeta = displayMeta
        inventory.setItem(4, displayItem)

        // Enemy Properties
        inventory.setItem(
            10, createRenamableItem(
                Material.RED_DYE, "§c§lHealth: {VALUE}", listOf("§7Click to change health"), health.toString()
            )
        )

        inventory.setItem(
            11, createRenamableItem(
                Material.SUGAR,
                "§b§lSpeed: {VALUE}",
                listOf("§7Movement speed multiplier", "§7Click to change"),
                speed.toString()
            )
        )

        inventory.setItem(
            12, createRenamableItem(
                Material.IRON_CHESTPLATE,
                "§7§lDefense Multiplier: {VALUE}",
                listOf("§7Damage reduction", "§71.0 = normal, 0.5 = 50% less damage", "§7Click to change"),
                defenseMultiplier.toString()
            )
        )

        inventory.setItem(
            13, createMenuItem(
                if (canBeStunned) Material.LIME_DYE else Material.GRAY_DYE,
                "§e§lCan Be Stunned: " + if (canBeStunned) "§aYes" else "§cNo",
                listOf("§7Can this enemy be stunned?", "§7Click to toggle")
            )
        )

        inventory.setItem(
            14, createMenuItem(
                if (canStunTowers) Material.LIME_DYE else Material.GRAY_DYE,
                "§e§lCan Stun Towers: " + if (canStunTowers) "§aYes" else "§cNo",
                listOf("§7Can this enemy stun towers?", "§7Click to toggle")
            )
        )

        inventory.setItem(
            15, createRenamableItem(
                Material.ENDER_PEARL,
                "§d§lStun Duration: {VALUE}s",
                listOf("§7How long to stun towers", "§7(if can stun towers)", "§7Click to change"),
                stunDuration.toString()
            )
        )

        inventory.setItem(
            16, createRenamableItem(
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
                Material.LIME_CONCRETE, "§a§lGenerate Enemy Item", listOf(
                    "§7Creates an enemy spawn item", "§7with the configured properties", "", "§eClick to generate!"
                )
            )
        )

        inventory.setItem(
            53, createMenuItem(
                Material.BOOK, "§e§lHelp", listOf(
                    "§7Run /tdgenerator enemy <type>",
                    "§7to configure enemy properties",
                    "§7Example: /tdgenerator enemy ZOMBIE"
                )
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return

        when (event.slot) {
            4 -> { // Item material selection
                MaterialSelectorMenu(player, itemMaterial) { selectedMaterial ->
                    itemMaterial = selectedMaterial
                    setMenuItems()
                    open()
                }.open()
            }

            10 -> { // Health
                player.closeInventory()
                player.sendMessage("§eEnter the enemy health (or 'cancel'):")
                player.sendMessage("§7(Type a number in chat)")
            }

            11 -> { // Speed
                player.closeInventory()
                player.sendMessage("§eEnter the speed multiplier (or 'cancel'):")
                player.sendMessage("§7(Type a number in chat)")
            }

            12 -> { // Defense
                player.closeInventory()
                player.sendMessage("§eEnter the defense multiplier (or 'cancel'):")
                player.sendMessage("§7(Type a number in chat)")
            }

            13 -> { // Can be stunned toggle
                canBeStunned = !canBeStunned
                setMenuItems()
            }

            14 -> { // Can stun towers toggle
                canStunTowers = !canStunTowers
                setMenuItems()
            }

            15 -> { // Stun Duration
                player.closeInventory()
                player.sendMessage("§eEnter the stun duration in seconds (or 'cancel'):")
                player.sendMessage("§7(Type a number in chat)")
            }

            16 -> { // Display Name
                player.closeInventory()
                player.sendMessage("§eEnter the display name (or 'cancel'):")
                player.sendMessage("§7(Type in chat)")
            }

            28 -> { // Baby toggle
                isBaby = !isBaby
                setMenuItems()
            }

            29 -> { // Size
                player.closeInventory()
                player.sendMessage("§eEnter the entity size multiplier (or 'cancel'):")
                player.sendMessage("§7(Type a number in chat)")
            }

            45 -> { // Cancel
                player.closeInventory()
                player.sendMessage("§cEnemy generation cancelled.")
            }

            49 -> { // Generate
                generateEnemy(player)
            }
        }
    }

    private fun generateEnemy(player: Player) {
        val data = EnemyGeneratorData(
            spawnEggType = spawnEggType,
            displayName = displayName,
            health = health,
            speed = speed,
            defenseMultiplier = defenseMultiplier,
            canBeStunned = canBeStunned,
            canStunTowers = canStunTowers,
            stunDuration = stunDuration,
            isBaby = isBaby,
            size = entitySize,
            itemMaterial = itemMaterial
        )

        // Generate a unique ID for the enemy
        val enemyId = "Generated_${displayName.replace(" ", "_")}_${System.currentTimeMillis()}"

        // Create enemy type for registry
        val enemyType = dev.etran.towerDefMc.registries.EnemyRegistry.EnemyType(
            id = enemyId,
            displayName = displayName,
            icon = itemMaterial,
            description = listOf(
                "Entity: ${spawnEggType.name}",
                "Health: $health",
                "Speed: ${speed}x",
                "Defense: ${defenseMultiplier}x",
                if (canStunTowers) "Can stun towers for ${stunDuration}s" else "Cannot stun towers"
            ),
            health = health,
            speed = speed,
            damage = 0 // Generated enemies don't have damage stat in registry
        )

        // Add to registry
        dev.etran.towerDefMc.registries.EnemyRegistry.addGeneratedEnemy(enemyId, enemyType)

        player.closeInventory()
        player.sendMessage("§a§lEnemy added to registry!")
        player.sendMessage("§7$displayName §ahas been added to the enemies selection menu.")
        player.sendMessage("§7You can now select it when creating waves.")
    }
}
