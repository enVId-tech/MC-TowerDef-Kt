package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.CustomMenu
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

object MenuListener : Listener {
    private val openMenus = mutableMapOf<UUID, CustomMenu>()
    val awaitingRename: MutableMap<UUID, RenameContext> = mutableMapOf()
    private lateinit var plugin: TowerDefMC

    data class RenameContext(
        val itemToRename: ItemStack,
        val sourceSlot: Int,
        val menuId: UUID
    )

    fun initialize(plugin: TowerDefMC) {
        this.plugin = plugin
    }

    fun registerMenu(player: Player, menu: CustomMenu) {
        openMenus[player.uniqueId] = menu
    }

    fun unregisterMenu(player: Player) {
        openMenus.remove(player.uniqueId)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val currentMenu = openMenus[player.uniqueId] ?: return

        currentMenu.handleGlobalClick(event)

        // Prevent user from moving inventory items
        if (event.inventory == currentMenu.inventory) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onChatInput(event: AsyncChatEvent) {
        val player = event.player
        val playerUUID = player.uniqueId

        val context = awaitingRename[playerUUID] ?: return

        event.isCancelled = true
        val inputComponent = event.message()
        val input = PlainTextComponentSerializer.plainText().serialize(inputComponent).trim()

        if (input.equals("cancel", true)) {
            awaitingRename.remove(playerUUID)
            player.sendMessage(Component.text("Item renaming cancelled.").color(TextColor.color(0xFF0000)))
            openMenus[playerUUID]?.open()
            return
        }

        object : BukkitRunnable() {
            override fun run() {
                val currentMenu = openMenus[playerUUID]
                val meta = context.itemToRename.itemMeta.clone()
                val rawName = input

                val displayNameComponent = TowerDefMC.MINI_MESSAGE.deserialize(rawName.replace("§", "&"))

                meta.displayName(displayNameComponent)

                val cleanName = PlainTextComponentSerializer.plainText().serialize(displayNameComponent).trim()

                meta.persistentDataContainer.set(
                    TowerDefMC.TITLE_KEY,
                    PersistentDataType.STRING,
                    cleanName
                )

                context.itemToRename.itemMeta = meta
                currentMenu?.inventory?.setItem(context.sourceSlot, context.itemToRename)

                awaitingRename.remove(playerUUID)
                player.sendMessage(
                    Component.text("Item successfully renamed to: ").color(TextColor.color(0x55FF55))
                        .append(displayNameComponent)
                )

                currentMenu?.open()
            }
        }.runTask(plugin)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        unregisterMenu(event.player as Player)
    }
}