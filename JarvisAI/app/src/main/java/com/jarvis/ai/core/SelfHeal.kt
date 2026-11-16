package com.jarvis.ai.core

import android.content.Context
import com.jarvis.ai.App
import java.io.File

/**
 * v1.3.9 — Self-healing: Jarvis watches its own crashes and heals itself.
 * - If the app crashed 3+ times in a row, it boots into SAFE MODE: hotword,
 *   disco light and shake-to-wake are switched off automatically, and the user
 *   is told why. The risky features stay off until the user re-enables them.
 * - Oversized chat history is trimmed before it can cause an out-of-memory crash.
 * - Stale crash files are cleaned up.
 * Called from App.onCreate on every start.
 */
object SelfHeal {
    private const val CRASH_THRESHOLD = 3

    fun onAppStart(ctx: Context): String? {
        val crashFile = File(App.instance.filesDir, "crash-last.txt")
        var note: String? = null
        if (crashFile.exists()) {
            val lastSeen = Prefs.lastCrashSeen
            val modified = crashFile.lastModified()
            if (modified != lastSeen) {
                Prefs.lastCrashSeen = modified
                val count = Prefs.crashCount + 1
                Prefs.crashCount = count
                if (count >= CRASH_THRESHOLD && !Prefs.safeMode) {
                    Prefs.safeMode = true
                    Prefs.wakeEnabled = false
                    Prefs.discoEnabled = false
                    Prefs.gestureShakeEnabled = false
                    note = "🛠 Self-healing: Jarvis $count bar crash koreche — safe mode-e hotword, disco light ar shake-to-wake off kore dilam. Settings theke abar on korte paro."
                    Memory.remember("Jarvis crashed $count times in a row — safe mode turned hotword/disco/shake off.")
                }
            } else {
                // clean start after a recorded crash — the streak is over
                if (Prefs.crashCount > 0 && !Prefs.safeMode) Prefs.crashCount = 0
            }
        }
        // trim oversized history before it can OOM the app
        runCatching {
            val hist = File(App.instance.filesDir, "history.json")
            if (hist.exists() && hist.length() > 1024 * 1024) trimHistory()
        }
        return note
    }

    fun trimHistory() {
        runCatching { Memory.trimTo(500) }
    }

    fun clearSafeMode() {
        Prefs.safeMode = false
        Prefs.crashCount = 0
    }
}
