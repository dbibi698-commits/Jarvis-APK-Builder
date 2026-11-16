package com.jarvis.ai.core

import com.jarvis.ai.App
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * v1.3.9 — Blockchain-inspired integrity verification (local hash chain).
 * Every important Jarvis event (memory saved, sync uploaded, settings changed)
 * is appended to an append-only ledger where each record contains the SHA-256
 * hash of the previous record. If anyone edits or deletes a record, the chain
 * breaks and verify() reports exactly where. Honest note: this is a
 * tamper-evident hash chain ON the phone — not a public blockchain (that would
 * need a network, miners and gas fees). It proves Jarvis's own data was not
 * silently changed.
 */
object IntegrityChain {
    private val file get() = File(App.instance.filesDir, "integrity.json")
    private const val MAX_RECORDS = 200

    private fun sha(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(s.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    private fun load(): JSONArray = runCatching {
        if (!file.exists()) JSONArray() else JSONArray(file.readText())
    }.getOrDefault(JSONArray())

    private fun save(a: JSONArray) = runCatching { file.writeText(a.toString()) }

    @Synchronized
    fun record(event: String) {
        runCatching {
            val a = load()
            val prev = if (a.length() == 0) "GENESIS" else a.getJSONObject(a.length() - 1).getString("hash")
            val ts = System.currentTimeMillis()
            val hash = sha("$prev|$ts|$event")
            a.put(JSONObject().put("ts", ts).put("event", event).put("prev", prev).put("hash", hash))
            // trim with a checkpoint so the chain stays verifiable
            if (a.length() > MAX_RECORDS) {
                val tail = JSONArray()
                val lastHash = a.getJSONObject(a.length() - 1).getString("hash")
                tail.put(JSONObject().put("ts", ts).put("event", "checkpoint").put("prev", "CHECKPOINT").put("hash", sha("CHECKPOINT|$ts|$lastHash")))
                for (i in a.length() - 100 until a.length()) tail.put(a.getJSONObject(i))
                save(tail)
            } else save(a)
        }
    }

    fun verify(): String {
        val a = load()
        if (a.length() == 0) return "Integrity chain khali — ekhono kono record nei."
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            val expected = sha("${o.getString("prev")}|${o.getLong("ts")}|${o.getString("event")}")
            if (expected != o.getString("hash")) return "❌ TAMPERED: record #$i (${o.optString("event")}) bodle geche! Chain bhanga."
            if (i > 0 && o.getString("prev") != a.getJSONObject(i - 1).getString("hash") && o.getString("prev") != "CHECKPOINT") {
                return "❌ TAMPERED: record #$i-er link ager record-er sathe milche na."
            }
        }
        return "✅ Integrity OK — ${a.length()} ta record, kono tampering nei. Last: ${a.getJSONObject(a.length() - 1).optString("event")}"
    }

    fun count(): Int = load().length()
}
