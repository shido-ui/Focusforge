package com.focusforge.app.network

import com.focusforge.app.BuildConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ApiConfigTest {
    @Test
    fun debugConfigNeverUsesPlaceholder() {
        assertNotEquals("https://localhost.invalid", BuildConfig.API_BASE_URL)
        assertFalse(BuildConfig.API_REQUIRES_HTTPS)
    }

    @Test
    fun placeholderEndpointIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            FocusForgeApi.requireValidBaseUrl("https://localhost.invalid", true)
        }
    }

    @Test
    fun releasePolicyRejectsHttp() {
        assertThrows(IllegalArgumentException::class.java) {
            FocusForgeApi.requireValidBaseUrl("http://192.168.1.20:8080", true)
        }
    }
}
