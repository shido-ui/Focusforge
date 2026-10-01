package com.focusforge.app.di

import com.focusforge.app.data.FocusForgeDatabase
import com.focusforge.app.focus.FocusSessionRepository
import com.focusforge.app.network.AuthRepository
import com.focusforge.app.network.FocusForgeApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun database(): FocusForgeDatabase
    fun authRepository(): AuthRepository
    fun api(): FocusForgeApi
    fun focusSessionRepository(): FocusSessionRepository
}
