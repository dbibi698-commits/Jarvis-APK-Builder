package com.jarvis.ai.core

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * v1.3.9 — Encrypted local storage: the user's memory + chat history are
 * encrypted on the phone with AES-256-GCM. The key lives in the Android
 * Keystore (hardware-backed on most phones) and never leaves the device.
 * No new libraries — pure Android framework. If decryption fails (e.g. the
 * key was wiped), callers fall back to reading plain text, so old data is
 * never lost.
 */
object SecureStore {
    private const val ALIAS = "jarvis-secure-key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey?.let { return it }
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        kg.init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build())
        return kg.generateKey()
    }

    /** Encrypts to Base64 (IV + ciphertext). Returns null on failure. */
    fun encrypt(plain: String): String? = runCatching {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, key())
        val iv = c.iv // 12 bytes
        val out = c.doFinal(plain.toByteArray(Charsets.UTF_8))
        Base64.encodeToString(iv + out, Base64.NO_WRAP)
    }.getOrNull()

    /** Decrypts a value produced by encrypt(). Returns null on failure. */
    fun decrypt(encoded: String): String? = runCatching {
        val bytes = Base64.decode(encoded.trim(), Base64.NO_WRAP)
        if (bytes.size < 13) return null
        val iv = bytes.copyOfRange(0, 12)
        val cipherText = bytes.copyOfRange(12, bytes.size)
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        String(c.doFinal(cipherText), Charsets.UTF_8)
    }.getOrNull()

    /** True when this looks like an encrypted value (Base64 that decrypts). */
    fun looksEncrypted(s: String): Boolean {
        val t = s.trim()
        return t.length > 20 && !t.startsWith("[") && !t.startsWith("{") && decrypt(t) != null
    }
}
