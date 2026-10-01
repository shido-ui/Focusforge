package com.focusforge.app.security

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresAtEpochMs: Long,
)

class SecureTokenStore(context: Context) {
    private val preferences = context.getSharedPreferences("focusforge_secure_session", Context.MODE_PRIVATE)
    private val keyAlias = "focusforge_auth_aes"

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = keyStore.getKey(keyAlias, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        generator.init(256)
        return generator.generateKey()
    }

    fun save(tokens: StoredTokens) {
        val payload = JSONObject()
            .put("access_token", tokens.accessToken)
            .put("refresh_token", tokens.refreshToken)
            .put("token_type", tokens.tokenType)
            .put("access_token_expires_at", tokens.accessTokenExpiresAtEpochMs)
            .toString()
            .toByteArray(StandardCharsets.UTF_8)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(payload)
        val packed = ByteArray(cipher.iv.size + encrypted.size)
        System.arraycopy(cipher.iv, 0, packed, 0, cipher.iv.size)
        System.arraycopy(encrypted, 0, packed, cipher.iv.size, encrypted.size)
        preferences.edit()
            .putString("blob", Base64.encodeToString(packed, Base64.NO_WRAP))
            .apply()
    }

    fun load(): StoredTokens? {
        val encoded = preferences.getString("blob", null) ?: return null
        return runCatching {
            val packed = Base64.decode(encoded, Base64.NO_WRAP)
            require(packed.size > 12)
            val iv = packed.copyOfRange(0, 12)
            val ciphertext = packed.copyOfRange(12, packed.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            val json = JSONObject(String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8))
            StoredTokens(
                accessToken = json.getString("access_token"),
                refreshToken = json.getString("refresh_token"),
                tokenType = json.optString("token_type", "bearer"),
                accessTokenExpiresAtEpochMs = json.getLong("access_token_expires_at"),
            )
        }.getOrNull()
    }

    fun clear() {
        preferences.edit().remove("blob").apply()
    }
}
