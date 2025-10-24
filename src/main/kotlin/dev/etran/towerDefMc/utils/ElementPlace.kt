package dev.etran.towerDefMc.utils

import de.tr7zw.nbtapi.NBT
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractEvent

fun placeElement(event: PlayerInteractEvent, loggerName: String, nbtId: String, nbtIdData: String): Entity? {
    event.isCancelled = true

    val block = event.clickedBlock ?: return null
    val location = block.location.add(0.5, 1.0, 0.5)
    val player = event.player

    player.sendBlockChange(location, location.block.blockData)

    // Prevents the player from placing checkpoints on top of each other, possibly causing bugs
    if (location.getNearbyEntities(0.5, 1.0, 0.5).count() >= 1) {
        player.sendMessage("You cannot place a game element of type $loggerName here!")
        return null
    }

    val world = location.world
    val entity = world.spawnEntity(location, EntityType.ARMOR_STAND)

    // Add NBT data to armor stands
    entity.isInvulnerable = true
    entity.fireTicks = 0
    entity.setGravity(false)

    NBT.modify(entity) { nbt ->
        nbt.setString(nbtId, nbtIdData)
    }

    return entity
}