package com.example.tryalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class AlarmDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        // 取消所有相关通知
        with(NotificationManagerCompat.from(context)) {
            cancel(1) // 前台服务通知
//            cancel(2) // 辅助通知
        }
    }
}