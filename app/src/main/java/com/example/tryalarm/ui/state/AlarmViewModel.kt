package com.example.tryalarm.ui.state

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import com.example.tryalarm.AlarmDismissReceiver
import com.example.tryalarm.AlarmReceiver
import com.example.tryalarm.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

val alarmViewModel = AlarmViewModel

object AlarmViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()
    var alarmOn by mutableStateOf(false)
    var pendingGapTime by mutableStateOf("")
    var isValidInput by mutableStateOf(true)
    var tip by mutableStateOf("")
    lateinit var alarmManager: AlarmManager
    var expanded by mutableStateOf(false)

    fun initAlarmManager(context: Context) {
        alarmManager = context.getSystemService<AlarmManager>()!!
    }

    fun onGapTimeChanged(updateGapTime: String) {
        pendingGapTime = updateGapTime
    }

    fun onExpandClick(){
        expanded = !expanded
    }

    private fun getAlarmIntent(context: Context): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    // 设置闹钟的具体逻辑
    fun setAlarm(alarmManager: AlarmManager, context: Context) {
//        val triggerTime = (System.currentTimeMillis() + minutes * 60 * 1000).toLong()
        val intent = getAlarmIntent(context)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            _uiState.value.triggerTime,
            intent
        )
    }

    // 检查输入是否可以转化成小数且大于0
    private fun checkValidInput(): Boolean {
        return if (pendingGapTime.toDoubleOrNull() != null) {
            if (pendingGapTime.toDouble() > 0) {
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun onSetAlarmClick(
        context: Context,
        alarmManager: AlarmManager,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
        notificationPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        if (!checkValidInput()) {             // 输入错误
            isValidInput = false
        } else {
            isValidInput = true
            _uiState.update { currentState ->
                currentState.copy(
                    gapTime = pendingGapTime.toDouble()
                )
            }
            if (!hasNotificationsPermission(context)) {      // 没有通知权限
                Toast.makeText(
                    context,
                    context.getString(R.string.no_notifiication_authority_inform),
                    Toast.LENGTH_SHORT
                ).show()
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
//                Toast.makeText(context, "已有通知权限", Toast.LENGTH_SHORT).show()
                if (hasExactAlarmPermission(context)) {
                    _uiState.update {
                        it.copy(
                            triggerTime = System.currentTimeMillis() + (_uiState.value.gapTime * 60_000L).toLong()
                        )
                    }
                    setAlarm(alarmManager, context)
                    alarmOn = true
                } else {
                    Toast.makeText(context,
                        context.getString(R.string.can_not_set_exact_alarm), Toast.LENGTH_SHORT)
                        .show()
                    permissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM)
                }
            }
        }
    }

    fun onCancelAlarmClick(context: Context) {
        alarmManager.cancel(getAlarmIntent(context))
        alarmOn = false
        isValidInput = true
        pendingGapTime = ""
        _uiState.update {
            it.copy(
                leftTime = 0L,
                triggerTime = 0L,
                gapTime = 1.0
            )
        }
//        Toast.makeText(context, "已取消当前周期闹钟", Toast.LENGTH_SHORT).show()
    }

    fun onTipChange(tip: String) {
        this.tip = tip
    }

    fun updateLeftTime() {
        if (alarmOn) {
            _uiState.update { currentState ->
                currentState.copy(
                    leftTime = _uiState.value.triggerTime - System.currentTimeMillis()
                )
            }
        }
    }

    fun resetAlarm(alarmManager: AlarmManager, context: Context) {
        val currentTime = System.currentTimeMillis()
        _uiState.update { currentState ->
            currentState.copy(
                triggerTime = currentTime + (_uiState.value.gapTime * 60_000L).toLong(),
                leftTime = (_uiState.value.gapTime * 60_000L).toLong()
            )
        }
        setAlarm(alarmManager, context)
    }

    fun showNotification(context: Context) {
        // 创建关闭意图
        // 使用明确的广播 Intent
        val dismissIntent = Intent(context, AlarmDismissReceiver::class.java).apply {
            action = "ACTION_DISMISS_ALARM" // 添加唯一 action
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(tip)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true) // 持续通知
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 允许锁屏界面显示
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 关键分类标识
//            .setFullScreenIntent(pendingIntent, true) // 提升锁屏优先级
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel, // 关闭图标
                context.getString(R.string.close_button), // 按钮文字
                dismissPendingIntent
            )
            .build()

        // 显示通知（添加权限检查）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context,
                context.getString(R.string.need_notification_authority), Toast.LENGTH_SHORT).show()
        } else {
            NotificationManagerCompat.from(context).notify(1, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            "alarm_channel",
            "闹钟通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notifications_for_precise_alarm_triggering)
//            this.vibrationPattern = vibrationPattern
            enableVibration(true)
//            setSound(null, null) // 关闭声音（可选）
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC // 明确锁屏可见性
            setAllowBubbles(true) // 允许交互式内容
        }

        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    // 检查是否已有权限
    private fun hasExactAlarmPermission(context: Context): Boolean {
        val alarmManager = context.getSystemService<AlarmManager>()!!
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Android 12 及以下版本自动获得权限
        }
    }

    private fun hasNotificationsPermission(context: Context): Boolean {
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED)
    }
}
