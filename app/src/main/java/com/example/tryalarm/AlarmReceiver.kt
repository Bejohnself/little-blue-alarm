package com.example.tryalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmViewModel = (context.applicationContext as MyApplication).alarmViewModel
        alarmViewModel.showNotification(context)
//        Toast.makeText(context, alarmViewModel.tip,Toast.LENGTH_SHORT).show()
    }
}