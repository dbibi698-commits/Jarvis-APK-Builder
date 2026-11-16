package com.jarvis.ai.core

import android.content.Context
import com.jarvis.ai.service.JarvisAccessibilityService

/**
 * v1.3.9 — Voice-activated accessibility commands: instant, on-device, no AI
 * round-trip and no internet. "scroll down", "go back", "home", "tap <text>",
 * "swipe up/down" work the moment the user says them. MainActivity.submit()
 * tries this right after QuickAnswer. Needs Accessibility ON (Jarvis Settings >
 * Permissions), otherwise returns null and the AI handles it.
 */
object VoiceCommands {
    fun tryHandle(ctx: Context, raw: String): String? {
        val svc = JarvisAccessibilityService.instance ?: return null
        val q = raw.trim().lowercase()
        val bn = raw.trim()
        return when {
            q == "scroll down" || q == "niche jao" || q == "niche scroll" || bn == "নিচে যাও" || q == "next" || q == "porer ta" -> {
                if (svc.scroll("down")) "⬇️ Scroll down korlam." else "Scroll korte parlam na."
            }
            q == "scroll up" || q == "upore jao" || q == "upore scroll" || bn == "উপরে যাও" || q == "ager ta" -> {
                if (svc.scroll("up")) "⬆️ Scroll up korlam." else "Scroll korte parlam na."
            }
            q == "go back" || q == "back" || q == "pichone jao" || bn == "পিছনে যাও" || q == "back jao" -> {
                if (svc.global("back")) "⬅️ Back gelam." else "Back jete parlam na."
            }
            q == "home" || q == "home jao" || bn == "হোমে যাও" -> {
                if (svc.global("home")) "🏠 Home-e gelam." else "Home jete parlam na."
            }
            q == "notifications" || q == "notification kholo" || bn == "নোটিফিকেশন খোলো" -> {
                if (svc.global("notifications")) "🔔 Notifications khullam." else "Notifications khulte parlam na."
            }
            q == "swipe up" || q == "upore swipe" -> {
                val dm = ctx.resources.displayMetrics
                if (svc.swipe(dm.widthPixels / 2f, dm.heightPixels * 0.8f, dm.widthPixels / 2f, dm.heightPixels * 0.2f)) "⬆️ Swipe up korlam." else "Swipe korte parlam na."
            }
            q == "swipe down" || q == "niche swipe" -> {
                val dm = ctx.resources.displayMetrics
                if (svc.swipe(dm.widthPixels / 2f, dm.heightPixels * 0.2f, dm.widthPixels / 2f, dm.heightPixels * 0.8f)) "⬇️ Swipe down korlam." else "Swipe korte parlam na."
            }
            q.startsWith("tap ") || q.startsWith("click ") || bn.startsWith("ট্যাপ ") -> {
                val target = raw.trim().substringAfter("tap ", "").substringAfter("click ", "").substringAfter("ট্যাপ ", "").trim()
                if (target.isEmpty()) return null
                if (svc.clickText(target)) "👆 \"$target\"-e tap korlam." else "\"$target\" screen-e pelam na."
            }
            else -> null
        }
    }
}
