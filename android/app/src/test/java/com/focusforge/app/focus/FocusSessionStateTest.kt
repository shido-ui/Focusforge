package com.focusforge.app.focus

import com.focusforge.app.data.FocusSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusSessionStateTest {
    @Test
    fun onlyKnownPersistedStatesParse() {
        assertEquals(FocusSessionState.LOCKED, FocusSessionState.parse("LOCKED"))
        assertNull(FocusSessionState.parse("LOCKED "))
        assertNull(FocusSessionState.parse("banana"))
    }
}
