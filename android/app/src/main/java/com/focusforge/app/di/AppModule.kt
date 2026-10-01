package com.focusforge.app.di
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.focusforge.app.data.FocusForgeDatabase
import com.focusforge.app.data.PreferencesStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule{
 @Provides @Singleton fun database(@ApplicationContext c:Context):FocusForgeDatabase =
  Room.databaseBuilder(c,FocusForgeDatabase::class.java,"focusforge.db").build()
 @Provides @Singleton fun preferences(@ApplicationContext c:Context)=PreferencesStore(c)
 @Provides @Singleton fun workManager(@ApplicationContext c:Context)=WorkManager.getInstance(c)
}
