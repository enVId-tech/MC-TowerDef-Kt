package dev.etran.towerDefMc.data

data class WaveData(
    val name: String,
    val sequence: List<WaveCommand>
)
interface WaveCommand {}

data class WaitCommand(
    val waitSeconds: Double // Time to wait after the previous command completes (or starts)
) : WaveCommand

data class EnemySpawnCommand(
    val enemies: Map<String, Int>,
    val intervalSeconds: Double
) : WaveCommand