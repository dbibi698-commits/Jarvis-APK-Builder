package com.jarvis.ai.core

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.util.Base64
import android.util.Log
import com.jarvis.ai.service.JarvisAccessibilityService
import com.jarvis.ai.service.ScreenCapture
import com.jarvis.ai.tools.ToolDefs
import com.jarvis.ai.tools.ToolExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.sqrt

/**
 * Real-time, human-like voice conversation using the Gemini Live API (native audio).
 * The microphone stays open the whole time: just talk, no button presses. Supports interruptions,
 * all phone-control tools and optional live screen sharing.
 */
class LiveSession(private val ctx: Context, private val listener: Listener) {

    interface Listener {
        fun onState(state: State) {}
        fun onUserText(text: String) {}
        fun onModelText(text: String) {}
        fun onLevel(level: Float) {}
        fun onError(msg: String) {}
    }

    enum class State { CONNECTING, LISTENING, SPEAKING, THINKING, STOPPED }

    companion object {
        private const val TAG = "JarvisLive"
        private const val WS = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
        private const val IN_RATE = 16000
        private const val OUT_RATE = 24000
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tools = ToolExecutor(ctx)
    private var ws: WebSocket? = null
    @Volatile private var ready = false
    @Volatile var running = false
        private set
    @Volatile private var muted = false
    private var resumeHandle: String? = null
    private var micJob: Job? = null
    private var screenJob: Job? = null
    private var record: AudioRecord? = null
    private var track: AudioTrack? = null
    private val userBuf = StringBuilder()
    private val modelBuf = StringBuilder()
    @Volatile private var lastAudioAt = 0L

    fun start() {
        if (running) return
        if (Prefs.apiKey.isBlank()) { listener.onError("Settings-এ Gemini API key দিন"); return }
        running = true
        initPlayer()
        connect()
    }

    fun stop() {
        running = false; ready = false
        micJob?.cancel(); screenJob?.cancel()
        runCatching { record?.stop() }; runCatching { record?.release() }; record = null
        runCatching { track?.stop() }; runCatching { track?.release() }; track = null
        runCatching { ws?.close(1000, "bye") }; ws = null
        flushTranscripts()
        listener.onState(State.STOPPED)
        scope.cancel()
    }

    fun setMuted(m: Boolean) { muted = m }

    /** v1.3.2: switch voice (multi-voice). Re-sends setup with the new voice if connected. */
    fun changeVoice(name: String) {
        Prefs.voice = name
        if (running && ws != null) send(setupMessage())
    }

    /** Send typed text into the live conversation. */
    fun sendText(text: String) {
        if (!ready) return
        Memory.add("user", text, "voice"); listener.onUserText(text)
        send(JSONObject().put("realtimeInput", JSONObject().put("text", text)))
    }

    // ------------------------------------------------------------------ connection
    private fun connect() {
        listener.onState(State.CONNECTING)
        val req = Request.Builder().url("$WS?key=${Prefs.apiKey}").build()
        ws = GeminiChat.http.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { webSocket.send(setupMessage().toString()) }
            override fun onMessage(webSocket: WebSocket, text: String) = handle(text)
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) = handle(bytes.utf8())
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "ws failure", t); ready = false
                if (running) { listener.onError("Connection lost, reconnecting…"); reconnect() }
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                ready = false
                if (code != 1000 && reason.isNotBlank()) listener.onError("Live closed: $reason")
                if (running) reconnect()
            }
        })
    }

    private fun reconnect() {
        micJob?.cancel(); screenJob?.cancel()
        scope.launch { delay(1200); if (running) connect() }
    }

    private fun setupMessage(): JSONObject {
        val speech = JSONObject().put("voiceConfig", JSONObject().put("prebuiltVoiceConfig", JSONObject().put("voiceName", Prefs.voice)))
        when (Prefs.language) { "bn" -> speech.put("languageCode", "bn-IN"); "hi" -> speech.put("languageCode", "hi-IN"); "en" -> speech.put("languageCode", "en-IN") }
        val setup = JSONObject()
            .put("model", "models/${Prefs.liveModel}")
            .put("generationConfig", JSONObject().put("responseModalities", JSONArray().put("AUDIO")).put("speechConfig", speech))
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", Persona.systemPrompt(true)))))
            .put("tools", JSONArray().put(JSONObject().put("functionDeclarations", ToolDefs.declarations)))
            .put("inputAudioTranscription", JSONObject())
            .put("outputAudioTranscription", JSONObject())
            .put("contextWindowCompression", JSONObject().put("slidingWindow", JSONObject()))
            .put("sessionResumption", JSONObject().apply { resumeHandle?.let { put("handle", it) } })
        return JSONObject().put("setup", setup)
    }

    private fun send(o: JSONObject) { ws?.send(o.toString()) }

    // ------------------------------------------------------------------ server messages
    private fun handle(raw: String) {
        val m = runCatching { JSONObject(raw) }.getOrNull() ?: return
        if (m.has("setupComplete")) {
            ready = true
            listener.onState(State.LISTENING)
            startMic()
            if (Prefs.liveScreenShare) startScreenStream()
            return
        }
        m.optJSONObject("sessionResumptionUpdate")?.let { u -> if (u.optBoolean("resumable")) u.optString("newHandle").takeIf { it.isNotEmpty() }?.let { resumeHandle = it } }
        m.optJSONObject("goAway")?.let { /* server will close soon; reconnect handled in onClosed */ }
        m.optJSONObject("toolCall")?.let { handleToolCall(it); return }
        val sc = m.optJSONObject("serverContent") ?: return

        sc.optJSONObject("inputTranscription")?.optString("text")?.let { if (it.isNotEmpty()) { userBuf.append(it); listener.onUserText(userBuf.toString()) } }
        sc.optJSONObject("outputTranscription")?.optString("text")?.let { if (it.isNotEmpty()) { modelBuf.append(it); listener.onModelText(modelBuf.toString()) } }

        if (sc.optBoolean("interrupted")) { track?.pause(); track?.flush(); track?.play(); listener.onState(State.LISTENING) }

        sc.optJSONObject("modelTurn")?.optJSONArray("parts")?.let { parts ->
            for (i in 0 until parts.length()) {
                val d = parts.getJSONObject(i).optJSONObject("inlineData") ?: continue
                if (d.optString("mimeType").startsWith("audio")) {
                    val pcm = Base64.decode(d.getString("data"), Base64.DEFAULT)
                    listener.onState(State.SPEAKING); lastAudioAt = System.currentTimeMillis()
                    track?.write(pcm, 0, pcm.size)
                }
            }
        }
        if (sc.optBoolean("turnComplete")) {
            flushTranscripts()
            scope.launch { delay(600); if (System.currentTimeMillis() - lastAudioAt > 500) listener.onState(State.LISTENING) }
        }
    }

    private fun flushTranscripts() {
        if (userBuf.isNotBlank()) Memory.add("user", userBuf.toString(), "voice")
        if (modelBuf.isNotBlank()) Memory.add("assistant", modelBuf.toString(), "voice")
        userBuf.clear(); modelBuf.clear()
    }

    private fun handleToolCall(tc: JSONObject) {
        val calls = tc.optJSONArray("functionCalls") ?: return
        listener.onState(State.THINKING)
        scope.launch {
            val responses = JSONArray()
            var image: ByteArray? = null
            for (i in 0 until calls.length()) {
                val c = calls.getJSONObject(i)
                val r = tools.run(c.getString("name"), c.optJSONObject("args") ?: JSONObject())
                if (r.imageJpeg != null) image = r.imageJpeg
                responses.put(JSONObject().put("id", c.optString("id")).put("name", c.getString("name")).put("response", r.data))
            }
            // Show the screenshot to the model as a video frame so it can "see" the screen.
            image?.let { sendFrame(it) }
            send(JSONObject().put("toolResponse", JSONObject().put("functionResponses", responses)))
        }
    }

    // ------------------------------------------------------------------ audio
    private fun initPlayer() {
        val min = AudioTrack.getMinBufferSize(OUT_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(OUT_RATE).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(maxOf(min * 4, OUT_RATE * 2)).setTransferMode(AudioTrack.MODE_STREAM).build()
        track?.play()
    }

    @SuppressLint("MissingPermission")
    private fun startMic() {
        micJob?.cancel()
        micJob = scope.launch {
            val chunk = IN_RATE / 25 * 2 // 40 ms of 16-bit audio
            if (record == null) {
                val min = AudioRecord.getMinBufferSize(IN_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                val r = AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, IN_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(min, chunk * 4))
                if (r.state != AudioRecord.STATE_INITIALIZED) { listener.onError("Microphone permission needed"); return@launch }
                if (AcousticEchoCanceler.isAvailable()) AcousticEchoCanceler.create(r.audioSessionId)?.enabled = true
                if (NoiseSuppressor.isAvailable()) NoiseSuppressor.create(r.audioSessionId)?.enabled = true
                record = r
                r.startRecording()
            }
            val buf = ByteArray(chunk)
            while (isActive && running) {
                val n = record?.read(buf, 0, buf.size) ?: break
                if (n <= 0) continue
                listener.onLevel(rms(buf, n))
                if (!ready || muted) continue
                send(JSONObject().put("realtimeInput", JSONObject().put("audio",
                    JSONObject().put("mimeType", "audio/pcm;rate=$IN_RATE").put("data", Base64.encodeToString(buf, 0, n, Base64.NO_WRAP)))))
            }
        }
    }

    private fun rms(b: ByteArray, n: Int): Float {
        var sum = 0.0; var i = 0
        while (i + 1 < n) { val s = (b[i].toInt() and 0xff) or (b[i + 1].toInt() shl 8); sum += s.toShort().toDouble() * s.toShort(); i += 2 }
        return (sqrt(sum / (n / 2)) / 6000.0).toFloat().coerceIn(0f, 1f)
    }

    // ------------------------------------------------------------------ screen
    private fun startScreenStream() {
        screenJob?.cancel()
        screenJob = scope.launch {
            while (isActive && running) {
                val jpg = if (ScreenCapture.active) ScreenCapture.latestJpeg(768) else JarvisAccessibilityService.instance?.screenshotJpeg(768)
                if (jpg != null && ready) sendFrame(jpg)
                delay(1500)
            }
        }
    }

    fun setScreenShare(on: Boolean) { Prefs.liveScreenShare = on; if (on && ready) startScreenStream() else screenJob?.cancel() }

    private fun sendFrame(jpg: ByteArray) {
        send(JSONObject().put("realtimeInput", JSONObject().put("video",
            JSONObject().put("mimeType", "image/jpeg").put("data", Base64.encodeToString(jpg, Base64.NO_WRAP)))))
    }
}
