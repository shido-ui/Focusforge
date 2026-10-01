package com.focusforge.app.ui
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private object Routes { const val HOME="home"; const val SETTINGS="settings" }

@Composable
fun FocusForgeNavHost() {
 val navController=rememberNavController()
 NavHost(navController=navController,startDestination=Routes.HOME) {
  composable(Routes.HOME) { HomeScreen(onSettings={navController.navigate(Routes.SETTINGS)}) }
  composable(Routes.SETTINGS) { SettingsScreen(onBack={navController.popBackStack()}) }
 }
}
