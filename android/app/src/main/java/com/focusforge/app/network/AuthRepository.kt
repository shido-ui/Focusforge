package com.focusforge.app.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: FocusForgeApi,
) {
    private var session: AuthSession? = null

    fun currentSession(): AuthSession? = session

    fun signup(
        email: String,
        password: String,
        dateOfBirth: String,
        termsVersion: String,
        privacyVersion: String,
    ): AuthSession = api.signup(email, password, dateOfBirth, termsVersion, privacyVersion).also {
        session = it
    }

    fun login(email: String, password: String): AuthSession =
        api.login(email, password).also { session = it }

    fun logout() {
        session = null
    }

    fun deleteAccount() {
        val accessToken = session?.accessToken ?: throw ApiException(401, "Authentication required.")
        api.deleteAccount(accessToken)
        session = null
    }
}
