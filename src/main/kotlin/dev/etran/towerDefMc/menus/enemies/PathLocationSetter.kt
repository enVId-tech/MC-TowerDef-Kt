package dev.etran.towerDefMc.menus.enemies

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID

/**
 * Utility for setting path locations via right-click
 */
object PathLocationSetter {
    private val activeSetters = mutableMapOf<UUID, LocationCallback>()

    data class LocationCallback(
        val gameId: Int,
        val callback: (Location) -> Unit
    )

    fun startLocationSetting(player: Player, gameId: Int, callback: (Location) -> Unit) {
        activeSetters[player.uniqueId] = LocationCallback(gameId, callback)
    }

    fun handleInteract(event: PlayerInteractEvent) {
        val player = event.player
        val setter = activeSetters[player.uniqueId] ?: return

        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true
            val location = event.clickedBlock?.location?.clone()?.add(0.5, 1.0, 0.5) ?: return

            activeSetters.remove(player.uniqueId)
            setter.callback(location)
        }
    }

    fun handleChat(event: AsyncChatEvent) {
        val player = event.player
        val setter = activeSetters[player.uniqueId] ?: return

        event.isCancelled = true
        activeSetters.remove(player.uniqueId)

        player.sendMessage("§cLocation setting cancelled.")
    }

    fun isSettingLocation(player: Player): Boolean {
        return activeSetters.containsKey(player.uniqueId)
    }

    fun cancelSetting(player: Player) {
        activeSetters.remove(player.uniqueId)
    }
}

