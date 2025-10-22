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
            componentNBT.setString("CustomTag", "Tower 1")
        }

        return egg
    }
}