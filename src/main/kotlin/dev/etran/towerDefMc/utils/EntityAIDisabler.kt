package dev.etran.towerDefMc.utils

import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Utility object for completely disabling all AI and abilities for any mob type.
 * This ensures mobs used as towers or enemies cannot attack players, shoot projectiles,
 * teleport, explode, or perform any other vanilla behaviors.
 */
object EntityAIDisabler {

    /**
     * Completely disables all AI, attacks, and special abilities for any living entity.
     * Safe to call on any EntityType - only applies changes where applicable.
     */
    fun disableAllAI(entity: LivingEntity) {
        // Basic AI disabling
        entity.setAI(false)
        entity.isCollidable = false
        entity.isSilent = true

        // Clear target for mobs that can target (safe for all types)
        if (entity is Mob) {
            entity.target = null
        }

        // Prevent all potion effects that could cause issues
        entity.removePotionEffect(PotionEffectType.REGENERATION)
        entity.removePotionEffect(PotionEffectType.POISON)
        entity.removePotionEffect(PotionEffectType.WITHER)

        // Apply slowness to ensure mob doesn't move on its own
        // (movement will be controlled by our pathfinding system for enemies)
        entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, Int.MAX_VALUE, 255, false, false, false))

        // Handle specific mob types with special abilities
        when (entity) {
            is Wither -> disableWitherAbilities(entity)
            is EnderDragon -> disableEnderDragonAbilities(entity)
            is Creeper -> disableCreeperAbilities(entity)
            is Ghast -> disableGhastAbilities(entity)
            is Blaze -> disableBlazeAbilities(entity)
            is Shulker -> disableShulkerAbilities(entity)
            is Enderman -> disableEndermanAbilities(entity)
            is Phantom -> disablePhantomAbilities(entity)
            is Vex -> disableVexAbilities(entity)
            is Evoker -> disableEvokerAbilities(entity)
            is Witch -> disableWitchAbilities(entity)
            is Guardian -> disableGuardianAbilities(entity)
            is Vindicator -> disableVindicatorAbilities(entity)
            is Pillager -> disablePillagerAbilities(entity)
            is Skeleton -> disableSkeletonAbilities(entity)
            is Stray -> disableStrayAbilities(entity)
            is WitherSkeleton -> disableWitherSkeletonAbilities(entity)
            is Drowned -> disableDrownedAbilities(entity)
            is PiglinBrute -> disablePiglinBruteAbilities(entity)
            is Piglin -> disablePiglinAbilities(entity)
            is Hoglin -> disableHoglinAbilities(entity)
            is Zoglin -> disableZoglinAbilities(entity)
            is Bee -> disableBeeAbilities(entity)
            is Spider -> disableSpiderAbilities(entity)
            is Slime -> disableSlimeAbilities(entity)
            is Golem -> disableGolemAbilities(entity)
            is Ravager -> disableRavagerAbilities(entity)
            is Llama -> disableLlamaAbilities(entity)
        }
    }

    private fun disableWitherAbilities(wither: Wither) {
        wither.isInvulnerable = false // We need to be able to damage it
        wither.target = null
        wither.setAI(false)
    }

    private fun disableEnderDragonAbilities(dragon: EnderDragon) {
        try {
            dragon.phase = EnderDragon.Phase.CIRCLING
        } catch (_: Exception) {
            // Phase may not be settable in some versions
        }
        dragon.setAI(false)
    }

    private fun disableCreeperAbilities(creeper: Creeper) {
        creeper.explosionRadius = 0
        creeper.maxFuseTicks = Int.MAX_VALUE
        creeper.isPowered = false
        creeper.target = null
    }

    private fun disableGhastAbilities(ghast: Ghast) {
        ghast.target = null
        ghast.setAI(false)
    }

    private fun disableBlazeAbilities(blaze: Blaze) {
        blaze.target = null
        blaze.setAI(false)
    }

    private fun disableShulkerAbilities(shulker: Shulker) {
        shulker.target = null
        shulker.setAI(false)
    }

    private fun disableEndermanAbilities(enderman: Enderman) {
        enderman.target = null
        enderman.setAI(false)
    }

    private fun disablePhantomAbilities(phantom: Phantom) {
        phantom.target = null
        phantom.setAI(false)
    }

    private fun disableVexAbilities(vex: Vex) {
        vex.target = null
        vex.setAI(false)
        vex.isCharging = false
    }

    private fun disableEvokerAbilities(evoker: Evoker) {
        evoker.target = null
        evoker.setAI(false)
        // Spell casting is disabled by AI being disabled
    }

    private fun disableWitchAbilities(witch: Witch) {
        witch.target = null
        witch.setAI(false)
    }

    private fun disableGuardianAbilities(guardian: Guardian) {
        guardian.target = null
        guardian.setAI(false)
        guardian.setLaser(false)
    }

    private fun disableVindicatorAbilities(vindicator: Vindicator) {
        vindicator.target = null
        vindicator.setAI(false)
        vindicator.isJohnny = false
    }

    private fun disablePillagerAbilities(pillager: Pillager) {
        pillager.target = null
        pillager.setAI(false)
        pillager.equipment.setItemInMainHand(null)
        pillager.equipment.setItemInOffHand(null)
    }

    private fun disableSkeletonAbilities(skeleton: Skeleton) {
        skeleton.target = null
        skeleton.setAI(false)
        skeleton.equipment.setItemInMainHand(null)
        skeleton.equipment.setItemInOffHand(null)
    }

    private fun disableStrayAbilities(stray: Stray) {
        stray.target = null
        stray.setAI(false)
        stray.equipment.setItemInMainHand(null)
        stray.equipment.setItemInOffHand(null)
    }

    private fun disableWitherSkeletonAbilities(witherSkeleton: WitherSkeleton) {
        witherSkeleton.target = null
        witherSkeleton.setAI(false)
        witherSkeleton.equipment.setItemInMainHand(null)
        witherSkeleton.equipment.setItemInOffHand(null)
    }

    private fun disableDrownedAbilities(drowned: Drowned) {
        drowned.target = null
        drowned.setAI(false)
        drowned.equipment.setItemInMainHand(null)
        drowned.equipment.setItemInOffHand(null)
    }

    private fun disablePiglinBruteAbilities(piglinBrute: PiglinBrute) {
        piglinBrute.target = null
        piglinBrute.setAI(false)
        piglinBrute.equipment.setItemInMainHand(null)
        piglinBrute.equipment.setItemInOffHand(null)
        piglinBrute.isImmuneToZombification = true
    }

    private fun disablePiglinAbilities(piglin: Piglin) {
        piglin.target = null
        piglin.setAI(false)
        piglin.equipment.setItemInMainHand(null)
        piglin.equipment.setItemInOffHand(null)
        piglin.isImmuneToZombification = true
    }

    private fun disableHoglinAbilities(hoglin: Hoglin) {
        hoglin.target = null
        hoglin.setAI(false)
        hoglin.isImmuneToZombification = true
    }

    private fun disableZoglinAbilities(zoglin: Zoglin) {
        zoglin.target = null
        zoglin.setAI(false)
    }

    private fun disableBeeAbilities(bee: Bee) {
        bee.target = null
        bee.setAI(false)
        bee.anger = 0
        bee.cannotEnterHiveTicks = Int.MAX_VALUE
    }

    private fun disableSpiderAbilities(spider: Spider) {
        spider.target = null
        spider.setAI(false)
    }

    private fun disableSlimeAbilities(slime: Slime) {
        slime.target = null
        slime.setAI(false)
        slime.size = 1 // Prevent splitting
    }

    private fun disableGolemAbilities(golem: Golem) {
        golem.setAI(false)
        if (golem is IronGolem) {
            golem.target = null
        } else if (golem is Snowman) {
            golem.isDerp = true
        }
    }

    private fun disableRavagerAbilities(ravager: Ravager) {
        ravager.target = null
        ravager.setAI(false)
        ravager.attackTicks = 0
    }

    private fun disableLlamaAbilities(llama: Llama) {
        llama.target = null
        llama.setAI(false)
    }
}
