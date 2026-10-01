package com.focusforge.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.focusforge.app.work.HeartbeatWorker
import java.util.concurrent.TimeUnit
@AndroidEntryPoint
class MainActivity:ComponentActivity(){
 @Inject lateinit var workManager:WorkManager
 override fun onCreate(state:Bundle?){super.onCreate(state)
  workManager.enqueueUniquePeriodicWork("focusforge-heartbeat",ExistingPeriodicWorkPolicy.KEEP,PeriodicWorkRequestBuilder<HeartbeatWorker>(15,TimeUnit.MINUTES).build())
  setContent{MaterialTheme{Surface(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
   Text("FocusForge",style=MaterialTheme.typography.headlineLarge)
   Text("Foundation build • P1",Modifier.padding(top=8.dp))
  }}}}
 }
}
