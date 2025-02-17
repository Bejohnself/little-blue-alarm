package com.example.tryalarm

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class FunctionalModule(context: Context) {
    private var vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    // 振动参数配置
    private val vibrationDuration = 300L  // 单次振动时长（毫秒）
    private val intervalDuration = 100L   // 振动间隔时长（毫秒）

    // 创建振动模式数组
    val vibrationPattern = longArrayOf(
        0,
        vibrationDuration, intervalDuration,
        vibrationDuration, intervalDuration,
        vibrationDuration, intervalDuration,
        vibrationDuration, intervalDuration,
        vibrationDuration, intervalDuration
    )

    fun shake() {
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                vibrationPattern,
                0  // 不重复模式
            )
        )
    }

    fun cancelShaking() {
        vibrator.cancel()
    }


}