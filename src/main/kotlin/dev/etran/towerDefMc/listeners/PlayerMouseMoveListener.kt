package dev.etran.towerDefMc.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMouseMoveListener : Listener {
    @EventHandler
    fun onPlayerMouseMoveEvent(event: PlayerMoveEvent) {
        val from = event.from
        val to = event.to
        val hasMouseMoved = from.yaw != to.yaw || from.pitch != to.pitch
        val hasPlayerWalked = from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ

        if (hasMouseMoved) {
            val yawDelta = Math.abs(from.yaw - to.yaw)
            val pitchDelta = Math.abs(from.pitch - to.pitch)

        }
    }
}