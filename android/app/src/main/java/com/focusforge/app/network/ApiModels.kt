package com.focusforge.app.network

import org.json.JSONObject

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresAtEpochMs: Long,
)

data class ApiError(
    val httpStatus: Int,
    val code: String,
    val userMessage: String,
    val requestId: String?,
    val retryable: Boolean,
)

class ApiException(
    val error: ApiError,
) : Exception(error.userMessage)

internal fun JSONObject.optNullableString(name: String): String? =
    if (has(name) && !isNull(name)) optString(name).ifBlank { null } else null
