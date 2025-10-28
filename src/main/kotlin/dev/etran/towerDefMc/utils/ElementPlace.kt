package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.managers.CheckpointManager.AMPLIFIER
import dev.etran.towerDefMc.managers.CheckpointManager.DURATION
import dev.etran.towerDefMc.managers.CheckpointManager.effectType
import dev.etran.towerDefMc.managers.CheckpointManager.standsAreVisible
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect

fun placeElement(event: PlayerInteractEvent, loggerName: String): Entity? {
    event.isCancelled = true

    val block = event.clickedBlock ?: return null
    val location = block.location.add(0.5, 1.0, 0.5)
    val player = event.player

    player.sendBlockChange(location, location.block.blockData)

    // Prevents the player from placing checkpoints on top of each other, possibly causing bugs
    /* TODO: Fix bug where user cannot place an entity close to them, because of this check.
        It should allow for users to only place instances of entities near them if no other entity exists
        on that block
     */
    if (location.getNearbyEntities(0.5, 1.0, 0.5).count() >= 1) {
        player.sendMessage("You cannot place a game element of type $loggerName here!")
        return null
    }

    val world = location.world
    val entity: ArmorStand = world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand

    // Add NBT data to armor stands
    if (standsAreVisible) {
        entity.isInvisible = false
        entity.addPotionEffect(PotionEffect(effectType, DURATION, AMPLIFIER))
    } else {
        entity.isInvisible = true
        entity.removePotionEffect(effectType)
    }
    entity.isInvulnerable = true
    entity.fireTicks = 0
    entity.setGravity(false)

    return entity
}