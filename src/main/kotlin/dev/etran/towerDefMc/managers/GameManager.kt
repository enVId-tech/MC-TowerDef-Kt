package dev.etran.towerDefMc.managers

import dev.etran.towerDefMc.data.GameSaveConfig
import dev.etran.towerDefMc.data.WaveData

class GameManager(
    private val gameId: Int,
    val gameConfig: GameSaveConfig,
    val waves: List<WaveData>
) {

}