package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.registries.GameRegistry
import net.kyori.adventure.text.Component
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Handles placement of Tower Shop villager spawners
 */
class ShopVillagerPlacementListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        if (item.type != org.bukkit.Material.VILLAGER_SPAWN_EGG) return

        val meta = item.itemMeta ?: return
        val isShopSpawner = meta.persistentDataContainer.has(
            TowerDefMC.GAME_ITEMS,
            PersistentDataType.STRING
        ) && meta.persistentDataContainer.get(
            TowerDefMC.GAME_ITEMS,
            PersistentDataType.STRING
        ) == "Tower_Shop_Spawner"

        if (!isShopSpawner) return

        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true

            val clickedBlock = event.clickedBlock ?: return
            val spawnLocation = clickedBlock.location.add(0.5, 1.0, 0.5)

            // Get the game ID from the item (stored when given from ModifyGame menu)
            val gameId = meta.persistentDataContainer.get(
                TowerDefMC.GAME_ID_KEY,
                PersistentDataType.INTEGER
            )

            if (gameId == null) {
                player.sendMessage("§cThis shop villager spawner is not linked to a game!")
                player.sendMessage("§7Get a new one from the game modification menu")
                return
            }

            // Verify the game exists
            val game = GameRegistry.allGames[gameId]
            if (game == null) {
                player.sendMessage("§cThe game this shop was linked to no longer exists!")
                player.sendMessage("§7Game ID: §e$gameId")
                return
            }

            // Spawn the villager
            val villager = spawnLocation.world.spawnEntity(spawnLocation, EntityType.VILLAGER) as Villager

            // Configure the villager
            villager.setAI(false)
            villager.isInvulnerable = true
            villager.isPersistent = true
            villager.isSilent = false
            villager.setCollidable(false)
            villager.profession = Villager.Profession.LIBRARIAN
            villager.villagerLevel = 5

            // Make villager face the player
            val direction = player.location.toVector().subtract(spawnLocation.toVector()).normalize()
            val yaw = Math.toDegrees(Math.atan2(-direction.x, direction.z)).toFloat()
            val villagerLocation = villager.location
            villagerLocation.yaw = yaw
            villager.teleport(villagerLocation)

            // Set custom name
            villager.customName(Component.text("§6§l⚔ Tower Shop ⚔"))
            villager.isCustomNameVisible = true

            // Mark as shop villager
            villager.persistentDataContainer.set(
                TowerDefMC.createKey("tower_shop"),
                PersistentDataType.STRING,
                "true"
            )

            // Store game ID
            villager.persistentDataContainer.set(
                TowerDefMC.GAME_ID_KEY,
                PersistentDataType.INTEGER,
                gameId
            )

            // Mark as game element
            villager.persistentDataContainer.set(
                TowerDefMC.ELEMENT_TYPES,
                PersistentDataType.STRING,
                "Shop"
            )

            // Remove item from inventory if not in creative
            if (player.gameMode != org.bukkit.GameMode.CREATIVE &&
                player.gameMode != org.bukkit.GameMode.SPECTATOR) {
                player.inventory.itemInMainHand.amount -= 1
            }

            player.sendMessage("§a§lTower Shop villager placed!")
            player.sendMessage("§7Linked to game §e#${gameId} §7- §6${game.config.name}")
            player.sendMessage("§7Players in this game can buy towers when it starts")
        }
    }
}
