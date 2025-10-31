package dev.etran.towerDefMc.utils

import dev.etran.towerDefMc.TowerDefMC
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.TextDisplay
import org.bukkit.persistence.PersistentDataType
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Entity

fun getHealthBarComponent(currentHealth: Double, maxHealth: Double): Component {
    // --- Graphical Bar Logic ---
    val percentage = currentHealth / maxHealth
    val totalSegments = 10

    // Ensure current health is non-negative for display purposes
    val displayHealth = maxOf(0.0, currentHealth)

    // Calculate segments for the graphical bar
    val filledSegments = (percentage * totalSegments).toInt()
    val filledCount = maxOf(0, filledSegments)
    val emptyCount = maxOf(0, totalSegments - filledCount)

    val filled = "█"
    val empty = "█" // Using the same block character for empty segments, differentiated by color

    // Determine color based on health percentage
    val healthColor: TextColor = when {
        percentage > 0.6 -> TextColor.color(0x55FF55) // Green
        percentage > 0.3 -> TextColor.color(0xFFFF55) // Yellow
        else -> TextColor.color(0xFF5555) // Red
    }

    val emptyColor = TextColor.color(0xAAAAAA) // Gray for empty segments

    // Format the numerical health to one decimal place
    val numericalHealth = String.format("%.1f", displayHealth)
    val numericalMaxHealth = String.format("%.1f", maxHealth)

    val graphicalBar = Component.text().decorate(TextDecoration.BOLD).append(
        Component.text(filled.repeat(filledCount)).color(healthColor)
    ).append(
        Component.text(empty.repeat(emptyCount)).color(emptyColor)
    ).append(Component.space()) // Add space between bar and numbers
        .build()

    val numericalDisplay = Component.text().append(
        Component.text(numericalHealth).color(healthColor) // Current health uses the same color as the bar
    ).append(
        Component.text("/").color(TextColor.color(0xFFFFFF)) // White slash
    ).append(
        Component.text(numericalMaxHealth).color(TextColor.color(0xFFFFFF)) // White max health
    ).build()

    return Component.text().append(graphicalBar).append(numericalDisplay).build()
}

fun updateHealthBar(enemy: LivingEntity, healthBar: TextDisplay, calculatedHealth: Double) {
    val maxHealth = enemy.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0

    val newComponent = getHealthBarComponent(calculatedHealth, maxHealth)

    healthBar.text(newComponent)
}

fun createHealthBar(enemy: LivingEntity): TextDisplay {
    val offset = 0.5 // Blocks above the enemy's head (adjustable)
    val targetLocation = enemy.location.add(0.0, enemy.height + offset, 0.0)

    val textDisplay = enemy.world.spawn(targetLocation, TextDisplay::class.java)

    // Configuration
    textDisplay.billboard = Billboard.CENTER
    textDisplay.isShadowed = true
    textDisplay.isDefaultBackground = false
    textDisplay.setGravity(false)

    textDisplay.persistentDataContainer.set(
        TowerDefMC.HEALTH_OWNER_UUID, PersistentDataType.STRING, enemy.uniqueId.toString()
    )

    enemy.addPassenger(textDisplay)

    updateHealthBar(enemy, textDisplay, enemy.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0)

    return textDisplay
}

fun cleanUpEnemyHealthBar(enemy: Entity) {
    val deadMobUUID = enemy.uniqueId.toString()

    // 1. Search near the enemy's location for the TextDisplay owner link
    enemy.getNearbyEntities(2.0, 2.0, 2.0).filterIsInstance<TextDisplay>().forEach { textDisplay ->
        if (textDisplay.persistentDataContainer.get(
                TowerDefMC.HEALTH_OWNER_UUID, PersistentDataType.STRING
            ) == deadMobUUID
        ) {
            textDisplay.remove()
            return@forEach
        }
    }

    enemy.passengers.forEach { passenger ->
        if (passenger is TextDisplay) {
            passenger.remove()
        }
    }
}