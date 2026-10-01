package com.focusforge.app.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack:()->Unit) {
 Scaffold(topBar={TopAppBar(title={Text("Settings")},navigationIcon={IconButton(onClick=onBack){Text("‹")}})}) { padding ->
  Column(Modifier.padding(padding).padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Text("Account & app configuration")
   Text("Focus controls will be introduced in P2.")
  }
 }
}
