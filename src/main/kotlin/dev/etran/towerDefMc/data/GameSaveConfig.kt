package dev.etran.towerDefMc.data

data class GameSaveConfig(
    val maxHealth: Int,
    val defaultCash: Int,
    val name: String,
    val waves: List<Wave>,
    val allowedTowers: List<String>
)