package com.focusforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.focusforge.app.core.AppLogger
import com.focusforge.app.ui.FocusForgeNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        AppLogger.i("MainActivity created")
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    FocusForgeNavHost()
                }
            }
        }
    }
}
