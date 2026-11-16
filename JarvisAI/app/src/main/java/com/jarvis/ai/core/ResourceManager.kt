package com.jarvis.ai.core

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import com.jarvis.ai.service.GestureControl

/**
 * v1.3.9 — Adaptive resource management: Jarvis watches battery + memory +
 * storage and adapts itself so the phone stays fast.
 * - Battery < 20% and not charging: disco light off, shake-to-wake off, tell the user.
 * - Low memory: clear the app cache.
 * - Storage < 500 MB free: trim old chat history.
 * Called from AssistantService start and from the resource_status tool.
 */
object ResourceManager {
    data class Status(val battery: Int, val charging: Boolean, val memAvailMB: Long, val memTotalMB: Long, val lowMemory: Boolean, val storageFreeMB: Long, val storageTotalMB: Long)

    fun status(ctx: Context): Status {
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val charging = bm.isCharging
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val sf = StatFs(Environment.getDataDirectory().path)
        val freeMB = sf.availableBytes / (1024 * 1024)
        val totalMB = sf.totalBytes / (1024 * 1024)
        return Status(level, charging, mi.availMem / (1024 * 1024), mi.totalMem / (1024 * 1024), mi.lowMemory, freeMB, totalMB)
    }

    /** Apply the adaptations; returns what changed. */
    fun adapt(ctx: Context): String {
        val s = status(ctx)
        val changes = mutableListOf<String>()
        if (s.battery in 1..19 && !s.charging) {
            if (Prefs.discoEnabled) { Prefs.discoEnabled = false; changes += "battery kom ($s.battery%) bole disco light off kore dilam" }
            if (Prefs.gestureShakeEnabled) { Prefs.gestureShakeEnabled = false; GestureControl.stop(); changes += "shake-to-wake off kore dilam (battery bachate)" }
        }
        if (s.lowMemory) {
            val freed = cleanCache(ctx)
            changes += "memory kom chilo — cache clean kore ${freed}MB khali korlam"
        }
        if (s.storageFreeMB < 500) {
            SelfHeal.trimHistory()
            changes += "storage kom chilo — purono chat history trim kore dilam"
        }
        return if (changes.isEmpty()) "Sob thik ache — battery ${s.battery}%, memory ${s.memAvailMB}MB free, storage ${s.storageFreeMB}MB free. Kono change laglo na."
        else "Adaptive resource management:\n- " + changes.joinToString("\n- ")
    }

    fun cleanCache(ctx: Context): Long {
        var freed = 0L
        fun wipe(f: java.io.File) {
            f.listFiles()?.forEach { c -> if (c.isDirectory) wipe(c) else { freed += c.length(); c.delete() } }
        }
        runCatching { wipe(ctx.cacheDir) }
        return freed / (1024 * 1024)
    }

    fun report(ctx: Context): String {
        val s = status(ctx)
        return buildString {
            appendLine("📊 Resource status:")
            appendLine("- Battery: ${s.battery}% ${if (s.charging) "(charging)" else "(not charging)"}")
            appendLine("- Memory: ${s.memAvailMB}MB free / ${s.memTotalMB}MB total ${if (s.lowMemory) "⚠️ LOW" else ""}")
            appendLine("- Storage: ${s.storageFreeMB}MB free / ${s.storageTotalMB}MB total")
            appendLine()
            append(adapt(ctx))
        }
    }
}
