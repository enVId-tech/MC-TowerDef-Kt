package dev.etran.towerDefMc.utils

import org.bukkit.Location
import org.bukkit.entity.Mob

/**
 * @param mob Mob should be the entity that you are applying the pathfinder to (enemies)
 * @param targetLocation Target locations should usually be armor stands
 * @param speed Speed is a multiplier, 1.0 being default, of the mob's speed to the targetLocation
 * @param stopRadius Distance at which pathfinding completes
 * @return Boolean Did mob successfully pathfind to said target location?
 */
fun setMobTargetLocation(mob: Mob, targetLocation: Location, speed: Double = 1.0, stopRadius: Double = 0.5): Boolean {
    val pathfinder = mob.pathfinder
    val distanceSquared = targetLocation.distanceSquared(targetLocation)
    val stopRadiusSquared = stopRadius * stopRadius
//    if (distanceSquared <= stopRadiusSquared) {
//        // Stop pathfinding
//        mob.pathfinder.stopPathfinding()
//
//        mob.setAI(false)
//        return true
//    } else {
        mob.setAI(true)
        val success = pathfinder.moveTo(targetLocation, speed)
        return success
//    }
}