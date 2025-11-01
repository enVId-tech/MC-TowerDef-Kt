package dev.etran.towerDefMc.commands

import dev.etran.towerDefMc.TowerDefMC
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object GiveShopVillager : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be executed by players!")
            return false
        }

        // Create a villager spawn egg that will spawn a shop villager
        val shopEgg = ItemStack(Material.VILLAGER_SPAWN_EGG, 1)
        val meta = shopEgg.itemMeta

        meta.displayName(Component.text("§6§lTower Shop Villager"))
        meta.lore(
            listOf(
                Component.text("§7Place this villager to create"),
                Component.text("§7a tower shop for your game"),
                Component.text(""),
                Component.text("§eRight-click to place"),
                Component.text("§7Players can buy towers from this shop")
            )
        )

        // Mark it as a shop villager spawner
        meta.persistentDataContainer.set(
            TowerDefMC.GAME_ITEMS, PersistentDataType.STRING, "Tower_Shop_Spawner"
        )

        shopEgg.itemMeta = meta

        // Give to player
        val leftover = sender.inventory.addItem(shopEgg)
        if (leftover.isNotEmpty()) {
            sender.world.dropItemNaturally(sender.location, shopEgg)
        }

        sender.sendMessage("§a§lTower Shop Villager spawner given!")
        sender.sendMessage("§7Place it in your game area and configure it")

        return true
    }
}

