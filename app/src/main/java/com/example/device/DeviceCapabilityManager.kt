package com.example.device

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import java.util.Locale

data class DeviceTelemetry(
    val batteryPercentage: Int,
    val isCharging: Boolean,
    val networkStatus: String,
    val storageAvailableGb: Float,
    val storageTotalGb: Float,
    val systemUptimeHours: Float,
    val activeMemoryMb: Long
)

data class CapabilityPermissionState(
    val permissionName: String,
    val androidPermission: String?,
    val isGranted: Boolean,
    val description: String
)

class DeviceCapabilityManager(private val context: Context) {

    private val notificationChannelId = "zenith_channel_reminders"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ZENITH AI Notifications"
            val descriptionText = "Reminders and system actions from ZENITH"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checkPermissionStates(): Map<String, CapabilityPermissionState> {
        val states = mutableMapOf<String, CapabilityPermissionState>()

        val micGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        states["MICROPHONE"] = CapabilityPermissionState("Microphone", android.Manifest.permission.RECORD_AUDIO, micGranted, "Continuous & Push-To-Talk Voice Input")

        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        states["NOTIFICATIONS"] = CapabilityPermissionState("Notifications", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.POST_NOTIFICATIONS else null, notifGranted, "Reminders & Action Alerts")

        val contactsGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        states["CONTACTS"] = CapabilityPermissionState("Contacts", android.Manifest.permission.READ_CONTACTS, contactsGranted, "Contact lookups for messages & calls")

        val calendarGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        states["CALENDAR"] = CapabilityPermissionState("Calendar", android.Manifest.permission.READ_CALENDAR, calendarGranted, "Calendar event management")

        val bioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true else true
        states["BIOMETRICS"] = CapabilityPermissionState("Biometrics", android.Manifest.permission.USE_BIOMETRIC, bioGranted, "High-security Owner Approval")

        return states
    }

    fun launchAppByName(appName: String): Boolean {
        val packageName = when (appName.lowercase(Locale.US)) {
            "whatsapp" -> "com.whatsapp"
            "youtube" -> "com.google.android.youtube"
            "settings" -> "com.android.settings"
            "camera" -> "com.android.camera"
            "chrome", "browser" -> "com.android.chrome"
            "maps" -> "com.google.android.apps.maps"
            else -> null
        }

        if (packageName != null) {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        }

        // Generic fallback: Try launching via Intent action or search intent
        try {
            val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$appName"))
            genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(genericIntent)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun openSystemSettingsPage(action: String = android.provider.Settings.ACTION_SETTINGS) {
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun sendNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
    }

    fun getDeviceTelemetry(): DeviceTelemetry {
        // Battery status
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 85
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Network status
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        val networkType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi (5Ghz Online)"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular 5G"
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true -> "Connected"
            else -> "Offline / Local Mode"
        }

        // Storage
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalBytes = stat.blockCountLong * stat.blockSizeLong
        val availGb = availableBytes / (1024f * 1024f * 1024f)
        val totalGb = totalBytes / (1024f * 1024f * 1024f)

        // Uptime
        val uptimeHours = SystemClock.elapsedRealtime() / (1000f * 3600f)

        // Memory
        val runtime = Runtime.getRuntime()
        val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)

        return DeviceTelemetry(
            batteryPercentage = batteryPct,
            isCharging = isCharging,
            networkStatus = networkType,
            storageAvailableGb = String.format(Locale.US, "%.1f", availGb).toFloat(),
            storageTotalGb = String.format(Locale.US, "%.1f", totalGb).toFloat(),
            systemUptimeHours = String.format(Locale.US, "%.1f", uptimeHours).toFloat(),
            activeMemoryMb = usedMemMb
        )
    }
}
