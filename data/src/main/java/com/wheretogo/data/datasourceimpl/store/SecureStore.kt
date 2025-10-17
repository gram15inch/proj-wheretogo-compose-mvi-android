package com.wheretogo.data.datasourceimpl.store

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStore(val context: Context) {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val securePrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val publicToken = "public_token"
    private val publicKey = "public_key"

    fun setPublicToken(token: String) {
        securePrefs.edit {
            putString(publicToken, token)
        }
    }

    fun getPublicToken(): String? {
        return securePrefs.getString(publicToken, null)
    }

    fun setPublicKey(key: String) {
        securePrefs.edit {
            putString(publicKey, key)
        }
    }

    fun getPublicKey(): String? {
        return securePrefs.getString(publicKey, null)
    }

    fun setCustomString(key: String, string: String) {
        securePrefs.edit {
            putString(key, string)
        }
    }

    fun getCustomString(key: String): String? {
        return securePrefs.getString(key, null)
    }
}