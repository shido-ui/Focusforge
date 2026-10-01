package com.focusforge.app.core

import android.util.Log

object AppLogger {
    private const val TAG = "FocusForge"
    fun d(message: String) = Log.d(TAG, message)
    fun i(message: String) = Log.i(TAG, message)
    fun w(message: String, throwable: Throwable? = null) { if (throwable == null) Log.w(TAG, message) else Log.w(TAG, message, throwable) }
    fun e(message: String, throwable: Throwable? = null) { if (throwable == null) Log.e(TAG, message) else Log.e(TAG, message, throwable) }
}
