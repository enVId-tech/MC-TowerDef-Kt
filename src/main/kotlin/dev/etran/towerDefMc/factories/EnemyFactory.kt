package dev.etran.towerDefMc.factories

import de.tr7zw.nbtapi.NBT
import dev.etran.towerDefMc.TowerDefMC
import dev.etran.towerDefMc.utils.findCheckpointById
import net.kyori.adventure.util.TriState
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object EnemyFactory {
    fun newBasicEnemy(amount: Int = 1): ItemStack {
        val enemySpawn = ItemStack(Material.REDSTONE_BLOCK, amount)

        // Add identifier for the end rod to make sure it is a spawn object and not just an end rod
        NBT.modify(enemySpawn) { nbt ->
            nbt.setString("tdef_item_name", "Enemy 1")
        }

        return enemySpawn
    }

    fun enemyPlace(event: PlayerInteractEvent) {
        event.isCancelled = true

        val block = event.clickedBlock ?: return
        val location = block.location.add(0.5, 1.0, 0.5)
        val player = event.player

        val world = location.world
        val entity = world.spawnEntity(location, EntityType.ZOMBIE)

        // Add NBT data if only the zombie exists after calling spawn
        if (entity !is LivingEntity) return

        entity.setAI(false)
        entity.isInvulnerable = false
        entity.fireTicks = 0
        entity.visualFire = TriState.TRUE
        entity.isPersistent = true
        entity.isSilent = true
        entity.persistentDataContainer.set(TowerDefMC.ELEMENT_TYPES, PersistentDataType.STRING, "Enemy")
        entity.persistentDataContainer.set(TowerDefMC.ENEMY_KEY, PersistentDataType.STRING, "Basic_Enemy_1")
        entity.persistentDataContainer.set(TowerDefMC.TARGET_CHECKPOINT_ID, PersistentDataType.INTEGER, 1)

        // Take away 1 from the user if they aren't in creative mode
        if (player.gameMode != GameMode.CREATIVE) {
            event.player.inventory.itemInMainHand.amount -= 1
        }
    }
}