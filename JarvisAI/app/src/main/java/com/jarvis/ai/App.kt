package com.jarvis.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.jarvis.ai.core.CrashLog

class App : Application() {
    override fun onCreate() {
        CrashLog.install()
        super.onCreate()
        instance = this
        CrashLog.breadcrumb("App.onCreate")
        // v1.4.0: auto online/offline switch — watch connectivity for the app lifetime
        runCatching { com.jarvis.ai.core.OfflineMode.watch(this) }
        // v1.3.9: self-healing — recover from repeated crashes (safe mode) + trim oversized data
        runCatching { com.jarvis.ai.core.SelfHeal.onAppStart(this) }
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(CH_SERVICE, "Jarvis running", NotificationManager.IMPORTANCE_LOW))
            nm.createNotificationChannel(NotificationChannel(CH_ALERT, "Jarvis alerts", NotificationManager.IMPORTANCE_HIGH))
        }
    }
    companion object {
        const val CH_SERVICE = "jarvis_service"
        const val CH_ALERT = "jarvis_alert"
        lateinit var instance: App
            private set
    }
}
