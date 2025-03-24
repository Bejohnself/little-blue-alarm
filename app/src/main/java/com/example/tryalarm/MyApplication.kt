package com.example.tryalarm

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.tryalarm.data.UserPreferencesStore
import com.example.tryalarm.ui.state.AlarmViewModel

class MyApplication : Application(), LifecycleObserver {
    private val appViewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }
    lateinit var alarmViewModel: AlarmViewModel

    override fun onCreate() {
        super.onCreate()
        val store = UserPreferencesStore.getInstance(this) // 初始化 store
        alarmViewModel = ViewModelProvider(
            appViewModelStoreOwner,
            AlarmViewModelFactory(store)
        )[AlarmViewModel::class.java]
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    // 新的生命周期监听方式
    override fun onTerminate() {
        alarmViewModel.stopWidgetUpdateLoop()
        super.onTerminate()
    }
}


class AlarmViewModelFactory(private val store: UserPreferencesStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(store) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
