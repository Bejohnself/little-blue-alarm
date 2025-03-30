package com.example.tryalarm.widget

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.tryalarm.MyApplication
import com.example.tryalarm.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        // 获取 ViewModel
        val context = LocalContext.current
        val alarmViewModel = (context.applicationContext as MyApplication).alarmViewModel

        // 观察剩余时间
        val leftTime by alarmViewModel.leftTime.collectAsState()

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(
                    day = Color.White.copy(alpha = 0.8f),
                    night = Color(0xFF050B13).copy(alpha = 0.8f)
                )
                .cornerRadius(30.dp)
                .padding(end = 8.dp, start = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(leftTime),
                style = TextStyle(
                    fontSize = 30.sp,
                    color = ColorProvider(
                        day = Color.Black,    // 日间模式文字颜色
                        night = Color.White   // 夜间模式文字颜色
                    )
                )
            )
            Spacer(
                modifier = GlanceModifier.size(150.dp)  // 添加一个弹性空间
            )
            if (!alarmViewModel.alarmOn) {
                Button(
                    text = "",
                    onClick = actionRunCallback<SetAlarmActionCallback>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(
                            day = Color(0xFF415F91),
                            night = Color(0xFFAAC7FF)
                        ), // 背景色
                        contentColor = ColorProvider(
                            day = Color(0xFFFFFFFF),
                            night = Color(0xFF0A305F)
                        )          // 文字颜色
                    ),
                    modifier = GlanceModifier.cornerRadius(90.dp).size(32.dp)
                )
            } else {
                Button(
                    text = "",
                    onClick = actionRunCallback<CancelAlarmActionCallback>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(
                            day = Color(0xFF860606),
                            night = Color(0xFFFFB4AB)
                        ), // 背景色
                        contentColor = ColorProvider(
                            day = Color(0xFFFFFFFF),
                            night = Color(0xFF0A305F)
                        )          // 文字颜色
                    ),
                    modifier = GlanceModifier.cornerRadius(90.dp).size(32.dp)
                )
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(timeInMillis: Long): String {
        val seconds = (timeInMillis / 1000) % 60
        val minutes = (timeInMillis / (1000 * 60))
        return if (minutes < 100) String.format("%02d : %02d", minutes, seconds)
        else String.format("%03d : %02d", minutes, seconds)
    }

    companion object {
        suspend fun updateWidget(context: Context) {
            // 更新所有widget的状态
            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(AlarmWidget::class.java)
            glanceIds.forEach { glanceId ->
                AlarmWidget().update(context, glanceId)
            }
        }
    }
}

class SetAlarmActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val alarmViewModel = (context.applicationContext as MyApplication).alarmViewModel
        alarmViewModel.initAlarmManager(context)

        withContext(Dispatchers.Main) {
            // 启动循环前先停止已有任务
            alarmViewModel.stopWidgetUpdateLoop()
            alarmViewModel.setAlarm(
                alarmManager = alarmViewModel.alarmManager,
                context = context
            )
            Toast.makeText(
                context,
                context.getString(
                    R.string.set_alarm_warning,
                    alarmViewModel.uiState.value.gapTime.toString()
                ), Toast.LENGTH_SHORT
            ).show()
            alarmViewModel.startWidgetUpdateLoop(context) // 启动新循环
            AlarmWidget.updateWidget(context)
        }
    }
}


class CancelAlarmActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val alarmViewModel = (context.applicationContext as MyApplication).alarmViewModel
        alarmViewModel.initAlarmManager(context)
        withContext(Dispatchers.Main) {
            alarmViewModel.widgetCancelAlarm(context)
        }
    }
}
