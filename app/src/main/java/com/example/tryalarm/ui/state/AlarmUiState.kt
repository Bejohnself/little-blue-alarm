package com.example.tryalarm.ui.state

data class AlarmUiState(
    var leftTime: Long = 0L,
    var triggerTime: Long = 0L,
    var gapTime: Double = 1.0,
)