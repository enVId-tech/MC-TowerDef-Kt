package dev.etran.towerDefMc.factories


import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.iface.ReadWriteNBT

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

object TowerFactory {
    fun newZombieEgg(amount: Int = 1): ItemStack {
        val egg = ItemStack(Material.ZOMBIE_SPAWN_EGG, amount)

        NBT.modifyComponents(egg) { componentNBT ->
            val customData = componentNBT.getOrCreateCompound("minecraft:custom_data")

            val entityTag = customData.getOrCreateCompound("EntityTag")
            entityTag.setByte("NoAI", 1.toByte())
            entityTag.setString("id", "minecraft:zombie")

             entityTag.setString("CustomName", "{\"text\":\"Tower Zombie\"}")
        }

        return egg
    }
}