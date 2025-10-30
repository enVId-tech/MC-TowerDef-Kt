package dev.etran.towerDefMc.managers

class WaveManager {
    private var currentWave = 0
    private var enemiesRemaining = 0
    private var timeRemaining = 0

    fun checkWaveCompletion(): Boolean {
        return enemiesRemaining <= 0 || timeRemaining <= 0
    }

    fun startNextWave() {
        currentWave++
    }
}