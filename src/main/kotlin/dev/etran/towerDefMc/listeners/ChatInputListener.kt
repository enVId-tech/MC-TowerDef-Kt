package dev.etran.towerDefMc.listeners

import dev.etran.towerDefMc.managers.SpawnModeManager
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ChatInputListener : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player

        // Check if player is in chat input mode
        if (SpawnModeManager.isInChatInputMode(player)) {
            event.isCancelled = true

            // Handle the chat input
            val message = event.message().toString()
            SpawnModeManager.handleChatInput(player, message)
        }
    }
}

