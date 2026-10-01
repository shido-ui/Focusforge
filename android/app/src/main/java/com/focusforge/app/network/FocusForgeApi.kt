package com.focusforge.app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.Instant

class FocusForgeApi(
    baseUrl: String,
    private val client: OkHttpClient,
    requiresHttps: Boolean,
) {
    private val baseUrl = requireValidBaseUrl(baseUrl, requiresHttps)
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun health(): Boolean = withContext(Dispatchers.IO) {
        execute(Request.Builder().url("$baseUrl/api/v1/health").get().build()).use { it.code == 200 }
    }

    suspend fun signup(
        email: String,
        password: String,
        dateOfBirth: String,
        termsVersion: String,
        privacyVersion: String,
        deviceLabel: String? = null,
    ): AuthSession = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("date_of_birth", dateOfBirth)
            .put("terms_version", termsVersion)
            .put("privacy_version", privacyVersion)
        authRequest("/api/v1/auth/signup", body, deviceLabel)
    }

    suspend fun login(email: String, password: String, deviceLabel: String? = null): AuthSession =
        withContext(Dispatchers.IO) {
            authRequest(
                "/api/v1/auth/login",
                JSONObject().put("email", email).put("password", password),
                deviceLabel,
            )
        }

    suspend fun refresh(refreshToken: String, deviceLabel: String? = null): AuthSession =
        withContext(Dispatchers.IO) {
            val body = JSONObject().put("refresh_token", refreshToken)
            if (deviceLabel != null) body.put("device_label", deviceLabel)
            authRequest("/api/v1/auth/refresh", body, null)
        }

    suspend fun logout(refreshToken: String) = withContext(Dispatchers.IO) {
        val body = JSONObject().put("refresh_token", refreshToken)
        execute(
            Request.Builder()
                .url("$baseUrl/api/v1/auth/logout")
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()
        ).use { response ->
            if (response.code !in 200..299) throw parseApiException(response)
        }
    }

    suspend fun deleteAccount(accessToken: String) = withContext(Dispatchers.IO) {
        execute(
            Request.Builder()
                .url("$baseUrl/api/v1/auth/account")
                .delete()
                .header("Authorization", "Bearer $accessToken")
                .build()
        ).use { response ->
            if (response.code != 204) throw parseApiException(response)
        }
    }

    suspend fun accountMe(accessToken: String): JSONObject = withContext(Dispatchers.IO) {
        execute(
            Request.Builder()
                .url("$baseUrl/api/v1/account/me")
                .get()
                .header("Authorization", "Bearer $accessToken")
                .build()
        ).use { response ->
            if (response.code !in 200..299) throw parseApiException(response)
            parseJsonObject(response)
        }
    }

    suspend fun syncEvent(accessToken: String, eventJson: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        execute(
            Request.Builder()
                .url("$baseUrl/api/v1/sync/events")
                .post(eventJson.toString().toRequestBody(jsonMediaType))
                .header("Authorization", "Bearer $accessToken")
                .header("Idempotency-Key", eventJson.optString("client_event_id"))
                .build()
        ).use { response ->
            if (response.code !in 200..299) throw parseApiException(response)
            parseJsonObject(response)
        }
    }

    private fun authRequest(path: String, body: JSONObject, deviceLabel: String?): AuthSession {
        if (deviceLabel != null) body.put("device_label", deviceLabel)
        execute(
            Request.Builder()
                .url(baseUrl + path)
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()
        ).use { response ->
            if (response.code !in 200..299) throw parseApiException(response)
            val json = parseJsonObject(response)
            val access = json.optString("access_token").takeIf { it.isNotBlank() }
                ?: throw malformedResponse(response.code)
            val refresh = json.optString("refresh_token").takeIf { it.isNotBlank() }
                ?: throw malformedResponse(response.code)
            val expiry = json.optString("access_token_expires_at").takeIf { it.isNotBlank() }
                ?.let { value -> runCatching { Instant.parse(value).toEpochMilli() }.getOrNull() }
                ?: throw malformedResponse(response.code)
            return AuthSession(
                accessToken = access,
                refreshToken = refresh,
                tokenType = json.optString("token_type", "bearer"),
                accessTokenExpiresAtEpochMs = expiry,
            )
        }
    }

    private fun parseJsonObject(response: okhttp3.Response): JSONObject {
        val payload = response.body?.string().orEmpty()
        return runCatching { JSONObject(payload) }.getOrElse { throw malformedResponse(response.code) }
    }

    private fun parseApiException(response: okhttp3.Response): ApiException {
        val payload = response.body?.string().orEmpty()
        val json = runCatching { JSONObject(payload) }.getOrNull()
        return ApiException(
            ApiError(
                httpStatus = response.code,
                code = json?.optString("code").orEmpty().ifBlank { "http_error" },
                userMessage = json?.optString("message").orEmpty().ifBlank { "Request failed." },
                requestId = json?.optString("request_id").orEmpty().ifBlank { null },
                retryable = json?.optBoolean("retryable", response.code >= 500) ?: (response.code >= 500),
            )
        )
    }

    private fun malformedResponse(status: Int): ApiException =
        ApiException(ApiError(status, "malformed_response", "The server returned an invalid response.", null, false))

    private fun execute(request: Request): okhttp3.Response =
        runCatching { client.newCall(request).execute() }.getOrElse {
            throw ApiException(ApiError(-1, "network_error", "Network request failed.", null, true))
        }

    companion object {
        fun requireValidBaseUrl(value: String, requiresHttps: Boolean): String {
            val normalized = value.trim().trimEnd('/')
            require(normalized.isNotBlank()) { "API base URL must not be blank." }
            require(!normalized.contains("localhost.invalid")) { "Placeholder API endpoint is forbidden." }
            require(normalized.startsWith("https://") || normalized.startsWith("http://")) {
                "API base URL must use http:// or https://."
            }
            if (requiresHttps) {
                require(normalized.startsWith("https://")) { "Production API endpoints must use HTTPS." }
            }
            return normalized
        }
    }
}
