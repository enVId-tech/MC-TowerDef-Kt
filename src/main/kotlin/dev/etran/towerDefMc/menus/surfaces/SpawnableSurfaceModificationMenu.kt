package dev.etran.towerDefMc.menus.surfaces

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.SpawnModeManager
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SpawnableSurfaceModificationMenu(
    player: Player, val gameId: Int, val surfaceId: Int
) : CustomMenu(player, 27, "Edit Spawnable Surface") {

    private val gameManager = GameRegistry.allGames[gameId]
    private val surface = gameManager?.spawnableSurfaceManager?.getSurface(surfaceId)

    override fun setMenuItems() {
        if (gameManager == null || surface == null) {
            player.sendMessage("§cError: Surface not found!")
            player.closeInventory()
            return
        }

        // Surface info
        val infoItem = ItemStack(surface.material)
        val infoMeta = infoItem.itemMeta
        infoMeta.displayName(Component.text("§e${surface.name}"))
        infoMeta.lore(
            listOf(
                "§7Material: §f${surface.material.name}",
                "§7Blocks placed: §f${surface.locations.size}"
            ).map { Component.text(it) }
        )
        infoItem.itemMeta = infoMeta
        inventory.setItem(4, infoItem)

        // Rename button
        inventory.setItem(
            10,
            createMenuItem(
                Material.NAME_TAG,
                "§eRename Surface",
                listOf(
                    "§7Current name: §f${surface.name}",
                    "",
                    "§eClick to rename"
                )
            )
        )

        // Place blocks button
        val placeItem = ItemStack(surface.material)
        val placeMeta = placeItem.itemMeta
        placeMeta.displayName(Component.text("§a§lPlace Blocks"))
        placeMeta.lore(
            listOf(
                "§7Click to receive the block",
                "§7Right-click to place blocks",
                "§7Left-click to remove blocks",
                "§7All placed blocks will be highlighted",
                "",
                "§eClick to start placing"
            ).map { Component.text(it) }
        )
        // Store surface ID in the item
        placeMeta.persistentDataContainer.set(
            TowerDefMC.createKey("spawnable_surface_id"),
            PersistentDataType.INTEGER,
            surfaceId
        )
        placeMeta.persistentDataContainer.set(
            TowerDefMC.createKey("game_id"),
            PersistentDataType.INTEGER,
            gameId
        )
        placeItem.itemMeta = placeMeta
        inventory.setItem(13, placeItem)

        // Clear all blocks button
        inventory.setItem(
            16,
            createMenuItem(
                Material.TNT,
                "§c§lClear All Blocks",
                listOf(
                    "§7Remove all placed blocks",
                    "§7This cannot be undone!",
                    "§7Currently: §f${surface.locations.size} blocks",
                    "",
                    "§cClick to clear"
                )
            )
        )

        // Back button
        inventory.setItem(
            22,
            createMenuItem(
                Material.OAK_DOOR,
                "§eBack",
                listOf("Return to spawnable surfaces")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (gameManager == null || surface == null) return

        when (event.slot) {
            10 -> handleRename()
            13 -> handlePlaceBlocks()
            16 -> handleClearBlocks()
            22 -> handleBack()
        }
    }

    private fun handleRename() {
        player.closeInventory()
        player.sendMessage("§eEnter the new name for this spawnable surface in chat:")

        SpawnModeManager.startChatInput(player) { input ->
            if (gameManager != null) {
                gameManager.spawnableSurfaceManager.renameSurface(surfaceId, input)
                gameManager.saveGame()
                player.sendMessage("§aRenamed surface to: $input")
                SpawnableSurfaceModificationMenu(player, gameId, surfaceId).open()
            }
        }
    }

    private fun handlePlaceBlocks() {
        if (surface == null) return

        player.closeInventory()

        // Give player the spawnable surface block item
        val blockItem = ItemStack(surface.material, 1)
        val meta = blockItem.itemMeta
        meta.displayName(Component.text("§e${surface.name} §7(Place/Remove)"))
        meta.lore(
            listOf(
                "§7Right-click: §aPlace block",
                "§7Left-click: §cRemove block",
                "§7Drop (Q): §eFinish editing",
                "",
                "§7All placed blocks are highlighted"
            ).map { Component.text(it) }
        )
        meta.persistentDataContainer.set(
            TowerDefMC.createKey("spawnable_surface_id"),
            PersistentDataType.INTEGER,
            surfaceId
        )
        meta.persistentDataContainer.set(
            TowerDefMC.createKey("game_id"),
            PersistentDataType.INTEGER,
            gameId
        )
        blockItem.itemMeta = meta

        player.inventory.addItem(blockItem)
        player.sendMessage("§aPlace blocks for: ${surface.name}")
        player.sendMessage("§7Drop the item (Q) when finished")

        // Start spawnable surface mode
        SpawnModeManager.startSpawnableSurfaceMode(player, gameId, surfaceId)
    }

    private fun handleClearBlocks() {
        if (gameManager == null || surface == null) return

        surface.locations.forEach { location ->
            location.block.type = Material.AIR
        }
        surface.locations.clear()
        gameManager.saveGame()

        player.sendMessage("§aCleared all blocks for: ${surface.name}")

        // Refresh menu
        player.closeInventory()
        SpawnableSurfaceModificationMenu(player, gameId, surfaceId).open()
    }

    private fun handleBack() {
        player.closeInventory()
        SpawnableSurfacesMenu(player, gameId).open()
    }
}

