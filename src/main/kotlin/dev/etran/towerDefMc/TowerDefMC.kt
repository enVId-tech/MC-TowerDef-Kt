package dev.etran.towerDefMc

import dev.etran.towerDefMc.commands.ClearCheckpoints
import dev.etran.towerDefMc.commands.ClearEnemies
import dev.etran.towerDefMc.commands.ClearTowers
import dev.etran.towerDefMc.commands.GiveEnemy
import dev.etran.towerDefMc.commands.GiveTower
import dev.etran.towerDefMc.commands.GiveStatsTracker
import dev.etran.towerDefMc.commands.ToggleStandVisibility
import dev.etran.towerDefMc.commands.GeneratorCommand
import dev.etran.towerDefMc.commands.GiveShopVillager
import dev.etran.towerDefMc.commands.menus.MenuCommands
import dev.etran.towerDefMc.factories.GameFactory
import dev.etran.towerDefMc.listeners.EnemyHealthListener
import dev.etran.towerDefMc.listeners.EnemyTargetListener
import dev.etran.towerDefMc.listeners.EntityDeathListener
import dev.etran.towerDefMc.listeners.FireproofListener
import dev.etran.towerDefMc.listeners.MenuListener
import dev.etran.towerDefMc.listeners.PathArmorStandRemovalListener
import dev.etran.towerDefMc.listeners.PathCreationListener
import dev.etran.towerDefMc.listeners.PathLocationListener
import dev.etran.towerDefMc.listeners.PathModificationListener
import dev.etran.towerDefMc.listeners.PlayerHoldListener
import dev.etran.towerDefMc.listeners.PlayerPlaceListener
import dev.etran.towerDefMc.listeners.SpawnModeListener
import dev.etran.towerDefMc.listeners.TowerUpgradeListener
import dev.etran.towerDefMc.listeners.TowerShopListener
import dev.etran.towerDefMc.listeners.ShopVillagerPlacementListener
import dev.etran.towerDefMc.managers.GameInstanceTracker
import dev.etran.towerDefMc.managers.GameManager
import dev.etran.towerDefMc.managers.PlayerHUDManager
import dev.etran.towerDefMc.managers.WaypointManager
import dev.etran.towerDefMc.managers.WaveManager
import dev.etran.towerDefMc.registries.EnemyRegistry
import dev.etran.towerDefMc.registries.GameRegistry
import dev.etran.towerDefMc.registries.TowerRegistry
import dev.etran.towerDefMc.schedulers.EnemyScheduler
import dev.etran.towerDefMc.schedulers.TowerScheduler
import dev.etran.towerDefMc.utils.TaskUtility
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class TowerDefMC : JavaPlugin() {
    /* TODO: Set up managers such that managers are
        * ready to be separate instances for each
        * game, and not singleton objects for the
        * entire Minecraft server
        */
    companion object {
        // Set by onEnable, clear in onDisable
        lateinit var instance: TowerDefMC
            private set

        // Access the key via the current instance
        // Major game objects
        val ELEMENT_TYPES: NamespacedKey
            get() = NamespacedKey(instance, "elementTypes")
        val TOWER_TYPES: NamespacedKey
            get() = NamespacedKey(instance, "towerKey")
        val ENEMY_TYPES: NamespacedKey
            get() = NamespacedKey(instance, "enemyKey")
        val GAME_ITEMS: NamespacedKey
            get() = NamespacedKey(instance, "towerItems")

        // Per-object properties
        // -- Checkpoints --
        val CHECKPOINT_ID: NamespacedKey
            get() = NamespacedKey(instance, "checkpointId")
        val TARGET_CHECKPOINT_ID: NamespacedKey
            get() = NamespacedKey(instance, "targetCheckpointId")
        val STARTPOINT_ID: NamespacedKey
            get() = NamespacedKey(instance, "startpointId")

        // -- Towers --
        // Attack wait time is used in seconds, converted to ms
        val TOWER_RANGE: NamespacedKey
            get() = NamespacedKey(instance, "towerRange")
        val READY_TIME: NamespacedKey
            get() = NamespacedKey(instance, "towerReadyTime")
        val ATTACK_WAIT_TIME: NamespacedKey
            get() = NamespacedKey(instance, "attackWaitTime")
        val TOWER_DMG: NamespacedKey
            get() = NamespacedKey(instance, "towerDMG")

        // -- Enemies --
        val HEALTH_OWNER_UUID: NamespacedKey
            get() = NamespacedKey(instance, "owner_uuid")

        // -- Misc --
        val TITLE_KEY: NamespacedKey
            get() = NamespacedKey(instance, "titleVal")
        val RENAMABLE_KEY: NamespacedKey
            get() = NamespacedKey(instance, "isRenamableAttribute")
        val LORE_TEMPLATE_KEY: NamespacedKey
            get() = NamespacedKey(instance, "loreTemplate")
        val MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

        const val RENAMABLE_MARKER_VALUE = "ITEM_IS_RENAMABLE"
        const val CHECK_INTERVAL_TICKS: Long = 4L

        // Helper to create custom NamespacedKeys
        fun createKey(key: String): NamespacedKey {
            return NamespacedKey(instance, key)
        }

        // Game ID key for tracking which game entities belong to
        val GAME_ID_KEY: NamespacedKey
            get() = NamespacedKey(instance, "gameId")
    }

    override fun onEnable() {
        instance = this

        // Plugin startup logic
        logger.info {
            "Tower Defense Plugin - Starting Initialization"
        }

        // Register utils
        TaskUtility.initialize(this)
        WaypointManager.initialize(this)
        MenuListener.initialize(this)
        GameFactory.initialize(this)
        GameRegistry.initialize(this)
        GameManager.initialize(this)
        WaveManager.initialize(this)
        EnemyRegistry.initialize(this)
        TowerRegistry.initialize(this)
        GameInstanceTracker.initialize(this)

        // Load saved games from files
        GameRegistry.loadAllSavedGames()

        logger.info {
            "Tower Defense Plugin - Primary Functions Initialized"
        }

        // Register continuous events
        server.pluginManager.registerEvents(PlayerPlaceListener, this)
        server.pluginManager.registerEvents(EntityDeathListener, this)
        server.pluginManager.registerEvents(FireproofListener, this)
        server.pluginManager.registerEvents(PlayerHoldListener, this)
        server.pluginManager.registerEvents(EnemyHealthListener, this)
        server.pluginManager.registerEvents(EnemyTargetListener(), this)
        server.pluginManager.registerEvents(MenuListener, this)
        server.pluginManager.registerEvents(SpawnModeListener(), this)
        server.pluginManager.registerEvents(PathLocationListener(), this)
        server.pluginManager.registerEvents(PathCreationListener(), this)
        server.pluginManager.registerEvents(PathModificationListener(), this)
        server.pluginManager.registerEvents(PathArmorStandRemovalListener(), this)
        server.pluginManager.registerEvents(TowerUpgradeListener(), this)
        server.pluginManager.registerEvents(dev.etran.towerDefMc.listeners.GameStatsDisplayListener(), this)
        server.pluginManager.registerEvents(TowerShopListener(), this)
        server.pluginManager.registerEvents(ShopVillagerPlacementListener(), this)

        logger.info {
            "Tower Defense Plugin - Continuous Listeners Registered"
        }

        // Set commands and behaviors
        getCommand("giveTDtower")?.setExecutor(GiveTower)
        getCommand("giveTDenemy")?.setExecutor(GiveEnemy)
        getCommand("clearTDallwaypoints")?.setExecutor(ClearCheckpoints)
        getCommand("clearTDalltowers")?.setExecutor(ClearTowers)
        getCommand("clearTDallenemies")?.setExecutor(ClearEnemies)
        getCommand("toggleStandVisibility")?.setExecutor(ToggleStandVisibility)
        getCommand("tdmenu")?.setExecutor(MenuCommands)
        getCommand("giveStatsTracker")?.setExecutor(GiveStatsTracker)

        // Generator commands
        getCommand("tdgenerator")?.setExecutor(GeneratorCommand)
        getCommand("tdgenerator")?.tabCompleter = GeneratorCommand
        getCommand("giveShopVillager")?.setExecutor(GiveShopVillager)

        // New admin game control commands
        getCommand("stopgame")?.setExecutor(dev.etran.towerDefMc.commands.StopGame)
        getCommand("givecash")?.setExecutor(dev.etran.towerDefMc.commands.GiveCash)
        getCommand("spawnenemy")?.setExecutor(dev.etran.towerDefMc.commands.SpawnEnemy)
        getCommand("nextwave")?.setExecutor(dev.etran.towerDefMc.commands.NextWave)

        logger.info {
            "Tower Defense Plugin - Game Commands Verified & Set up"
        }

        // Scheduler tasks
        startTowerCheckTask()
        startEnemyCheckTask()
        startHealthBarCleanupTask()

        // Start player HUD updates
        PlayerHUDManager.startHUDTask()

        logger.info {
            "Tower Defense Plugin - Scheduler Tasks Started"
        }

        logger.info {
            "Tower Defense Plugin - Initialization Process Complete!"
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic

        // Stop HUD updates
        PlayerHUDManager.stopHUDTask()

        logger.info {
            "Tower Defense Plugin - File configuration saved"
        }

        logger.info {
            "Tower Defense Plugin - Shut down all tasks"
        }
    }

    fun startTowerCheckTask() {
        // Makes a task every 5 game ticks to update object behavior based on new data
        Bukkit.getScheduler().runTaskTimer(
            this, Runnable {
                for (world in Bukkit.getWorlds()) {
                    TowerScheduler.checkAndHandleTowers(world)
                }
            },
            0L,
            CHECK_INTERVAL_TICKS
        )
    }

    fun startEnemyCheckTask() {
        // Makes a task every 5 game ticks to update object behavior based on new data
        Bukkit.getScheduler().runTaskTimer(
            this, Runnable {
                for (world in Bukkit.getWorlds()) {
                    EnemyScheduler.checkAndHandleEnemies(world)
                }
            },
            0L,
            CHECK_INTERVAL_TICKS
        )
    }

    fun startHealthBarCleanupTask() {
        // Cleanup orphaned health bars every 5 seconds (100 ticks)
        Bukkit.getScheduler().runTaskTimer(
            this, Runnable {
                for (world in Bukkit.getWorlds()) {
                    // Find all TextDisplay entities with health bar markers
                    world.entities.filterIsInstance<org.bukkit.entity.TextDisplay>().forEach { textDisplay ->
                        val ownerUUID = textDisplay.persistentDataContainer.get(
                            HEALTH_OWNER_UUID, org.bukkit.persistence.PersistentDataType.STRING
                        )

                        if (ownerUUID != null) {
                            // Check if the owner entity still exists
                            val ownerExists = world.entities.any { it.uniqueId.toString() == ownerUUID }

                            // If owner doesn't exist, remove this orphaned health bar
                            if (!ownerExists) {
                                textDisplay.remove()
                            }
                        }
                    }
                }
            },
            100L, // Start after 5 seconds
            100L  // Run every 5 seconds
        )
    }
}
