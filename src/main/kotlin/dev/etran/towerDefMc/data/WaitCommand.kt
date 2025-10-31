package dev.etran.towerDefMc.data

data class WaitCommand(
    val waitSeconds: Double // Time to wait after the previous command completes (or starts)
) : WaveCommand
