package com.example.tryalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.tryalarm.ui.layout.AlarmApp
import com.example.tryalarm.ui.theme.TryAlarmTheme


class MainActivity : ComponentActivity() {
    private val alarmViewModel by lazy {
        if (application is MyApplication) {
            (application as MyApplication).alarmViewModel
        } else {
            throw IllegalStateException("Application is not an instance of MyApplication")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmViewModel.createNotificationChannel(this)

        setContent {
            TryAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { AlarmApp(alarmViewModel) }
            }
        }
    }
}


