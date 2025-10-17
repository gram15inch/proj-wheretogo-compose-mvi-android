package com.wheretogo.domain.feature

import java.security.MessageDigest

fun hashSha256(string: String): String {
    val bytes = string.toByteArray(Charsets.UTF_8)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}