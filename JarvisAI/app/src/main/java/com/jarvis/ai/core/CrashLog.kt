package com.jarvis.ai.core

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.MediaStore
import com.jarvis.ai.App
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * Crash diagnostics (v1.2.1).
 * If the app crashes, the full stack trace + startup breadcrumbs are written to
 * Downloads/JarvisAI/JarvisAI-crash-<time>.txt (and filesDir/crash-last.txt),
 * so the user can share the file and the crash can actually be fixed.
 * The handler never swallows the crash — after writing, it lets the system
 * kill the process exactly as before.
 */
object CrashLog {
    private val lock = Any()
    private val crumbs = mutableListOf<String>()

    private fun time(): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

    fun breadcrumb(s: String) {
        synchronized(lock) {
            crumbs.add("${time()} $s")
            if (crumbs.size > 100) crumbs.removeAt(0)
            runCatching { File(App.instance.filesDir, "startup.log").writeText(crumbs.joinToString("\n")) }
        }
    }

    fun install() {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            runCatching { writeCrash(t, e) }
            if (prev != null) prev.uncaughtException(t, e)
            else { Process.killProcess(Process.myPid()); exitProcess(10) }
        }
    }

    private fun writeCrash(t: Thread, e: Throwable) {
        val sw = StringWriter(); e.printStackTrace(PrintWriter(sw))
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val text = buildString {
            appendLine("Jarvis AI crash — $stamp")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Thread: ${t.name}")
            appendLine("--- startup breadcrumbs ---")
            synchronized(lock) { appendLine(crumbs.joinToString("\n")) }
            appendLine("--- stack trace ---")
            appendLine(sw.toString())
        }
        runCatching { File(App.instance.filesDir, "crash-last.txt").writeText(text) }
        // Downloads copy, so the user can find it in Files > Downloads > JarvisAI
        if (Build.VERSION.SDK_INT >= 29) runCatching {
            val ctx = App.instance
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "JarvisAI-crash-$stamp.txt")
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/JarvisAI")
            }
            val uri = ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) ctx.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        }
    }
}
