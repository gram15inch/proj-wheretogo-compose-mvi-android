package com.wheretogo.domain.feature

interface DataEncryptor {
    fun encryptRsaBase64(payload: String, publicPem: String): String

    fun decryptRsaBase64(encryptedBase64: String, privatePem: String): String

    fun generateRsaKeyPair(): Pair<String, String>

    fun generateSignature(expireIn: Long): String
}