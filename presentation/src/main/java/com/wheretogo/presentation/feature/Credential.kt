package com.wheretogo.presentation.feature

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.wheretogo.presentation.BuildConfig
import java.security.MessageDigest
import java.util.UUID

suspend fun getGoogleCredential(context: Context): GetCredentialResponse? {
    return try {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID_KEY)
            .setAutoSelectEnabled(true)
            //.setNonce(generateNonce())
            .build()

        val credentialManager = CredentialManager.create(context)
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()


        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        result
    } catch (e: Exception) {
        null
    }
}

fun generateNonce(): String {
    val nonce = UUID.randomUUID().toString()
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(nonce.toByteArray())
        Base64.encodeToString(hash, Base64.NO_WRAP or Base64.URL_SAFE)
    } catch (e: Exception) {
        nonce
    }
}




