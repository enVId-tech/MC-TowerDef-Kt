package dev.etran.towerDefMc.utils

import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Applies a specified status effect to a mob.
 *
 * @param entity The mob (LivingEntity) to apply the effect to.
 * @param type The PotionEffectType (e.g., PotionEffectType.SPEED).
 * @param seconds The duration of the effect in real-time seconds.
 * @param level The effect amplifier (1 for Level II, 2 for Level III, etc.).
 */
fun applyMobEffect(entity: LivingEntity, type: PotionEffectType, seconds: Int, level: Int) {
    // 20 ticks = 1 second
    val durationTicks = seconds * 20

    // The amplifier is zero-indexed, so Level I is 0, Level II is 1, etc.
    val amplifier = level - 1

    // Create the PotionEffect object
    val effect = PotionEffect(type, durationTicks, amplifier)

    // Apply the effect to the entity
    entity.addPotionEffect(effect)
}