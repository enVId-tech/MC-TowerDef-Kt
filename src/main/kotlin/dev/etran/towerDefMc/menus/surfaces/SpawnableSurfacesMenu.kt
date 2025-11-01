package dev.etran.towerDefMc.menus.surfaces

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.menus.games.ModifyGame
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SpawnableSurfacesMenu(
    player: Player, val gameId: Int
) : CustomMenu(player, 54, "Spawnable Surfaces") {

    private val gameManager = GameRegistry.allGames[gameId]

    override fun setMenuItems() {
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            player.closeInventory()
            return
        }

        val surfaces = gameManager.spawnableSurfaceManager.getAllSurfaces()

        // Display all spawnable surfaces
        surfaces.forEachIndexed { index, surface ->
            if (index < 45) { // Leave bottom row for actions
                val item = ItemStack(surface.material)
                val meta = item.itemMeta
                meta.displayName(Component.text("§e${surface.name}"))

                val lore = mutableListOf(
                    "§7Material: §f${surface.material.name}",
                    "§7Locations: §f${surface.locations.size}",
                    "",
                    "§aLeft-click: §7Edit blocks",
                    "§cRight-click: §7Delete surface"
                )
                meta.lore(lore.map { Component.text(it) })

                // Store surface ID in PDC
                meta.persistentDataContainer.set(
                    TowerDefMC.createKey("surface_id"),
                    PersistentDataType.INTEGER,
                    surface.id
                )

                item.itemMeta = meta
                inventory.setItem(index, item)
            }
        }

        // Create New Surface button
        inventory.setItem(
            49,
            createMenuItem(
                Material.LIME_DYE,
                "§a§lCreate New Surface",
                listOf(
                    "§7Create a new spawnable surface",
                    "§7for tower placement",
                    "",
                    "§eClick to create"
                )
            )
        )

        // Back button
        inventory.setItem(
            53,
            createMenuItem(
                Material.OAK_DOOR,
                "§eBack",
                listOf("Return to game modification")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (gameManager == null) return

        val clickedItem = event.currentItem ?: return
        val meta = clickedItem.itemMeta ?: return

        when (event.slot) {
            49 -> handleCreateSurface()
            53 -> handleBack()
            else -> {
                // Check if clicked on a surface item
                val surfaceId = meta.persistentDataContainer.get(
                    TowerDefMC.createKey("surface_id"),
                    PersistentDataType.INTEGER
                )

                if (surfaceId != null) {
                    if (event.isLeftClick) {
                        handleEditSurface(surfaceId)
                    } else if (event.isRightClick) {
                        handleDeleteSurface(surfaceId)
                    }
                }
            }
        }
    }

    private fun handleCreateSurface() {
        player.closeInventory()
        SpawnableSurfacesMenu(player, gameId).open()
    }

    private fun handleEditSurface(surfaceId: Int) {
        player.closeInventory()
        SpawnableSurfaceModificationMenu(player, gameId, surfaceId).open()
    }

    private fun handleDeleteSurface(surfaceId: Int) {
        if (gameManager == null) return

        val surface = gameManager.spawnableSurfaceManager.getSurface(surfaceId)
        if (surface != null) {
            gameManager.spawnableSurfaceManager.deleteSurface(surfaceId)
            gameManager.saveGame()
            player.sendMessage("§aDeleted spawnable surface: ${surface.name}")

            // Refresh menu
            player.closeInventory()
            SpawnableSurfacesMenu(player, gameId).open()
        }
    }

    private fun handleBack() {
        player.closeInventory()
        ModifyGame(player, gameId).open()
    }
}

