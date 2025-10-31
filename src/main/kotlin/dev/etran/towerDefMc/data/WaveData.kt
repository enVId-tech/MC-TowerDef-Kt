package dev.etran.towerDefMc.data

data class WaveData(
    val name: String,
    val sequence: List<WaveCommand>,
    val minTime: Double = 0.0,
    val maxTime: Double = 60.0,
    val waveHealth: Double? = null,
    val cashGiven: Int = 0
)