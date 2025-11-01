package dev.etran.towerDefMc.menus.generators

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.data.EnemyGeneratorData
import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class EnemyGeneratorMenu(player: Player) : CustomMenu(player, 54, "Enemy Generator") {

    private var spawnEggType: EntityType = EntityType.ZOMBIE
    private var displayName: String = "Custom Enemy"
    private var health: Double = 20.0
    private var speed: Double = 1.0
    private var defenseMultiplier: Double = 1.0
    private var canBeStunned: Boolean = true
    private var canStunTowers: Boolean = false
    private var stunDuration: Double = 0.0
    private var isBaby: Boolean = false
    private var entitySize: Double = 1.0

    override fun setMenuItems() {
        // Check if player is holding a spawn egg
        val heldItem = player.inventory.itemInMainHand
        if (heldItem.type.name.endsWith("_SPAWN_EGG")) {
            val eggName = heldItem.type.name.replace("_SPAWN_EGG", "")
            try {
                spawnEggType = EntityType.valueOf(eggName)
            } catch (e: Exception) {
                player.sendMessage("§cInvalid spawn egg type!")
            }
        }

        // Display the spawn egg being configured (slot 4)
        val spawnEggMaterial = Material.getMaterial("${spawnEggType.name}_SPAWN_EGG") ?: Material.ZOMBIE_SPAWN_EGG
        inventory.setItem(4, createMenuItem(
            spawnEggMaterial,
            "§6§lEnemy Spawn Egg",
            listOf("§7Entity Type: §e${spawnEggType.name}")
        ))

        // Enemy Properties
        inventory.setItem(10, createRenamableItem(
            Material.RED_DYE,
            "§c§lHealth: {VALUE}",
            listOf("§7Click to change health"),
            health.toString()
        ))

        inventory.setItem(11, createRenamableItem(
            Material.SUGAR,
            "§b§lSpeed: {VALUE}",
            listOf("§7Movement speed multiplier", "§7Click to change"),
            speed.toString()
        ))

        inventory.setItem(12, createRenamableItem(
            Material.IRON_CHESTPLATE,
            "§7§lDefense Multiplier: {VALUE}",
            listOf("§7Damage reduction", "§71.0 = normal, 0.5 = 50% less damage", "§7Click to change"),
            defenseMultiplier.toString()
        ))

        inventory.setItem(13, createMenuItem(
            if (canBeStunned) Material.LIME_DYE else Material.GRAY_DYE,
            "§e§lCan Be Stunned: " + if (canBeStunned) "§aYes" else "§cNo",
            listOf("§7Can this enemy be stunned?", "§7Click to toggle")
        ))

        inventory.setItem(14, createMenuItem(
            if (canStunTowers) Material.LIME_DYE else Material.GRAY_DYE,
            "§e§lCan Stun Towers: " + if (canStunTowers) "§aYes" else "§cNo",
            listOf("§7Can this enemy stun towers?", "§7Click to toggle")
        ))

        inventory.setItem(15, createRenamableItem(
            Material.ENDER_PEARL,
            "§d§lStun Duration: {VALUE}s",
            listOf("§7How long to stun towers", "§7(if can stun towers)", "§7Click to change"),
            stunDuration.toString()
        ))

        inventory.setItem(16, createRenamableItem(
            Material.NAME_TAG,
            "§f§lDisplay Name: {VALUE}",
            listOf("§7Click to change name"),
            displayName
        ))

        // Mob Properties
        inventory.setItem(28, createMenuItem(
            if (isBaby) Material.LIME_DYE else Material.GRAY_DYE,
            "§e§lBaby Mode: " + if (isBaby) "§aEnabled" else "§cDisabled",
            listOf("§7Click to toggle")
        ))

        inventory.setItem(29, createRenamableItem(
            Material.SLIME_BALL,
            "§e§lSize: {VALUE}",
            listOf("§7Entity scale multiplier", "§7Click to change"),
            entitySize.toString()
        ))

        // Separator
        for (i in 36..44) {
            inventory.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " "))
        }

        // Action buttons
        inventory.setItem(45, createMenuItem(
            Material.BARRIER,
            "§c§lCancel",
            listOf("§7Close without saving")
        ))

        inventory.setItem(49, createMenuItem(
            Material.LIME_CONCRETE,
            "§a§lGenerate Enemy Item",
            listOf(
                "§7Creates an enemy spawn item",
                "§7with the configured properties",
                "",
                "§eClick to generate!"
            )
        ))

        inventory.setItem(53, createMenuItem(
            Material.BOOK,
            "§e§lHelp",
            listOf(
                "§7Hold a spawn egg and run",
                "§7/tdgenerator enemy",
                "§7to configure enemy properties"
            )
        ))
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return

        when (event.slot) {
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
            size = entitySize
        )

        // Create the enemy item
        val spawnEggMaterial = Material.getMaterial("${spawnEggType.name}_SPAWN_EGG") ?: Material.ZOMBIE_SPAWN_EGG
        val enemyItem = ItemStack(spawnEggMaterial, 1)
        val meta = enemyItem.itemMeta

        meta.displayName(Component.text("§c$displayName"))
        meta.lore(listOf(
            Component.text("§7Health: §c$health"),
            Component.text("§7Speed: §b$speed"),
            Component.text("§7Defense: §7${defenseMultiplier}x"),
            Component.text("§7Can Be Stunned: ${if (canBeStunned) "§aYes" else "§cNo"}"),
            Component.text("§7Can Stun Towers: ${if (canStunTowers) "§aYes" else "§cNo"}"),
            Component.text("§7Stun Duration: §d${stunDuration}s"),
            Component.text(""),
            Component.text("§7Baby: ${if (isBaby) "§aYes" else "§cNo"}"),
            Component.text("§7Size: §e$entitySize")
        ))

        // Store enemy data in PDC
        meta.persistentDataContainer.set(TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Generated_Enemy")
        meta.persistentDataContainer.set(TowerDefMC.createKey("enemy_generator_data"), PersistentDataType.STRING, data.toItemMetaString())

        enemyItem.itemMeta = meta

        // Give to player
        val leftover = player.inventory.addItem(enemyItem)
        if (leftover.isNotEmpty()) {
            player.world.dropItemNaturally(player.location, enemyItem)
        }

        player.closeInventory()
        player.sendMessage("§a§lEnemy generated successfully!")
        player.sendMessage("§7${displayName} §ahas been added to your inventory.")
    }
}
