package com.example.tryalarm.data

import android.annotation.SuppressLint
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

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: UserPreferencesStore? = null

        fun getInstance(context: Context): UserPreferencesStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesStore(context).also { INSTANCE = it }
            }
        }
    }

    // 获取实时数据流
    fun observePreferences(): Flow<AlarmUiState> = context.dataStore.data.map { prefs ->
        AlarmUiState(
            triggerTime = (prefs[GAP_TIME_KEY]?.toDouble()?.times(60_000))?.toLong()
                ?.plus(System.currentTimeMillis()) ?: System.currentTimeMillis(),
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