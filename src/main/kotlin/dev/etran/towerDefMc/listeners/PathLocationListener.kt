package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.menus.enemies.PathLocationSetter
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Listener for path location setting interactions
 */
class PathLocationListener : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (PathLocationSetter.isSettingLocation(event.player)) {
            PathLocationSetter.handleInteract(event)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerChat(event: AsyncChatEvent) {
        if (PathLocationSetter.isSettingLocation(event.player)) {
            PathLocationSetter.handleChat(event)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        PathLocationSetter.cancelSetting(event.player)
    }
}

