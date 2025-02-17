package com.example.tryalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tryalarm.ui.state.alarmViewModel


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        alarmViewModel.showNotification(context)
//        Toast.makeText(context, alarmViewModel.tip,Toast.LENGTH_SHORT).show()
    }
}