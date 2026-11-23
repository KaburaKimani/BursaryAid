
package com.example.bursaryaid.feature.auth.logic

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    fun hash(password: String): String {

        require(password.isNotEmpty()) {
            "Password cannot be empty."
        }

        val salt = ByteArray(SALT_LENGTH).also {
            SecureRandom().nextBytes(it)
        }

        val passwordHash = pbkdf2(
            password,
            salt,
            ITERATIONS
        )

        return listOf(
            "pbkdf2",
            ITERATIONS.toString(),
            Base64.getEncoder().encodeToString(salt),
            Base64.getEncoder().encodeToString(passwordHash)
        ).joinToString("$")
    }

    fun verify(
        password: String,
        stored: String
    ): Boolean {

        if (password.isEmpty()) return false

        return if (stored.startsWith("pbkdf2$")) {
            verifyPbkdf2(password, stored)
        } else {
            verifyLegacy(password, stored)
        }
    }

    fun needsUpgrade(stored: String): Boolean {
        return !stored.startsWith("pbkdf2$")
    }

    private fun verifyPbkdf2(
        password: String,
        stored: String
    ): Boolean {

        return try {

            val parts = stored.split("$")

            if (parts.size != 4) return false

            val iterations = parts[1].toInt()

            if (iterations !in 1..1_000_000) return false

            val salt = Base64.getDecoder().decode(parts[2])

            val expected = Base64.getDecoder().decode(parts[3])

            if (salt.size < SALT_LENGTH || expected.size != 32) {
                return false
            }

            val actual = pbkdf2(
                password,
                salt,
                iterations
            )

            MessageDigest.isEqual(expected, actual)

        } catch (e: Exception) {
            false
        }
    }

    // Existing repository format: salt:SHA256(salt + password)
    private fun verifyLegacy(
        password: String,
        stored: String
    ): Boolean {

        return try {

            val parts = stored.split(":")

            if (parts.size != 2) return false

            val salt = fromHex(parts[0])

            val expected = parts[1]

            val actual = sha256(salt, password)

            MessageDigest.isEqual(
                expected.toByteArray(Charsets.UTF_8),
                actual.toByteArray(Charsets.UTF_8)
            )

        } catch (e: Exception) {
            false
        }
    }

    private fun pbkdf2(
        password: String,
        salt: ByteArray,
        iterations: Int
    ): ByteArray {

        val specification = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            KEY_LENGTH
        )

        return try {

            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(specification)
                .encoded

        } finally {

            specification.clearPassword()
        }
    }

    private fun sha256(
        salt: ByteArray,
        password: String
    ): String {

        val digest = MessageDigest.getInstance("SHA-256")

        digest.update(salt)

        return toHex(
            digest.digest(password.toByteArray(Charsets.UTF_8))
        )
    }

    private fun toHex(bytes: ByteArray): String =
        bytes.joinToString("") {
            "%02x".format(it)
        }

    private fun fromHex(hex: String): ByteArray {

        require(hex.isNotEmpty() && hex.length % 2 == 0)

        return hex.chunked(2).map {
            it.toInt(16).toByte()
        }.toByteArray()
    }
}
