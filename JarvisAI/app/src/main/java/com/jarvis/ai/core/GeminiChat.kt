package com.jarvis.ai.core

import android.content.Context
import android.util.Base64
import com.jarvis.ai.tools.ToolDefs
import com.jarvis.ai.tools.ToolExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Text chat agent: Gemini (latest model) with function calling, loops until the phone task is done. */
class GeminiChat(private val ctx: Context) {
    companion object {
        val http: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS).build()
        const val BASE = "https://generativelanguage.googleapis.com/v1beta"
        private const val MAX_STEPS = 30

        /** One-shot text generation (used for auto replies). */
        fun quick(prompt: String, system: String, model: String = Prefs.DEFAULT_FAST_MODEL): String? = runCatching {
            val body = JSONObject()
                .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
                .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", prompt)))))
            val req = Request.Builder().url("$BASE/models/$model:generateContent")
                .header("x-goog-api-key", Prefs.apiKey)
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
            http.newCall(req).execute().use { r ->
                val j = JSONObject(r.body!!.string())
                j.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                    .let { p -> (0 until p.length()).mapNotNull { p.getJSONObject(it).optString("text").takeIf { t -> t.isNotBlank() && !p.getJSONObject(it).optBoolean("thought") } }.joinToString("") }
            }
        }.getOrNull()
    }

    private val tools = ToolExecutor(ctx)
    private val contents = JSONArray()

    init {
        // Seed with recent history so Jarvis remembers the conversation across restarts.
        // v1.3: skip consecutive duplicate texts (old history may contain repeats).
        var last = ""
        var lastText = ""
        Memory.history().filter { it.mode == "chat" || it.mode == "voice" }.takeLast(12).forEach {
            val role = if (it.role == "user") "user" else "model"
            if (it.text == lastText) return@forEach
            lastText = it.text
            if (role == last) {
                val parts = contents.getJSONObject(contents.length() - 1).getJSONArray("parts")
                parts.put(JSONObject().put("text", it.text))
            } else contents.put(JSONObject().put("role", role).put("parts", JSONArray().put(JSONObject().put("text", it.text))))
            last = role
        }
        while (contents.length() > 0 && contents.getJSONObject(0).getString("role") != "user") contents.remove(0)
        if (contents.length() > 0 && contents.getJSONObject(contents.length() - 1).getString("role") == "user") contents.remove(contents.length() - 1)
    }

    fun reset() { while (contents.length() > 0) contents.remove(0) }

    suspend fun send(userText: String, image: ByteArray? = null, onStep: (String) -> Unit = {}, onToken: (String) -> Unit = {}): String = withContext(Dispatchers.IO) {
        // v1.3: multi-provider chat (Settings > AI API Keys). OpenAI/Claude reply
        // as smart text chat; full phone control with tools needs Gemini.
        if (Prefs.chatProvider == "openai") return@withContext sendPlainOpenAI(userText)
        if (Prefs.chatProvider == "claude") return@withContext sendPlainClaude(userText)
        if (Prefs.apiKey.isBlank()) return@withContext "Settings-এ গিয়ে আপনার Gemini API key দিন। (Settings → Gemini API Key)"
        val parts = JSONArray().put(JSONObject().put("text", userText))
        image?.let { parts.put(inline(it)) }
        contents.put(JSONObject().put("role", "user").put("parts", parts))

        var step = 0
        while (step++ < MAX_STEPS) {
            val resp = try { call(onToken) } catch (e: Exception) { return@withContext "Network error: ${e.message}" }
            val err = resp.optJSONObject("error")
            if (err != null) {
                contents.remove(contents.length() - 1)
                return@withContext "Gemini error (${err.optInt("code")}): ${err.optString("message")}"
            }
            val cand = resp.optJSONArray("candidates")?.optJSONObject(0)
                ?: return@withContext "No response (${resp.optJSONObject("promptFeedback")?.optString("blockReason") ?: "empty"})"
            val content = cand.optJSONObject("content") ?: JSONObject().put("role", "model").put("parts", JSONArray())
            if (!content.has("role")) content.put("role", "model")
            contents.put(content) // keep all parts (incl. thought signatures) exactly as returned
            val mParts = content.optJSONArray("parts") ?: JSONArray()

            val calls = (0 until mParts.length()).mapNotNull { mParts.getJSONObject(it).optJSONObject("functionCall") }
            if (calls.isEmpty()) {
                val text = (0 until mParts.length()).map { mParts.getJSONObject(it) }
                    .filter { !it.optBoolean("thought") }.joinToString("") { it.optString("text") }.trim()
                stripOldImages()
                return@withContext text.ifEmpty { "✓" }
            }

            val respParts = JSONArray()
            val images = mutableListOf<ByteArray>()
            for (c in calls) {
                val name = c.getString("name")
                onStep(name)
                val r = tools.run(name, c.optJSONObject("args") ?: JSONObject())
                val fr = JSONObject().put("name", name).put("response", r.data)
                c.optString("id").takeIf { it.isNotEmpty() }?.let { fr.put("id", it) }
                respParts.put(JSONObject().put("functionResponse", fr))
                r.imageJpeg?.let { images.add(it) }
            }
            images.forEach { respParts.put(inline(it)) }
            contents.put(JSONObject().put("role", "user").put("parts", respParts))
        }
        "I stopped after $MAX_STEPS steps. Tell me if I should continue."
    }

    private fun inline(jpeg: ByteArray) = JSONObject().put("inlineData",
        JSONObject().put("mimeType", "image/jpeg").put("data", Base64.encodeToString(jpeg, Base64.NO_WRAP)))

    /** Keep only the most recent screenshot to stay fast and cheap. */
    private fun stripOldImages() {
        for (i in 0 until contents.length() - 2) {
            val p = contents.getJSONObject(i).optJSONArray("parts") ?: continue
            for (k in 0 until p.length()) if (p.getJSONObject(k).has("inlineData")) p.put(k, JSONObject().put("text", "[screenshot]"))
        }
        while (contents.length() > 60) { contents.remove(0); while (contents.length() > 0 && contents.getJSONObject(0).getString("role") != "user") contents.remove(0) }
    }

    /** Recent chat history as OpenAI-style messages (deduped, last 12). */
    private fun recentMessages(userText: String): JSONArray {
        val msgs = JSONArray()
        var lastText = ""
        Memory.history().filter { it.mode == "chat" || it.mode == "voice" }.takeLast(12).forEach {
            if (it.text == lastText) return@forEach
            lastText = it.text
            val role = if (it.role == "user") "user" else "assistant"
            if (msgs.length() > 0) {
                val prev = msgs.getJSONObject(msgs.length() - 1)
                if (prev.getString("role") == role) { prev.put("content", prev.getString("content") + "\n" + it.text); return@forEach }
            }
            msgs.put(JSONObject().put("role", role).put("content", it.text))
        }
        msgs.put(JSONObject().put("role", "user").put("content", userText))
        while (msgs.length() > 0 && msgs.getJSONObject(0).getString("role") != "user") msgs.remove(0)
        return msgs
    }

    /** v1.3: plain text chat via OpenAI (no phone tools — tools need Gemini). */
    private fun sendPlainOpenAI(userText: String): String {
        if (Prefs.openaiKey.isBlank()) return "Settings-এ গিয়ে আপনার OpenAI API key দিন। (Settings → AI API Keys → OpenAI)"
        return runCatching {
            val msgs = JSONArray().put(JSONObject().put("role", "system").put("content", Persona.systemPrompt(false)))
            val hist = recentMessages(userText)
            for (i in 0 until hist.length()) msgs.put(hist.getJSONObject(i))
            val body = JSONObject().put("model", Prefs.openaiModel).put("messages", msgs).put("temperature", 0.7)
            val req = Request.Builder().url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer ${Prefs.openaiKey}")
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
            http.newCall(req).execute().use { r ->
                val j = JSONObject(r.body?.string() ?: "{}")
                j.optJSONObject("error")?.let { return "OpenAI error: ${it.optString("message")}" }
                j.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content").trim()
                    .ifEmpty { "✓" }
            }
        }.getOrElse { "Network error: ${it.message}" }
    }

    /** v1.3: plain text chat via Claude (no phone tools — tools need Gemini). */
    private fun sendPlainClaude(userText: String): String {
        if (Prefs.claudeKey.isBlank()) return "Settings-এ গিয়ে আপনার Claude API key দিন। (Settings → AI API Keys → Claude)"
        return runCatching {
            val hist = recentMessages(userText)
            val body = JSONObject().put("model", Prefs.claudeModel).put("max_tokens", 1024)
                .put("system", Persona.systemPrompt(false)).put("messages", hist)
            val req = Request.Builder().url("https://api.anthropic.com/v1/messages")
                .header("x-api-key", Prefs.claudeKey).header("anthropic-version", "2023-06-01")
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
            http.newCall(req).execute().use { r ->
                val j = JSONObject(r.body?.string() ?: "{}")
                j.optJSONObject("error")?.let { return "Claude error: ${it.optJSONObject("error")?.optString("message")}" }
                val parts = j.optJSONArray("content") ?: return "✓"
                (0 until parts.length()).mapNotNull { parts.getJSONObject(it).optString("text").takeIf { t -> t.isNotBlank() } }.joinToString("").trim().ifEmpty { "✓" }
            }
        }.getOrElse { "Network error: ${it.message}" }
    }

    private fun call(onToken: (String) -> Unit = {}): JSONObject {
        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", Persona.systemPrompt(false)))))
            .put("contents", contents)
            .put("tools", JSONArray().put(JSONObject().put("functionDeclarations", ToolDefs.declarations)))
        // v1.3.5 Turbo: streaming (alt=sse) — words arrive as Gemini generates them.
        val req = Request.Builder().url("$BASE/models/${Prefs.chatModel}:streamGenerateContent?alt=sse")
            .header("x-goog-api-key", Prefs.apiKey)
            .post(body.toString().toRequestBody("application/json".toMediaType())).build()
        val merged = JSONObject()
        val parts = JSONArray()
        val textSoFar = StringBuilder()
        val callsByName = LinkedHashMap<String, JSONObject>()
        http.newCall(req).execute().use { r ->
            val src = r.body?.source() ?: return JSONObject()
            try {
                while (!src.exhausted()) {
                    val line = src.readUtf8Line() ?: break
                    if (!line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
                    if (data.isEmpty() || data == "[DONE]") continue
                    val j = runCatching { JSONObject(data) }.getOrNull() ?: continue
                    j.optJSONObject("error")?.let { merged.put("error", it) }
                    val cand = j.optJSONArray("candidates")?.optJSONObject(0) ?: continue
                    val content = cand.optJSONObject("content") ?: continue
                    val ps = content.optJSONArray("parts") ?: continue
                    for (i in 0 until ps.length()) {
                        val p = ps.getJSONObject(i)
                        val fc = p.optJSONObject("functionCall")
                        if (fc != null) {
                            // merge streamed function-call args by name (later chunks complete them)
                            val name = fc.optString("name")
                            val prev = callsByName[name]
                            if (prev == null) callsByName[name] = fc
                            else {
                                val args = fc.optJSONObject("args")
                                if (args != null) {
                                    val pa = prev.optJSONObject("args") ?: JSONObject()
                                    val keys = args.keys()
                                    while (keys.hasNext()) { val k = keys.next(); pa.put(k, args.get(k)) }
                                    prev.put("args", pa)
                                }
                            }
                            continue
                        }
                        if (p.has("text") && !p.optBoolean("thought")) {
                            textSoFar.append(p.optString("text"))
                            onToken(textSoFar.toString())
                        }
                        parts.put(p)
                    }
                }
            } catch (e: Exception) {
                if (parts.length() == 0 && callsByName.isEmpty()) throw e
                // stream cut off mid-reply — return what we have
            }
        }
        callsByName.values.forEach { fc -> parts.put(JSONObject().put("functionCall", fc)) }
        val content = JSONObject().put("role", "model").put("parts", parts)
        merged.put("candidates", JSONArray().put(JSONObject().put("content", content)))
        return merged
    }
}
