package dev.etran.towerDefMc.utils

import org.bukkit.entity.*
import org.bukkit.potion.PotionEffectType

/**
 * Utility object for disabling specific AI behaviors while keeping entities functional.
 * This ensures mobs used as towers or enemies cannot attack players, shoot projectiles,
 * teleport, explode, or perform vanilla behaviors, but can still be controlled by custom logic.
 */
object EntityAIDisabler {

    /**
     * Disables vanilla AI behaviors but keeps the entity responsive to custom systems.
     * AI is NOT fully disabled - only specific vanilla behaviors are removed.
     */
    fun disableAllAI(entity: LivingEntity) {
        // Clear target for mobs - prevents them from attacking players
        if (entity is Mob) {
            entity.target = null
            entity.isAware = false // This prevents pathfinding to players but allows custom movement
        }

        // Basic configuration
        entity.isCollidable = false
        entity.isSilent = true
        entity.removeWhenFarAway = false

        // Prevent all potion effects that could cause issues
        entity.removePotionEffect(PotionEffectType.REGENERATION)
        entity.removePotionEffect(PotionEffectType.POISON)
        entity.removePotionEffect(PotionEffectType.WITHER)

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
        wither.isInvulnerable = false
        wither.target = null
        wither.isAware = false
    }

    private fun disableEnderDragonAbilities(dragon: EnderDragon) {
        try {
            dragon.phase = EnderDragon.Phase.CIRCLING
        } catch (_: Exception) {
            // Phase may not be settable in some versions
        }
    }

    private fun disableCreeperAbilities(creeper: Creeper) {
        creeper.explosionRadius = 0
        creeper.maxFuseTicks = Int.MAX_VALUE
        creeper.isPowered = false
        creeper.target = null
    }

    private fun disableGhastAbilities(ghast: Ghast) {
        ghast.target = null
        ghast.isAware = false
    }

    private fun disableBlazeAbilities(blaze: Blaze) {
        blaze.target = null
        blaze.isAware = false
    }

    private fun disableShulkerAbilities(shulker: Shulker) {
        shulker.target = null
        shulker.isAware = false
    }

    private fun disableEndermanAbilities(enderman: Enderman) {
        enderman.target = null
        enderman.isAware = false
    }

    private fun disablePhantomAbilities(phantom: Phantom) {
        phantom.target = null
        phantom.isAware = false
    }

    private fun disableVexAbilities(vex: Vex) {
        vex.target = null
        vex.isAware = false
        vex.isCharging = false
    }

    private fun disableEvokerAbilities(evoker: Evoker) {
        evoker.target = null
        evoker.isAware = false
    }

    private fun disableWitchAbilities(witch: Witch) {
        witch.target = null
        witch.isAware = false
    }

    private fun disableGuardianAbilities(guardian: Guardian) {
        guardian.target = null
        guardian.isAware = false
        guardian.setLaser(false)
    }

    private fun disableVindicatorAbilities(vindicator: Vindicator) {
        vindicator.target = null
        vindicator.isAware = false
        vindicator.isJohnny = false
    }

    private fun disablePillagerAbilities(pillager: Pillager) {
        pillager.target = null
        pillager.isAware = false
        pillager.equipment.setItemInMainHand(null)
        pillager.equipment.setItemInOffHand(null)
    }

    private fun disableSkeletonAbilities(skeleton: Skeleton) {
        skeleton.target = null
        skeleton.isAware = false
        skeleton.equipment.setItemInMainHand(null)
        skeleton.equipment.setItemInOffHand(null)
    }

    private fun disableStrayAbilities(stray: Stray) {
        stray.target = null
        stray.isAware = false
        stray.equipment.setItemInMainHand(null)
        stray.equipment.setItemInOffHand(null)
    }

    private fun disableWitherSkeletonAbilities(witherSkeleton: WitherSkeleton) {
        witherSkeleton.target = null
        witherSkeleton.isAware = false
        witherSkeleton.equipment.setItemInMainHand(null)
        witherSkeleton.equipment.setItemInOffHand(null)
    }

    private fun disableDrownedAbilities(drowned: Drowned) {
        drowned.target = null
        drowned.isAware = false
        drowned.equipment.setItemInMainHand(null)
        drowned.equipment.setItemInOffHand(null)
    }

    private fun disablePiglinBruteAbilities(piglinBrute: PiglinBrute) {
        piglinBrute.target = null
        piglinBrute.isAware = false
        piglinBrute.equipment.setItemInMainHand(null)
        piglinBrute.equipment.setItemInOffHand(null)
        piglinBrute.isImmuneToZombification = true
    }

    private fun disablePiglinAbilities(piglin: Piglin) {
        piglin.target = null
        piglin.isAware = false
        piglin.equipment.setItemInMainHand(null)
        piglin.equipment.setItemInOffHand(null)
        piglin.isImmuneToZombification = true
    }

    private fun disableHoglinAbilities(hoglin: Hoglin) {
        hoglin.target = null
        hoglin.isAware = false
        hoglin.isImmuneToZombification = true
    }

    private fun disableZoglinAbilities(zoglin: Zoglin) {
        zoglin.target = null
        zoglin.isAware = false
    }

    private fun disableBeeAbilities(bee: Bee) {
        bee.target = null
        bee.isAware = false
        bee.anger = 0
        bee.cannotEnterHiveTicks = Int.MAX_VALUE
    }

    private fun disableSpiderAbilities(spider: Spider) {
        spider.target = null
        spider.isAware = false
    }

    private fun disableSlimeAbilities(slime: Slime) {
        slime.target = null
        slime.isAware = false
        slime.size = 1 // Prevent splitting
    }

    private fun disableGolemAbilities(golem: Golem) {
        golem.isAware = false
        if (golem is IronGolem) {
            golem.target = null
        } else if (golem is Snowman) {
            golem.isDerp = true
        }
    }

    private fun disableRavagerAbilities(ravager: Ravager) {
        ravager.target = null
        ravager.isAware = false
        ravager.attackTicks = 0
    }

    private fun disableLlamaAbilities(llama: Llama) {
        llama.target = null
        llama.isAware = false
    }
}
