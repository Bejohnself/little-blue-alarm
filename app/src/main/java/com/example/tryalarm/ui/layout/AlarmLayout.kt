package com.example.tryalarm.ui.layout

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryalarm.R
import com.example.tryalarm.ui.state.AlarmViewModel
import com.example.tryalarm.ui.theme.TryAlarmTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("NewApi")
@Composable
fun AlarmApp() {
    Scaffold(
        topBar = {
            // Material 3 风格示例
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.alarm),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(40.dp, 40.dp)
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
//                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.align(Alignment.CenterVertically),
                            fontFamily = FontFamily(Font(resId = R.font.title))
                        )
                    }
                }
            )
        },

        content = { innerPadding ->
            // 这里放置你的主要内容
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                AlarmScreen()
            }
        }
    )
}

@Preview
@Composable
fun AlarmAppPreview() {
    TryAlarmTheme {
        AlarmApp()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AlarmScreen(
    alarmViewModel: AlarmViewModel = viewModel<AlarmViewModel>(),
) {
    val alarmUiState by alarmViewModel.uiState.collectAsState()
    val context = LocalContext.current
    alarmViewModel.initAlarmManager(context)

    // 权限请求 Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            alarmViewModel.setAlarm(alarmViewModel.alarmManager, context)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.authority_request),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 在 Composable 中添加权限请求
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 处理结果 */ }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = alarmViewModel.tip,
            singleLine = true,
            onValueChange = { alarmViewModel.onTipChange(it) },
            label = {
                Text(stringResource(R.string.tip_input_tag))
            }
        )
        Spacer(modifier = Modifier.padding(16.dp))
        OutlinedTextField(
            value = alarmViewModel.pendingGapTime,
            singleLine = true,
            onValueChange = { alarmViewModel.onGapTimeChanged(it) },
            isError = !alarmViewModel.isValidInput,
            label = {
                if (alarmViewModel.isValidInput) Text(stringResource(R.string.gapTime_input_tag))
                else Text(stringResource(R.string.wrong_input_tag))
            }
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Row {
            Button(
                onClick = {
                    alarmViewModel.onSetAlarmClick(
                        context = context,
                        alarmManager = alarmViewModel.alarmManager,
                        notificationPermissionLauncher = notificationPermissionLauncher,
                        permissionLauncher = permissionLauncher
                    )
                },
            ) {
                Text(stringResource(R.string.set_exact_alarm))
            }
            Spacer(modifier = Modifier.padding(20.dp))
            Button(
                onClick = {
                    alarmViewModel.onCancelAlarmClick(context = context)
                },
            ) {
                Text(stringResource(R.string.cancel_current_alarm))
            }
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = stringResource(
                R.string.remaining_time,
                alarmUiState.leftTime / 1000 / 60,
                alarmUiState.leftTime / 1000 % 60
            )
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column (modifier = Modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                Text(
                    text = stringResource(R.string.description),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                IconButton(
                    onClick = { alarmViewModel.onExpandClick() }
                ) {
                    Icon(
                        imageVector = if (!alarmViewModel.expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.expand_button_content_description),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
            if (alarmViewModel.expanded) {
                Text(
                    text = stringResource(R.string.description_content),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                alarmViewModel.updateLeftTime()
                if (alarmUiState.leftTime < 0 && alarmViewModel.alarmOn) {
                    alarmViewModel.resetAlarm(
                        alarmManager = alarmViewModel.alarmManager,
                        context = context
                    )
                }
                delay(1000)
            }
        }
    }
}