package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import org.bukkit.persistence.PersistentDataType

fun getRenamableItemValue(menu: CustomMenu, slot: Int): String? {
    val item = menu.inventory.getItem(slot)

    if (item == null) {
        return null
    }

    val meta = item.itemMeta ?: return null
    val pdc = meta.persistentDataContainer

    // Attempt to retrieve the value from the universal PDC key
    val savedValue = pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)

    return savedValue
}

fun getRenamableItemIntValue(menu: CustomMenu, slot: Int): Int? {
    val item = menu.inventory.getItem(slot)

    if (item == null) {
        return null
    }

    val meta = item.itemMeta ?: return null
    val pdc = meta.persistentDataContainer

    // 1. Retrieve the saved string
    val savedValueString = pdc.get(TowerDefMC.TITLE_KEY, PersistentDataType.STRING)

    // 2. Attempt safe conversion to Int
    return savedValueString?.toIntOrNull()
}