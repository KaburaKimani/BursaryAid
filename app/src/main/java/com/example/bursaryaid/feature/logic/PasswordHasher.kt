package com.example.bursaryaid.feature.auth.logic

import java.security.MessageDigest
import java.security.SecureRandom

/** Salted SHA-256. Stored as "salt:hash". Plain-text passwords are never saved. */
object PasswordHasher {

    fun hash(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "${toHex(salt)}:${sha256(salt, password)}"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        return sha256(fromHex(parts[0]), password) == parts[1]
    }

    private fun sha256(salt: ByteArray, password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return toHex(digest.digest(password.toByteArray(Charsets.UTF_8)))
    }

    private fun toHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun fromHex(hex: String): ByteArray =
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}