package com.focusforge.app.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onSettings:()->Unit) {
 Scaffold(topBar={TopAppBar(title={Text("FocusForge")},actions={TextButton(onClick=onSettings){Text("Settings")}})}) { padding ->
  Column(Modifier.padding(padding).padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Text("Foundation build",style=MaterialTheme.typography.headlineMedium)
   Text("P1 • Android shell is ready for the next feature layers.")
  }
 }
}
