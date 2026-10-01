package com.focusforge.app.network

import com.focusforge.app.data.FocusForgeDatabase
import com.focusforge.app.security.SecureTokenStore
import com.focusforge.app.security.StoredTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthState {
    data object Restoring : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val session: AuthSession) : AuthState
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: FocusForgeApi,
    private val tokenStore: SecureTokenStore,
    private val database: FocusForgeDatabase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshMutex = Mutex()
    private val _state = MutableStateFlow<AuthState>(AuthState.Restoring)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        scope.launch { restoreSession() }
    }

    private suspend fun restoreSession() {
        val stored = tokenStore.load()
        if (stored == null) {
            _state.value = AuthState.SignedOut
            return
        }
        val session = stored.toAuthSession()
        if (session.accessTokenExpiresAtEpochMs <= System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)) {
            try {
                refreshIfNeeded(session)
            } catch (_: Exception) {
                clearLocalSession()
            }
        } else {
            _state.value = AuthState.SignedIn(session)
        }
    }

    suspend fun signup(
        email: String,
        password: String,
        dateOfBirth: String,
        termsVersion: String,
        privacyVersion: String,
        deviceLabel: String? = null,
    ): AuthSession {
        val session = api.signup(email, password, dateOfBirth, termsVersion, privacyVersion, deviceLabel)
        persist(session)
        return session
    }

    suspend fun login(email: String, password: String, deviceLabel: String? = null): AuthSession {
        val session = api.login(email, password, deviceLabel)
        persist(session)
        return session
    }

    suspend fun currentAccessToken(): String {
        val current = (_state.value as? AuthState.SignedIn)?.session
            ?: throw ApiException(ApiError(401, "authentication_required", "Authentication required.", null, false))
        return refreshIfNeeded(current).accessToken
    }

    suspend fun deleteAccount() {
        val access = currentAccessToken()
        api.deleteAccount(access)
        clearLocalSession(clearDatabase = true)
    }

    suspend fun logout() {
        val current = (_state.value as? AuthState.SignedIn)?.session
        if (current != null) runCatching { api.logout(current.refreshToken) }
        clearLocalSession(clearDatabase = false)
    }

    private suspend fun refreshIfNeeded(session: AuthSession): AuthSession =
        refreshMutex.withLock {
            val latest = (_state.value as? AuthState.SignedIn)?.session ?: session
            if (latest.accessTokenExpiresAtEpochMs > System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)) {
                _state.value = AuthState.SignedIn(latest)
                return@withLock latest
            }
            val refreshed = api.refresh(latest.refreshToken)
            persist(refreshed)
            refreshed
        }

    private fun persist(session: AuthSession) {
        tokenStore.save(
            StoredTokens(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                tokenType = session.tokenType,
                accessTokenExpiresAtEpochMs = session.accessTokenExpiresAtEpochMs,
            )
        )
        _state.value = AuthState.SignedIn(session)
    }

    private suspend fun clearLocalSession(clearDatabase: Boolean = false) {
        tokenStore.clear()
        if (clearDatabase) database.clearAllTables()
        _state.value = AuthState.SignedOut
    }

    private fun StoredTokens.toAuthSession() = AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        accessTokenExpiresAtEpochMs = accessTokenExpiresAtEpochMs,
    )
}
