package dev.etran.towerDefMc.data

data class EnemySpawnCommand(
    val enemies: Map<String, Int>, val intervalSeconds: Double
) : WaveCommand