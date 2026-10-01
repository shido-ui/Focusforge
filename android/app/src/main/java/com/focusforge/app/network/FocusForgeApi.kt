package com.focusforge.app.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class AuthSession(val accessToken: String, val tokenType: String)

class ApiException(val code: Int, message: String) : Exception(message)

class FocusForgeApi(
    baseUrl: String,
    private val client: OkHttpClient,
) {
    private val baseUrl = baseUrl.trimEnd('/')
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun health(): Boolean {
        val request = Request.Builder().url("$baseUrl/api/v1/health").get().build()
        return execute(request).code == 200
    }

    fun signup(
        email: String,
        password: String,
        dateOfBirth: String,
        termsVersion: String,
        privacyVersion: String,
    ): AuthSession {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("date_of_birth", dateOfBirth)
            .put("terms_version", termsVersion)
            .put("privacy_version", privacyVersion)
        return authRequest("/api/v1/auth/signup", body)
    }

    fun login(email: String, password: String): AuthSession {
        val body = JSONObject().put("email", email).put("password", password)
        return authRequest("/api/v1/auth/login", body)
    }

    fun deleteAccount(accessToken: String) {
        val request = Request.Builder()
            .url("$baseUrl/api/v1/auth/account")
            .delete()
            .header("Authorization", "Bearer $accessToken")
            .build()
        val response = execute(request)
        if (response.code != 204) {
            throw ApiException(response.code, response.body?.string().orEmpty().ifBlank { "Account deletion failed." })
        }
    }

    private fun authRequest(path: String, body: JSONObject): AuthSession {
        val request = Request.Builder()
            .url(baseUrl + path)
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
        val response = execute(request)
        val payload = response.body?.string().orEmpty()
        if (response.code !in 200..299) {
            val message = runCatching { JSONObject(payload).optString("detail") }.getOrNull()
                ?.ifBlank { null } ?: "Request failed."
            throw ApiException(response.code, message)
        }
        val json = JSONObject(payload)
        return AuthSession(
            accessToken = json.getString("access_token"),
            tokenType = json.optString("token_type", "bearer"),
        )
    }

    private fun execute(request: Request): okhttp3.Response =
        try {
            client.newCall(request).execute()
        } catch (error: Exception) {
            throw ApiException(-1, error.message ?: "Network request failed.")
        }
}
