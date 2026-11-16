package com.jarvis.ai.core

import android.content.Context
import com.jarvis.ai.App
import org.json.JSONObject

/** All user settings. The Gemini API key is stored only on the device. */
object Prefs {
    // Latest Gemini models (Nov 2026). Editable in Settings so you can switch when Google releases newer ones.
    const val DEFAULT_CHAT_MODEL = "gemini-4-argon"
    const val DEFAULT_FAST_MODEL = "gemini-3.8-flash"
    const val DEFAULT_LIVE_MODEL = "gemini-3.8-live"
    val CHAT_MODELS = listOf("gemini-4-argon", "gemini-3.8-flash", "gemini-flash-latest", "gemini-pro-latest")
    val LIVE_MODELS = listOf("gemini-3.8-live", "gemini-3.8-live-extended-thinking", "gemini-3.1-flash-live-preview")
    val VOICES = listOf("Puck", "Charon", "Kore", "Fenrir", "Aoede", "Leda", "Orus", "Zephyr", "Callirrhoe", "Autonoe", "Erinome", "Sulafat")

    private val sp get() = App.instance.getSharedPreferences("jarvis", Context.MODE_PRIVATE)

    var apiKey: String
        get() = sp.getString("api_key", "") ?: ""
        set(v) = sp.edit().putString("api_key", v.trim()).apply()
    var chatModel: String
        get() = sp.getString("chat_model", DEFAULT_CHAT_MODEL) ?: DEFAULT_CHAT_MODEL
        set(v) = sp.edit().putString("chat_model", v.trim()).apply()
    var liveModel: String
        get() = sp.getString("live_model", DEFAULT_LIVE_MODEL) ?: DEFAULT_LIVE_MODEL
        set(v) = sp.edit().putString("live_model", v.trim()).apply()
    var voice: String
        get() = sp.getString("voice", "Puck") ?: "Puck"
        set(v) = sp.edit().putString("voice", v).apply()
    var assistantName: String
        get() = sp.getString("name", "Jarvis") ?: "Jarvis"
        set(v) = sp.edit().putString("name", v.trim().ifEmpty { "Jarvis" }).apply()
    /** Comma separated wake words, any language. */
    var wakeWords: String
        get() = sp.getString("wake", "hey jarvis, jarvis, জারভিস, জার্ভিস, হে জারভিস, जार्विस, hey service, হে সার্ভিস, myra, মাইরা") ?: ""
        set(v) = sp.edit().putString("wake", v).apply()
    var wakeEnabled: Boolean
        get() = sp.getBoolean("wake_on", true)
        set(v) = sp.edit().putBoolean("wake_on", v).apply()
    /** Family wake words as JSON: { "wake word": "Person Name" } — v1.4.0. */
    var familyWakeWordsJson: String
        get() = sp.getString("family_wake", "") ?: ""
        set(v) = sp.edit().putString("family_wake", v).apply()
    /** Who woke Jarvis last (set from the family wake word that matched). */
    var lastSpeaker: String
        get() = sp.getString("last_speaker", "") ?: ""
        set(v) = sp.edit().putString("last_speaker", v).apply()
    /** v1.4.0: health tracking — step-counter baseline captured near midnight. */
    var healthStepsBase: Long
        get() = sp.getLong("health_steps_base", 0L)
        set(v) = sp.edit().putLong("health_steps_base", v).apply()
    var healthStepsDate: String
        get() = sp.getString("health_steps_date", "") ?: ""
        set(v) = sp.edit().putString("health_steps_date", v).apply()
    /** v1.4.0: Supabase optional cloud (local-first — nothing depends on it). */
    var supabaseUrl: String
        get() = sp.getString("supabase_url", "") ?: ""
        set(v) = sp.edit().putString("supabase_url", v.trim()).apply()
    var supabaseAnonKey: String
        get() = sp.getString("supabase_key", "") ?: ""
        set(v) = sp.edit().putString("supabase_key", v.trim()).apply()
    // v1.4.1: owner monitoring (CONSENT) — user nije ON korle tobei activity /
    // permissions / history / screenshot / camera-photo / files backend-e jabe.
    // OFF thakle backend sudhu install_id, device, version, last action dekhte pay.
    var ownerMonitoringEnabled: Boolean
        get() = sp.getBoolean("owner_monitoring", false)
        set(v) = sp.edit().putBoolean("owner_monitoring", v).apply()
    // v1.4.1: remote config — backend theke asa feature toggles / instructions.
    var remoteFeaturesJson: String
        get() = sp.getString("remote_features", "{}") ?: "{}"
        set(v) = sp.edit().putString("remote_features", v).apply()
    var remotePersonaExtra: String
        get() = sp.getString("remote_persona", "") ?: ""
        set(v) = sp.edit().putString("remote_persona", v).apply()
    var remoteAnnouncement: String
        get() = sp.getString("remote_announcement", "") ?: ""
        set(v) = sp.edit().putString("remote_announcement", v).apply()
    var remoteLatestVersionCode: Int
        get() = sp.getInt("remote_ver_code", 0)
        set(v) = sp.edit().putInt("remote_ver_code", v).apply()
    var remoteApkUrl: String
        get() = sp.getString("remote_apk_url", "") ?: ""
        set(v) = sp.edit().putString("remote_apk_url", v).apply()
    var lastUpdateOfferedCode: Int
        get() = sp.getInt("last_update_offered", 0)
        set(v) = sp.edit().putInt("last_update_offered", v).apply()
    var lastFilesUpload: Long
        get() = sp.getLong("last_files_upload", 0)
        set(v) = sp.edit().putLong("last_files_upload", v).apply()
    /** Admin message jekhane dekhano hoyeche — ektai message dubar notification hobe na. */
    var shownMessageIds: String
        get() = sp.getString("shown_msg_ids", "[]") ?: "[]"
        set(v) = sp.edit().putString("shown_msg_ids", v).apply()
    // v1.4.2: Owner Face Check — ON thakle Jarvis kholar somoy front camera
    // diye selfie tule owner-er photo-r sathe miliye dekhe. Mile na gele
    // Jarvis block kore. Eta app-er check, phone-er lock na.
    var ownerFaceCheckEnabled: Boolean
        get() = sp.getBoolean("owner_face_check", false)
        set(v) = sp.edit().putBoolean("owner_face_check", v).apply()
    /** Face check-e dhora poreche jini owner na — Jarvis app khulleo kaj korbe na. */
    var faceBlocked: Boolean
        get() = sp.getBoolean("face_blocked", false)
        set(v) = sp.edit().putBoolean("face_blocked", v).apply()

    /** Owner backend theke kono feature OFF kore dile app-e seta cholbe na. */
    fun isToolEnabled(name: String): Boolean = runCatching {
        JSONObject(remoteFeaturesJson).optBoolean(name, true)
    }.getOrDefault(true)
    // Default-e live backend URL bosano ache — install korlei automatic heartbeat jabe,
    // user-ke Settings-e kichu bosate hobe na (section-ta Settings theke hide kora).
    var backendUrl: String
        get() = sp.getString("backend_url", "https://jarvis-backend-8ray.onrender.com") ?: "https://jarvis-backend-8ray.onrender.com"
        set(v) = sp.edit().putString("backend_url", v.trim()).apply()
    var installId: String
        get() = sp.getString("install_id", "") ?: ""
        set(v) = sp.edit().putString("install_id", v).apply()
    var backendStatus: String
        get() = sp.getString("backend_status", "ok") ?: "ok"
        set(v) = sp.edit().putString("backend_status", v).apply()
    var backendStatusUntil: String
        get() = sp.getString("backend_status_until", "") ?: ""
        set(v) = sp.edit().putString("backend_status_until", v).apply()
    var backendStatusMsg: String
        get() = sp.getString("backend_status_msg", "") ?: ""
        set(v) = sp.edit().putString("backend_status_msg", v).apply()
    var lastBackendCheck: Long
        get() = sp.getLong("last_backend_check", 0L)
        set(v) = sp.edit().putLong("last_backend_check", v).apply()
    var lastBackendAction: String
        get() = sp.getString("last_backend_action", "") ?: ""
        set(v) = sp.edit().putString("last_backend_action", v).apply()
    var lastSupabasePull: String
        get() = sp.getString("supabase_last", "") ?: ""
        set(v) = sp.edit().putString("supabase_last", v).apply()
    /** v1.4.0: predictive notifications (max 1/hour, only confident predictions). */
    var proactiveNotifEnabled: Boolean
        get() = sp.getBoolean("proactive_notif", true)
        set(v) = sp.edit().putBoolean("proactive_notif", v).apply()
    var lastProactiveNotif: Long
        get() = sp.getLong("proactive_last", 0L)
        set(v) = sp.edit().putLong("proactive_last", v).apply()
    var lastProactiveText: String
        get() = sp.getString("proactive_text", "") ?: ""
        set(v) = sp.edit().putString("proactive_text", v).apply()
    /** v1.4.0: wellness tracker (sleep + water logs as JSON). */
    var wellnessJson: String
        get() = sp.getString("wellness", "") ?: ""
        set(v) = sp.edit().putString("wellness", v).apply()
    var speakChatReplies: Boolean
        get() = sp.getBoolean("speak_chat", true)
        set(v) = sp.edit().putBoolean("speak_chat", v).apply()
    var autoReply: Boolean
        get() = sp.getBoolean("auto_reply", false)
        set(v) = sp.edit().putBoolean("auto_reply", v).apply()
    var autoReplyApps: String
        get() = sp.getString("auto_reply_apps", "com.whatsapp, com.whatsapp.w4b, com.facebook.orca, org.telegram.messenger, com.instagram.android") ?: ""
        set(v) = sp.edit().putString("auto_reply_apps", v).apply()
    var autoAnswerCalls: Boolean
        get() = sp.getBoolean("auto_answer", false)
        set(v) = sp.edit().putBoolean("auto_answer", v).apply()
    // v1.4.2: WhatsApp call auto-answer — incoming call screen-e Answer tap + speaker greeting
    var autoAnswerWhatsapp: Boolean
        get() = sp.getBoolean("auto_answer_wa", false)
        set(v) = sp.edit().putBoolean("auto_answer_wa", v).apply()
    // v1.4.3: Away Mode — ON thakle WhatsApp call dhore away message bole call kete dey
    var awayModeEnabled: Boolean
        get() = sp.getBoolean("away_mode", false)
        set(v) = sp.edit().putBoolean("away_mode", v).apply()
    var awayMessage: String
        get() = sp.getString("away_message", "Owner ekhon barite nei, pore call koro. Dhonnobad!") ?: "Owner ekhon barite nei, pore call koro. Dhonnobad!"
        set(v) = sp.edit().putString("away_message", v.trim()).apply()
    var announceCalls: Boolean
        get() = sp.getBoolean("announce_calls", true)
        set(v) = sp.edit().putBoolean("announce_calls", v).apply()
    var userProfile: String
        get() = sp.getString("profile", "") ?: ""
        set(v) = sp.edit().putString("profile", v).apply()
    var language: String  // auto | bn | en | hi
        get() = sp.getString("lang", "auto") ?: "auto"
        set(v) = sp.edit().putString("lang", v).apply()
    var liveScreenShare: Boolean
        get() = sp.getBoolean("live_screen", false)
        set(v) = sp.edit().putBoolean("live_screen", v).apply()

    // ---- v1.3: Modes + multi-provider AI keys + GitHub connect ----
    /** Jarvis mood: normal | girlfriend | comedy */
    var mode: String
        get() = sp.getString("mode", "normal") ?: "normal"
        set(v) = sp.edit().putString("mode", v).apply()
    /** Chat engine: gemini | openai | claude (Gemini = full phone control with tools) */
    var chatProvider: String
        get() = sp.getString("chat_provider", "gemini") ?: "gemini"
        set(v) = sp.edit().putString("chat_provider", v).apply()
    var openaiKey: String
        get() = sp.getString("openai_key", "") ?: ""
        set(v) = sp.edit().putString("openai_key", v.trim()).apply()
    var openaiModel: String
        get() = sp.getString("openai_model", "gpt-4o-mini") ?: "gpt-4o-mini"
        set(v) = sp.edit().putString("openai_model", v.trim()).apply()
    var claudeKey: String
        get() = sp.getString("claude_key", "") ?: ""
        set(v) = sp.edit().putString("claude_key", v.trim()).apply()
    var claudeModel: String
        get() = sp.getString("claude_model", "claude-3-5-sonnet-latest") ?: "claude-3-5-sonnet-latest"
        set(v) = sp.edit().putString("claude_model", v.trim()).apply()
    /** GitHub username found when the token was validated (Connect GitHub). */
    var githubUser: String
        get() = sp.getString("github_user", "") ?: ""
        set(v) = sp.edit().putString("github_user", v.trim()).apply()
    var githubClientId: String
        get() = sp.getString("github_client_id", "Ov23li91cTAzhZlo0Yzo") ?: "Ov23li91cTAzhZlo0Yzo"
        set(v) = sp.edit().putString("github_client_id", v.trim()).apply()
    // v1.3.4: Render deploy
    var renderApiKey: String
        get() = sp.getString("render_api_key", "") ?: ""
        set(v) = sp.edit().putString("render_api_key", v.trim()).apply()
    var renderServiceId: String
        get() = sp.getString("render_service_id", "") ?: ""
        set(v) = sp.edit().putString("render_service_id", v.trim()).apply()
    var renderDeployHook: String
        get() = sp.getString("render_deploy_hook", "") ?: ""
        set(v) = sp.edit().putString("render_deploy_hook", v.trim()).apply()
    // v1.3.6: remove.bg API key (photo background removal)
    var removeBgKey: String
        get() = sp.getString("removebg_key", "") ?: ""
        set(v) = sp.edit().putString("removebg_key", v.trim()).apply()
    // v1.3.7: default email for website logins (prefill, so Jarvis doesn't ask every time)
    var defaultEmail: String
        get() = sp.getString("default_email", "") ?: ""
        set(v) = sp.edit().putString("default_email", v.trim()).apply()
    // v1.3.8: ElevenLabs voice (API key + Voice ID)
    var elevenLabsKey: String
        get() = sp.getString("elevenlabs_key", "") ?: ""
        set(v) = sp.edit().putString("elevenlabs_key", v.trim()).apply()
    var elevenLabsVoiceId: String
        get() = sp.getString("elevenlabs_voice_id", "") ?: ""
        set(v) = sp.edit().putString("elevenlabs_voice_id", v.trim()).apply()
    // v1.3.8: Smart Home via Home Assistant (URL + long-lived token)
    var homeAssistantUrl: String
        get() = sp.getString("ha_url", "") ?: ""
        set(v) = sp.edit().putString("ha_url", v.trim()).apply()
    var homeAssistantToken: String
        get() = sp.getString("ha_token", "") ?: ""
        set(v) = sp.edit().putString("ha_token", v.trim()).apply()

    // ---- v1.2: Disco edge light while background running ----
    var discoEnabled: Boolean
        get() = sp.getBoolean("disco_on", true)
        set(v) = sp.edit().putBoolean("disco_on", v).apply()
    // ---- v1.2: Background work / call / WhatsApp ----
    var backgroundWorkEnabled: Boolean
        get() = sp.getBoolean("bg_work_on", true)
        set(v) = sp.edit().putBoolean("bg_work_on", v).apply()
    var callGreeting: String
        get() = sp.getString("call_greeting", "Hello, this is Jarvis, the owner's assistant. How can I help you?") ?: ""
        set(v) = sp.edit().putString("call_greeting", v).apply()
    var autoReplyTemplate: String
        get() = sp.getString("auto_reply_template", "") ?: ""
        set(v) = sp.edit().putString("auto_reply_template", v).apply()
    // ---- v1.2: Hosting / Publish via command ----
    var githubToken: String
        get() = sp.getString("github_token", "") ?: ""
        set(v) = sp.edit().putString("github_token", v.trim()).apply()
    var githubRepo: String
        get() = sp.getString("github_repo", "") ?: ""
        set(v) = sp.edit().putString("github_repo", v.trim()).apply()
    var githubBranch: String
        get() = sp.getString("github_branch", "main") ?: "main"
        set(v) = sp.edit().putString("github_branch", v.trim().ifEmpty { "main" }).apply()
    // ---- v1.2: Background video upload ----
    /** Upload endpoint (POST multipart, field "file"). Leave blank to use share-to-app upload flow. */
    var uploadUrl: String
        get() = sp.getString("upload_url", "") ?: ""
        set(v) = sp.edit().putString("upload_url", v.trim()).apply()
    var hostingPort: Int
        get() = sp.getInt("hosting_port", 8080)
        set(v) = sp.edit().putInt("hosting_port", v).apply()

    // ---- v1.3.9: gestures, mood, offline, theme, learning, sync, security, locks ----
    var gestureShakeEnabled: Boolean
        get() = sp.getBoolean("gesture_shake_enabled", false)
        set(v) = sp.edit().putBoolean("gesture_shake_enabled", v).apply()
    var moodDetectEnabled: Boolean
        get() = sp.getBoolean("mood_detect_enabled", true)
        set(v) = sp.edit().putBoolean("mood_detect_enabled", v).apply()
    var lastMood: String
        get() = sp.getString("last_mood", "") ?: ""
        set(v) = sp.edit().putString("last_mood", v).apply()
    var lastNuance: String
        get() = sp.getString("last_nuance", "") ?: ""
        set(v) = sp.edit().putString("last_nuance", v).apply()
    var offlineMode: Boolean
        get() = sp.getBoolean("offline_mode", false)
        set(v) = sp.edit().putBoolean("offline_mode", v).apply()
    var themeAccent: String
        get() = sp.getString("theme_accent", "#22D3EE") ?: "#22D3EE"
        set(v) = sp.edit().putString("theme_accent", v).apply()
    // v1.4.0: light / dark mode — "dark" (default) ba "light"
    var themeMode: String
        get() = sp.getString("theme_mode", "dark") ?: "dark"
        set(v) = sp.edit().putString("theme_mode", v).apply()
    var toolUsageJson: String
        get() = sp.getString("tool_usage_json", "{}") ?: "{}"
        set(v) = sp.edit().putString("tool_usage_json", v).apply()
    var schedulesJson: String
        get() = sp.getString("schedules_json", "[]") ?: "[]"
        set(v) = sp.edit().putString("schedules_json", v).apply()
    var encryptLocal: Boolean
        get() = sp.getBoolean("encrypt_local", true)
        set(v) = sp.edit().putBoolean("encrypt_local", v).apply()
    var safeMode: Boolean
        get() = sp.getBoolean("safe_mode", false)
        set(v) = sp.edit().putBoolean("safe_mode", v).apply()
    var crashCount: Int
        get() = sp.getInt("crash_count", 0)
        set(v) = sp.edit().putInt("crash_count", v).apply()
    var lastCrashSeen: Long
        get() = sp.getLong("last_crash_seen", 0L)
        set(v) = sp.edit().putLong("last_crash_seen", v).apply()
    var appPassphraseHash: String
        get() = sp.getString("app_passphrase_hash", "") ?: ""
        set(v) = sp.edit().putString("app_passphrase_hash", v).apply()
    var appLockEnabled: Boolean
        get() = sp.getBoolean("app_lock_enabled", false)
        set(v) = sp.edit().putBoolean("app_lock_enabled", v).apply()
    var lastSessionSummary: String
        get() = sp.getString("last_session_summary", "") ?: ""
        set(v) = sp.edit().putString("last_session_summary", v).apply()
    var styleJson: String
        get() = sp.getString("style_json", "{}") ?: "{}"
        set(v) = sp.edit().putString("style_json", v).apply()
    var bookmarksJson: String
        get() = sp.getString("bookmarks_json", "[]") ?: "[]"
        set(v) = sp.edit().putString("bookmarks_json", v).apply()
    var gestureMapJson: String
        get() = sp.getString("gesture_map_json", "{}") ?: "{}"
        set(v) = sp.edit().putString("gesture_map_json", v).apply()
}
