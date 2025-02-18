package com.example.tryalarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tryalarm.ui.state.AlarmUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class UserPreferencesStore(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    // 定义键值
    companion object {
        private val TIP_KEY = stringPreferencesKey("tip")
        private val GAP_TIME_KEY = stringPreferencesKey("gap_time")
    }

    // 获取实时数据流
    fun observePreferences(): Flow<AlarmUiState> = context.dataStore.data.map { prefs ->
        AlarmUiState(
            leftTime = 0L,
            triggerTime = 0L,
            gapTime = prefs[GAP_TIME_KEY],
            tip = prefs[TIP_KEY]
        )
    }

    // 更新数据
    suspend fun update(tip: String, gapTime: String) {
        context.dataStore.edit { prefs ->
            prefs[TIP_KEY] = tip
            prefs[GAP_TIME_KEY] = gapTime
        }
    }

    // 清空数据
    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}