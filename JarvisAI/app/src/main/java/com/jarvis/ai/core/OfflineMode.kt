package com.jarvis.ai.core

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.AlarmClock
import com.jarvis.ai.service.LockHelper
import com.jarvis.ai.tools.AppFinder

/**
 * v1.3.9 — Offline mode: when there is no internet (or the user forces it in
 * Settings), the basic features still work, instantly, on the phone itself.
 * MainActivity.submit() calls this before Gemini. Returns null when the
 * request needs the network.
 */
object OfflineMode {
    // v1.4.0: auto online/offline switch — the last known live state, kept by
    // the NetworkCallback watcher (started once in App.onCreate).
    @Volatile var autoOffline: Boolean = false
    @Volatile private var watching = false
    @Volatile private var lastNotifiedOnline: Boolean? = null

    /**
     * v1.4.0: true when there is no USABLE internet. A Wi-Fi that is connected
     * but has no real internet (captive portal, router down) does NOT count as
     * online — Android marks that with NET_CAPABILITY_VALIDATED, so we require
     * it too. The user can also force offline in Settings (Prefs.offlineMode).
     */
    fun isOffline(ctx: Context): Boolean {
        if (Prefs.offlineMode) return true
        return runCatching {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return true
            val caps = cm.getNetworkCapabilities(net) ?: return true
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }.getOrDefault(true)
    }

    fun typeName(ctx: Context): String = runCatching {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return "no network"
        val caps = cm.getNetworkCapabilities(net) ?: return "no network"
        when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "mobile data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "network"
        }
    }.getOrDefault("no network")

    fun statusText(ctx: Context): String {
        return if (isOffline(ctx)) {
            if (Prefs.offlineMode) "📵 Offline mode (forced in Settings) — internet thakleo sudhu local kaj cholbe. Online jete Settings > Offline mode off koro."
            else "📵 Offline — internet nei ekhon. Jarvis local mode-e ache: time, battery, torch, lock, app khola, call, alarm, scroll/back cholbe. Net ele nije thekei online hoye jabe."
        } else {
            "🌐 Online — ${typeName(ctx)}-e connected. Full Jarvis cholche."
        }
    }

    /**
     * v1.4.0: watch connectivity for the whole app lifetime. The moment the
     * internet drops, Jarvis switches to local/offline mode by itself; the
     * moment it returns, Jarvis switches back to online by itself — and tells
     * the user with a notification each time. Registers only once per process.
     */
    fun watch(appCtx: Context) {
        if (watching) return
        watching = true
        autoOffline = isOffline(appCtx)
        lastNotifiedOnline = !autoOffline
        val cm = appCtx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // a network appeared — wait for its capabilities before deciding
            }
            override fun onLost(network: Network) = setState(appCtx, true)
            override fun onUnavailable() = setState(appCtx, true)
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                val ok = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                setState(appCtx, !ok)
            }
        }
        runCatching {
            cm.registerNetworkCallback(
                NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), cb
            )
        }
    }

    private fun setState(ctx: Context, offline: Boolean) {
        if (Prefs.offlineMode) { autoOffline = true; return } // forced offline — no notifications
        autoOffline = offline
        if (lastNotifiedOnline == !offline) return // same state — don't spam
        lastNotifiedOnline = !offline
        BackgroundWorker.notifyDone(
            ctx,
            if (offline) "📵 Internet chole geche" else "🌐 Internet fire eseche",
            if (offline) "Jarvis nije thekei offline mode-e chole geche — basic kaj (time, torch, app khola, call, alarm) cholbe. Net ele abar full Jarvis hoye jabe."
            else "Jarvis nije thekei online hoye geche (${typeName(ctx)}) — full AI fire eseche."
        )
    }

    fun handleOffline(ctx: Context, raw: String): String? {
        val q = raw.trim().lowercase()
        val bn = raw.trim()
        // time / date / battery are already instant local answers
        QuickAnswer.tryAnswer(ctx, raw)?.let { return it }
        // torch on / off
        if (q.contains("torch on") || q.contains("flashlight on") || bn.contains("টর্চ জ্বালাও") || q.contains("light jalao")) {
            return if (setTorch(ctx, true)) "🔦 Torch on kore dilam (offline-eo kaj kore)." else "Torch on korte parlam na — camera permission lagbe."
        }
        if (q.contains("torch off") || q.contains("flashlight off") || bn.contains("টর্চ নিভাও") || q.contains("light nivao")) {
            return if (setTorch(ctx, false)) "🔦 Torch off kore dilam." else "Torch off korte parlam na."
        }
        // lock screen
        if (q.contains("lock") && (q.contains("screen") || q.contains("phone") || bn.contains("লক"))) {
            return if (LockHelper.lockNow(ctx)) "🔒 Screen lock kore dilam." else "Screen lock korte parlam na — Device Admin on korte hobe (Settings > Screen Lock)."
        }
        // open app by name — only when the app actually exists, otherwise let Gemini handle the sentence
        if (q.startsWith("open ") || q.startsWith("kholo ") || bn.startsWith("খোলো")) {
            val name = raw.trim().substringAfter("open ", "").substringAfter("kholo ", "").substringAfter("খোলো ", "").trim()
            if (name.isNotEmpty()) {
                val app = AppFinder.find(ctx, name)
                if (app != null && AppFinder.launch(ctx, app.pkg)) return "📱 ${app.label} khule dilam."
                // not an app name — fall through to the AI
            }
        }
        // volume up / down
        if (q.contains("volume up") || q.contains("volume barao") || bn.contains("ভলিউম বাড়াও")) {
            (ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager).adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            return "🔊 Volume baralam."
        }
        if (q.contains("volume down") || q.contains("volume komao") || bn.contains("ভলিউম কমাও")) {
            (ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager).adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            return "🔉 Volume komalam."
        }
        // call a number (opens the dialer if CALL_PHONE is not granted)
        val digits = raw.filter { it.isDigit() }
        if ((q.contains("call") || q.contains("phone koro") || bn.contains("কল করো")) && digits.length >= 7) {
            val i = Intent(Intent.ACTION_CALL, android.net.Uri.parse("tel:$digits")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            return runCatching { ctx.startActivity(i); "📞 $digits number-e call korchi." }.getOrNull()
                ?: runCatching {
                    ctx.startActivity(Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$digits")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    "📞 Dialer khule dilam — call button chapo."
                }.getOrNull()
        }
        // alarm (system alarm clock, works offline) — only when the user actually wants to SET one
        if ((q.contains("alarm") || bn.contains("অ্যালার্ম")) && (q.contains("set") || q.contains("dao") || q.contains("lagao") || bn.contains("দাও") || bn.contains("লাগাও") || Regex("\\d{1,2}").containsMatchIn(q))) {
            val h = Regex("(\\d{1,2})\\s*[:.]?\\s*(\\d{2})?\\s*(am|pm)?").find(q)
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                h?.let {
                    var hour = it.groupValues[1].toIntOrNull() ?: 7
                    val min = it.groupValues[2].toIntOrNull() ?: 0
                    val ampm = it.groupValues[3]
                    if (ampm == "pm" && hour < 12) hour += 12
                    if (ampm == "am" && hour == 12) hour = 0
                    putExtra(AlarmClock.EXTRA_HOUR, hour); putExtra(AlarmClock.EXTRA_MINUTES, min)
                }
                putExtra(AlarmClock.EXTRA_MESSAGE, "Jarvis alarm")
            }
            return runCatching { ctx.startActivity(intent); "⏰ Alarm set korar screen khule dilam (offline-eo kaj kore)." }.getOrNull()
        }
        return null
    }

    private fun setTorch(ctx: Context, on: Boolean): Boolean = runCatching {
        val cm = ctx.getSystemService(CameraManager::class.java)
        val id = cm.cameraIdList.firstOrNull() ?: return false
        cm.setTorchMode(id, on); true
    }.getOrDefault(false)
}
