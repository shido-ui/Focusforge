package com.focusforge.app.data
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
private val Context.focusForgeDataStore by preferencesDataStore(name="focusforge_preferences")
class PreferencesStore(private val context:Context){
 private val onboardingKey=booleanPreferencesKey("onboarding_complete")
 val onboardingComplete:Flow<Boolean> = context.focusForgeDataStore.data.map{it[onboardingKey] ?: false}
 suspend fun setOnboardingComplete(value:Boolean){context.focusForgeDataStore.edit{it[onboardingKey]=value}}
}
