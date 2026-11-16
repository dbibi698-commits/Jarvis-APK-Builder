package com.jarvis.ai.core

import com.jarvis.ai.App
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Chat history (everything said, both modes) + long-term memory facts.
 * v1.3.9: stored ENCRYPTED on device (AES-256-GCM, key in Android Keystore)
 * when Prefs.encryptLocal is on (default). Old plain files keep working —
 * load() tries plain first, then decrypts. trimTo() lets SelfHeal keep the
 * history from growing into an out-of-memory crash. export*() give the plain
 * JSON for sync (the Keystore key is per-device, so the encrypted blob could
 * never be read on another phone).
 */
object Memory {
    data class Msg(val role: String, val text: String, val time: Long, val mode: String)

    private val histFile get() = File(App.instance.filesDir, "history.json")
    private val memFile get() = File(App.instance.filesDir, "memory.json")
    private val lock = Any()
    private var cache: MutableList<Msg>? = null

    // ---- encrypted file IO ----
    private fun readStored(f: File): String? {
        if (!f.exists()) return null
        val raw = runCatching { f.readText() }.getOrNull() ?: return null
        if (raw.isBlank()) return null
        val t = raw.trim()
        if (t.startsWith("[") || t.startsWith("{")) return t
        return SecureStore.decrypt(t) ?: t // try decrypt; fall back to plain text
    }

    private fun writeStored(f: File, plain: String) {
        val out = if (Prefs.encryptLocal) SecureStore.encrypt(plain) ?: plain else plain
        runCatching { f.writeText(out) }
    }

    fun history(): List<Msg> = synchronized(lock) { load().toList() }

    fun add(role: String, text: String, mode: String = "chat") {
        if (text.isBlank()) return
        synchronized(lock) {
            val l = load()
            // v1.3: never store the same message twice in a row — this is what
            // made Jarvis repeat the same reply again and again.
            val last = l.lastOrNull()
            if (last != null && last.role == role && last.text.trim() == text.trim()) return
            l.add(Msg(role, text.trim(), System.currentTimeMillis(), mode))
            while (l.size > 5000) l.removeAt(0)
            save(l)
        }
        listeners.forEach { runCatching { it() } }
    }

    val listeners = mutableListOf<() -> Unit>()

    fun clearHistory() = synchronized(lock) { cache = mutableListOf(); save(cache!!) }

    /** v1.3.9: keep only the newest n messages (self-healing). */
    fun trimTo(n: Int) = synchronized(lock) {
        val l = load()
        if (l.size > n) {
            val kept = l.takeLast(n).toMutableList()
            cache = kept
            save(kept)
        }
    }

    /** Plain JSON of the whole history (for sync export). */
    fun exportHistoryJson(): String = synchronized(lock) {
        val a = JSONArray()
        load().forEach { a.put(JSONObject().put("r", it.role).put("t", it.text).put("ts", it.time).put("m", it.mode)) }
        a.toString()
    }

    /** Plain JSON of the long-term facts (for sync export). */
    fun exportMemoryJson(): String = JSONArray(facts()).toString()

    private fun load(): MutableList<Msg> {
        cache?.let { return it }
        val l = mutableListOf<Msg>()
        runCatching {
            val text = readStored(histFile) ?: return@runCatching
            val a = JSONArray(text)
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                val m = Msg(o.getString("r"), o.getString("t"), o.getLong("ts"), o.optString("m", "chat"))
                // v1.3: drop consecutive duplicates left by older versions
                val prev = l.lastOrNull()
                if (prev != null && prev.role == m.role && prev.text == m.text) continue
                l.add(m)
            }
        }
        cache = l
        return l
    }

    private fun save(l: List<Msg>) {
        val a = JSONArray()
        l.forEach { a.put(JSONObject().put("r", it.role).put("t", it.text).put("ts", it.time).put("m", it.mode)) }
        writeStored(histFile, a.toString())
    }

    // ---- long-term memory ----
    fun facts(): List<String> = runCatching {
        val text = readStored(memFile) ?: return emptyList()
        val a = JSONArray(text); (0 until a.length()).map { a.getString(it) }
    }.getOrDefault(emptyList())

    fun remember(fact: String) {
        val f = facts().toMutableList()
        if (f.none { it.equals(fact, true) }) f.add(fact)
        writeStored(memFile, JSONArray(f).toString())
        IntegrityChain.record("memory saved: ${fact.take(60)}")
    }

    fun forget(keyword: String): Int {
        val f = facts()
        val keep = f.filterNot { it.contains(keyword, true) }
        writeStored(memFile, JSONArray(keep).toString())
        return f.size - keep.size
    }

    fun clearFacts() = memFile.delete()

    fun memoryBlock(): String {
        val f = facts()
        val p = Prefs.userProfile
        val sb = StringBuilder()
        if (p.isNotBlank()) sb.append("About the user: ").append(p).append('\n')
        if (f.isNotEmpty()) sb.append("Things you remember about the user:\n").append(f.joinToString("\n") { "- $it" })
        return sb.toString()
    }
}
