package com.wheretogo.data.feature


import android.content.Context
import android.content.pm.PackageManager
import com.wheretogo.data.model.payload.PublicPayload
import com.wheretogo.domain.feature.DataEncryptor
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class DataEncryptorImpl(private val context: Context) : DataEncryptor {

    private val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private val ALGORITHM = "RSA"
    private val KEY_SIZE = 2048


    override fun encryptRsaBase64(payload: String, publicPem: String): String {
        val publicKey = createPublicKey(publicPem)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
        return Base64.encode(encrypted)
    }

    override fun decryptRsaBase64(encryptedBase64: String, privatePem: String): String {
        val encryptedBytes = Base64.decode(encryptedBase64)

        val privateKey = createPrivateKey(privatePem)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }

    override fun generateRsaKeyPair(): Pair<String, String> {
        val keyPairGen = KeyPairGenerator.getInstance(ALGORITHM)
        keyPairGen.initialize(KEY_SIZE)
        val keyPair = keyPairGen.genKeyPair()

        val publicKeyBytes = keyPair.public.encoded
        val privateKeyBytes = keyPair.private.encoded

        val publicKeyPem = """
        -----BEGIN PUBLIC KEY-----
        ${Base64.encode(publicKeyBytes)}
        -----END PUBLIC KEY-----
        """.trimIndent()

        val privateKeyPem = """
        -----BEGIN PRIVATE KEY-----
        ${Base64.encode(privateKeyBytes)}
        -----END PRIVATE KEY-----
        """.trimIndent()

        return publicKeyPem to privateKeyPem
    }

    override fun generateSignature(expireIn: Long): String {
        val pm = context.packageManager
        val packageName = context.packageName
        val md = MessageDigest.getInstance("SHA-256")
        val sha256 = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = info.signingInfo!!
            val signatures = if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
            md.update(signatures[0].toByteArray())
            md.digest().joinToString(":") { "%02X".format(it) }
        } else {
            @Suppress("DEPRECATION")
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            md.update(info.signatures?.get(0)!!.toByteArray())
            md.digest().joinToString(":") { "%02X".format(it) }
        }
        return Json.encodeToString(
            PublicPayload(
                packageName = packageName,
                sha256 = sha256,
                expireAt = System.currentTimeMillis() + expireIn
            )
        )
    }


    private fun createPublicKey(pem: String): PublicKey {
        val sanitized = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
            .let { Base64.decode(it) }

        val spec = X509EncodedKeySpec(sanitized)
        return KeyFactory.getInstance(ALGORITHM).generatePublic(spec)
    }

    private fun createPrivateKey(pem: String): PrivateKey {
        val sanitized = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
            .let { Base64.decode(it) }

        val spec = PKCS8EncodedKeySpec(sanitized)
        return KeyFactory.getInstance(ALGORITHM).generatePrivate(spec)
    }
}