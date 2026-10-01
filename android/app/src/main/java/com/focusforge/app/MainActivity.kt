package com.focusforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.focusforge.app.core.AppLogger
import com.focusforge.app.ui.FocusForgeNavHost
import com.focusforge.app.work.HeartbeatWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var workManager: WorkManager
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        AppLogger.i("MainActivity created")
        workManager.enqueueUniquePeriodicWork("focusforge-heartbeat", ExistingPeriodicWorkPolicy.KEEP, PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES).build())
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { FocusForgeNavHost() } } }
    }
}
