package com.focusforge.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.focusforge.app.data.FocusForgeDatabase
import com.focusforge.app.data.PreferencesStore
import com.focusforge.app.network.FocusForgeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): FocusForgeDatabase =
        Room.databaseBuilder(context, FocusForgeDatabase::class.java, "focusforge.db").build()

    @Provides
    @Singleton
    fun preferences(@ApplicationContext context: Context) = PreferencesStore(context)

    @Provides
    @Singleton
    fun workManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun httpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun api(httpClient: OkHttpClient): FocusForgeApi =
        FocusForgeApi(BuildConfig.API_BASE_URL, httpClient)
}
