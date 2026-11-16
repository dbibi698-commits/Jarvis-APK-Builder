package com.jarvis.ai.core

import java.security.MessageDigest

/**
 * v1.3.9 — Custom passphrase protection for app entry (optional, OFF by default).
 * The passphrase is never stored in plain text — only its SHA-256 hash lives
 * in Prefs. If the user forgets it, the only way back in is clearing the app's
 * data (Settings > Apps > Jarvis > Storage > Clear data) — that is stated in
 * the Settings screen so nobody is surprised.
 */
object PassLock {
    private const val SALT = "jarvis-lock-v1"

    fun hash(passphrase: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((SALT + passphrase.trim()).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(passphrase: String): Boolean {
        val stored = Prefs.appPassphraseHash
        if (stored.isBlank()) return false
        return runCatching {
            MessageDigest.isEqual(hash(passphrase).toByteArray(), stored.toByteArray())
        }.getOrDefault(false)
    }

    fun isSet(): Boolean = Prefs.appPassphraseHash.isNotBlank()
}
