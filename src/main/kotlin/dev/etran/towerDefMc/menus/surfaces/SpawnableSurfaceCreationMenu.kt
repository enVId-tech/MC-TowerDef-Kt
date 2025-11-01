package dev.etran.towerDefMc.menus.surfaces

import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.utils.CustomMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
class SpawnableSurfaceCreationMenu(
    player: Player, val gameId: Int
) : CustomMenu(player, 54, "Create Spawnable Surface") {

    private val gameManager = GameRegistry.allGames[gameId]

    override fun setMenuItems() {
        if (gameManager == null) {
            player.sendMessage("§cError: Game not found!")
            player.closeInventory()
            return
        }

        // Name input
        inventory.setItem(
            13,
            createRenamableItem(
                Material.NAME_TAG,
                "Surface Name: {VALUE}",
                listOf("§7The name of this spawnable surface", "§eCurrent: {VALUE}"),
                "Spawnable Surface"
            )
        )

        // Material selection - Common materials
        val materials = listOf(
            Material.STONE to "§7Stone",
            Material.GRASS_BLOCK to "§aGrass Block",
            Material.DIRT to "§6Dirt",
            Material.SAND to "§eSand",
            Material.COBBLESTONE to "§7Cobblestone",
            Material.OAK_PLANKS to "§6Oak Planks",
            Material.BARRIER to "§cBarrier (Invisible)",
            Material.GLASS to "§bGlass (Transparent)",
            Material.WHITE_CONCRETE to "§fWhite Concrete",
            Material.GRAY_CONCRETE to "§7Gray Concrete"
        )

        materials.forEachIndexed { index, (material, displayName) ->
            val row = 2 + (index / 7)
            val col = 1 + (index % 7)
            val slot = row * 9 + col

            inventory.setItem(
                slot,
                createMenuItem(
                    material,
                    displayName,
                    listOf(
                        "§7Use this material for",
                        "§7the spawnable surface",
                        "",
                        "§eClick to select"
                    )
                )
            )
        }

        // Create button
        inventory.setItem(
            49,
            createMenuItem(
                Material.EMERALD_BLOCK,
                "§a§lCreate Surface",
                listOf(
                    "§7Create the spawnable surface",
                    "§7You will then be able to place blocks",
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
                listOf("Return to spawnable surfaces")
            )
        )
    }

    override fun handleClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (gameManager == null) return

        val clickedItem = event.currentItem ?: return

        when (event.slot) {
            13 -> handleNameUpdate(event)
            49 -> handleCreate()
            53 -> handleBack()
            else -> {
                // Check if clicked on a material
                if (clickedItem.type != Material.AIR && clickedItem.type.isBlock) {
                    handleMaterialSelection(clickedItem.type)
                }
            }
        }
    }

    private var selectedName = "Spawnable Surface"
    private var selectedMaterial = Material.BARRIER

    private fun handleNameUpdate(event: InventoryClickEvent) {
        player.closeInventory()
        player.sendMessage("§eEnter the name for this spawnable surface in chat:")

        // Set up chat listener for name input
        dev.etran.towerDefMc.managers.SpawnModeManager.startChatInput(player) { input ->
            selectedName = input
            player.sendMessage("§aName set to: $input")
            SpawnableSurfaceCreationMenu(player, gameId).open()
        }
    }

    private fun handleMaterialSelection(material: Material) {
        selectedMaterial = material
        player.sendMessage("§aSelected material: ${material.name}")

        // Refresh menu to show selection
        SpawnableSurfaceCreationMenu(player, gameId).open()
    }

    private fun handleCreate() {
        if (gameManager == null) return

        val surfaceId = gameManager.spawnableSurfaceManager.createSurface(selectedName, selectedMaterial)
        gameManager.saveGame()

        player.sendMessage("§aCreated spawnable surface: $selectedName")
        player.closeInventory()

        // Open the modification menu to place blocks
        SpawnableSurfaceModificationMenu(player, gameId, surfaceId).open()
    }

    private fun handleBack() {
        player.closeInventory()
        SpawnableSurfacesMenu(player, gameId).open()
    }
}
