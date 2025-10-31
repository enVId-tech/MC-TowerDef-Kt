package dev.etran.towerDefMc.factories

import dev.etran.towerDefMc.data.EnemySpawnCommand
import dev.etran.towerDefMc.data.WaitCommand
import dev.etran.towerDefMc.data.WaveCommand
import dev.etran.towerDefMc.data.WaveData
import dev.etran.towerDefMc.registries.EnemyRegistry
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object WaveFactory {
    /**
     * Generate a wave with reasonable defaults based on the wave number
     * @param waveNumber The wave number (1-indexed)
     * @return A WaveData object with auto-generated content
     */
    fun generateWave(waveNumber: Int): WaveData {
        val sequence = mutableListOf<WaveCommand>()
        val availableEnemies = EnemyRegistry.getAllEnemies().map { it.id }

        // If no enemies are registered, create a simple wave
        if (availableEnemies.isEmpty()) {
            sequence.add(WaitCommand(2.0))
            sequence.add(EnemySpawnCommand(mapOf("Basic_Enemy_1" to 5), 1.0))

            return WaveData(
                name = "Wave $waveNumber",
                sequence = sequence,
                minTime = 10.0,
                maxTime = 60.0,
                waveHealth = null,
                cashGiven = 50 + (waveNumber * 10)
            )
        }

        // Determine wave complexity based on wave number
        val numSpawnPhases = when {
            waveNumber <= 3 -> 1
            waveNumber <= 10 -> 2
            waveNumber <= 20 -> 3
            else -> min(4, 2 + (waveNumber / 10))
        }

        // Generate spawn phases
        for (phase in 1..numSpawnPhases) {
            // Add wait between phases (except for first phase)
            if (phase > 1) {
                val waitTime = 3.0 + (waveNumber * 0.5)
                sequence.add(WaitCommand(min(waitTime, 10.0)))
            }

            // Select enemies for this phase
            val enemiesForPhase = selectEnemiesForPhase(waveNumber, phase, numSpawnPhases, availableEnemies)
            val spawnInterval = calculateSpawnInterval(waveNumber, phase)

            sequence.add(EnemySpawnCommand(enemiesForPhase, spawnInterval))
        }

        // Calculate wave parameters
        val minTime = 5.0 + (numSpawnPhases * 3.0)
        val totalEnemies = sequence.filterIsInstance<EnemySpawnCommand>()
            .sumOf { it.enemies.values.sum() }
        val maxTime = minTime + (totalEnemies * 2.0) + 20.0

        // Cash reward increases with wave difficulty
        val cashGiven = 50 + (waveNumber * 15) + (totalEnemies * 2)

        // Boss waves (every 10 waves) get special modifiers
        val waveName = if (waveNumber % 10 == 0) {
            "Boss Wave $waveNumber"
        } else {
            "Wave $waveNumber"
        }

        return WaveData(
            name = waveName,
            sequence = sequence,
            minTime = minTime,
            maxTime = maxTime,
            waveHealth = null, // Use default health
            cashGiven = cashGiven
        )
    }

    /**
     * Select enemies for a specific phase of a wave
     */
    private fun selectEnemiesForPhase(
        waveNumber: Int,
        phase: Int,
        totalPhases: Int,
        availableEnemies: List<String>
    ): Map<String, Int> {
        val enemyMap = mutableMapOf<String, Int>()

        // Early waves: only basic enemies
        if (waveNumber <= 3) {
            val basicEnemy = availableEnemies.firstOrNull() ?: "Basic_Enemy_1"
            val count = 5 + (waveNumber * 2)
            enemyMap[basicEnemy] = count
            return enemyMap
        }

        // Calculate base enemy count with exponential scaling
        val baseCount = (5 * (1.2.pow(waveNumber - 1))).toInt()
        val phaseMultiplier = when {
            phase == totalPhases -> 1.5 // Last phase has more enemies
            phase == 1 -> 0.8 // First phase has fewer enemies
            else -> 1.0
        }

        val enemyCount = max(3, (baseCount * phaseMultiplier / totalPhases).toInt())

        // Distribute enemies based on wave number
        when {
            waveNumber <= 5 -> {
                // Waves 1-5: Mostly basic, introduce one other type
                val basicEnemy = availableEnemies.firstOrNull() ?: "Basic_Enemy_1"
                enemyMap[basicEnemy] = (enemyCount * 0.8).toInt()

                if (availableEnemies.size > 1) {
                    val secondEnemy = availableEnemies[1]
                    enemyMap[secondEnemy] = max(1, (enemyCount * 0.2).toInt())
                }
            }
            waveNumber <= 10 -> {
                // Waves 6-10: Mix of basic and advanced
                val numTypes = min(2, availableEnemies.size)
                val enemiesPerType = enemyCount / numTypes

                for (i in 0 until numTypes) {
                    val enemy = availableEnemies[i % availableEnemies.size]
                    enemyMap[enemy] = max(1, enemiesPerType)
                }
            }
            waveNumber % 10 == 0 -> {
                // Boss waves: Include all enemy types with emphasis on tough ones
                if (availableEnemies.size >= 3) {
                    // Add tank/boss enemies
                    val bossEnemy = availableEnemies.getOrNull(2) ?: availableEnemies.last()
                    enemyMap[bossEnemy] = max(2, enemyCount / 4)

                    // Add support enemies
                    val supportCount = enemyCount - enemyMap.values.sum()
                    availableEnemies.take(2).forEach { enemy ->
                        enemyMap[enemy] = max(1, supportCount / 2)
                    }
                } else {
                    // Fallback: lots of basic enemies
                    availableEnemies.forEach { enemy ->
                        enemyMap[enemy] = max(1, enemyCount / availableEnemies.size)
                    }
                }
            }
            else -> {
                // Regular advanced waves: Random variety
                val numTypes = min(3, availableEnemies.size)
                val enemiesPerType = enemyCount / numTypes

                // Use different enemies based on phase
                val startIndex = (phase - 1) % availableEnemies.size
                for (i in 0 until numTypes) {
                    val enemy = availableEnemies[(startIndex + i) % availableEnemies.size]
                    enemyMap[enemy] = max(1, enemiesPerType)
                }
            }
        }

        // Ensure we have at least some enemies
        if (enemyMap.isEmpty()) {
            enemyMap[availableEnemies.first()] = 5
        }

        return enemyMap
    }

    /**
     * Calculate spawn interval based on wave difficulty
     */
    private fun calculateSpawnInterval(waveNumber: Int, @Suppress("UNUSED_PARAMETER") phase: Int): Double {
        // Earlier waves have slower spawn rates
        val baseInterval = when {
            waveNumber <= 5 -> 1.5
            waveNumber <= 10 -> 1.0
            waveNumber <= 20 -> 0.7
            else -> 0.5
        }

        // Boss waves have faster spawns
        return if (waveNumber % 10 == 0) {
            max(0.3, baseInterval * 0.7)
        } else {
            baseInterval
        }
    }

    /**
     * Generate multiple waves at once
     * @param startWave Starting wave number
     * @param count Number of waves to generate
     * @return List of generated WaveData
     */
    fun generateWaves(startWave: Int, count: Int): List<WaveData> {
        return (startWave until startWave + count).map { generateWave(it) }
    }
}