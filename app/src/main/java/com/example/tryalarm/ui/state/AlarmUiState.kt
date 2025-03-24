package com.example.tryalarm.ui.state

data class AlarmUiState(
    var gapTime: String? = "0",
    var triggerTime: Long = 0L,
    var tip: String? = ""
)