package com.focusforge.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity: ComponentActivity(){
 override fun onCreate(state:Bundle?){super.onCreate(state);setContent{
  MaterialTheme { Surface(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
   Text("FocusForge",style=MaterialTheme.typography.headlineLarge); Text("Foundation build • P1",Modifier.padding(top=8.dp))
  }}}
 }}
}
