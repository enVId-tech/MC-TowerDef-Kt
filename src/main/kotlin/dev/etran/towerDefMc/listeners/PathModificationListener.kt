package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.managers.PathModificationSession
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Handles path modification mode where players punch waypoints to edit them
 */
class PathModificationListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val player = event.damager as? org.bukkit.entity.Player ?: return
        val stand = event.entity as? ArmorStand ?: return

        if (!PathModificationSession.isInSession(player)) return

        event.isCancelled = true
        PathModificationSession.handleWaypointPunch(player, stand)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (!PathModificationSession.isInSession(player)) return

        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) return

        val meta = item.itemMeta ?: return
        val isReplacementItem = meta.persistentDataContainer.has(
            TowerDefMC.createKey("pathReplacementItem"), PersistentDataType.STRING
        )

        if (!isReplacementItem) return

        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true

            val location = if (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock != null) {
                event.clickedBlock!!.location.add(0.5, 1.0, 0.5)
            } else {
                player.location.clone()
            }

            PathModificationSession.handleReplacement(player, location, item.type)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player

        if (!PathModificationSession.isInSession(player)) return

        event.isCancelled = true

        val message = PlainTextComponentSerializer.plainText().serialize(event.message()).lowercase()

        if (message == "finish") {
            Bukkit.getScheduler().runTask(TowerDefMC.instance, Runnable {
                PathModificationSession.finishSession(player)
            })
        } else {
            player.sendMessage("§7Type §afinish §7to complete modifications, or continue editing waypoints")
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (PathModificationSession.isInSession(event.player)) {
            PathModificationSession.cancelSession(event.player)
        }
    }
}

