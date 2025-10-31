package dev.etran.towerDefMc.data

data class GameSaveConfig(
    var maxHealth: Int,
    var defaultCash: Int,
    var name: String,
    var waves: List<WaveData>,
    var allowedTowers: List<String>
)