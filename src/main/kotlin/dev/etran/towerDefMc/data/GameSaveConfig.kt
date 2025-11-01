package dev.etran.towerDefMc.data

data class GameSaveConfig(
    var maxHealth: Int,
    var defaultCash: Int,
    var name: String,
    var waves: List<WaveData>,
    var allowedTowers: List<String>,
    var paths: List<SerializablePathData> = emptyList(),
    var towerSellRefundPercentage: Int = 50 // Default 50% refund when selling towers
)