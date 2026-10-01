package com.focusforge.p0.kiosk

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var dpm: DevicePolicyManager
    private lateinit var admin: ComponentName
    private lateinit var status: TextView

    private val prefs by lazy { getSharedPreferences("p0_session", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        admin = ComponentName(this, FocusDeviceAdminReceiver::class.java)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 32)
        }
        status = TextView(this).apply { textSize = 18f }
        layout.addView(status)
        layout.addView(Button(this).apply {
            text = "Refresh status"
            setOnClickListener { renderStatus() }
        })
        layout.addView(Button(this).apply {
            text = "ENTER LOCK TASK"
            setOnClickListener { enterLockTaskIfPossible() }
        })
        layout.addView(Button(this).apply {
            text = "EXIT LOCK TASK (TEST ONLY)"
            setOnClickListener {
                stopLockTask()
                prefs.edit().putString("state", "IDLE").apply()
                renderStatus()
            }
        })
        setContentView(layout)
        renderStatus()
    }

    private fun renderStatus() {
        val deviceOwner = dpm.isDeviceOwnerApp(packageName)
        val adminActive = dpm.isAdminActive(admin)
        val lockTaskPackages = if (deviceOwner) {
            runCatching { dpm.getLockTaskPackages() }.getOrDefault(emptyArray())
        } else emptyArray()
        status.text = buildString {
            appendLine("FocusForge P0 Device Control Proof — TEST HARNESS")
            appendLine("Device Owner: $deviceOwner")
            appendLine("Admin active: $adminActive")
            appendLine("Persisted session state: \${prefs.getString("state", "IDLE")}")
            appendLine("Lock-task allowlisted: \${packageName in lockTaskPackages}")
        }
    }

    private fun enterLockTaskIfPossible() {
        if (!dpm.isDeviceOwnerApp(packageName)) {
            status.text = "NOT DEVICE OWNER — provisioning is required before Lock Task can be tested."
            return
        }
        runCatching {
            dpm.setLockTaskPackages(admin, arrayOf(packageName))
            prefs.edit().putString("state", "LOCKED").apply()
            startLockTask()
        }.onFailure {
            prefs.edit().putString("state", "ERROR").apply()
            status.text = "Lock Task failed: \${it.javaClass.simpleName}: \${it.message}"
        }.onSuccess {
            renderStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        renderStatus()
    }
}
