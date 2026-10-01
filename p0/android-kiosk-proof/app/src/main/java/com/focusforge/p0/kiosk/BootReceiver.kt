package com.focusforge.p0.kiosk

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, FocusDeviceAdminReceiver::class.java)
        if (!dpm.isDeviceOwnerApp(context.packageName) || !dpm.isAdminActive(admin)) {
            Log.w("FocusForgeP0", "Boot recovery skipped: Device Owner/admin state is not valid.")
            return
        }

        val prefs = context.getSharedPreferences("p0_session", Context.MODE_PRIVATE)
        val state = prefs.getString("state", "IDLE")
        if (state != "LOCKED" && state != "ARMED") {
            Log.i("FocusForgeP0", "Boot recovery skipped: no active persisted focus state.")
            return
        }

        runCatching {
            context.startActivity(
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            )
        }.onFailure {
            Log.e("FocusForgeP0", "Boot activity restore was rejected by Android/OEM policy.", it)
        }
    }
}
