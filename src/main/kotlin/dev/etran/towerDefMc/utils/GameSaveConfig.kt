package dev.etran.towerDefMc.utils

data class GameSaveConfig(
    val maxHealth: Int,
    val defaultCash: Int,
    val name: String,
    val waves: List<Map<String, Any>>,
    val allowedTowers: List<String>
)