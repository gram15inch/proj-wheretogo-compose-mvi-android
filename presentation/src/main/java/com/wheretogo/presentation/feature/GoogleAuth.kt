package com.wheretogo.presentation.feature

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.wheretogo.domain.AuthType
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.model.auth.AuthToken


suspend fun googleAuthOnDevice(idOption: GetGoogleIdOption, context: Context): Result<AuthRequest> {
    return runCatching {
        val credentialManager = CredentialManager.create(context)
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(idOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )
        AuthRequest(
            authType = AuthType.TOKEN,
            authToken = getGoogleIdTokenCredential(result.credential)?.toAuthToken()
        )
    }
}

private fun getGoogleIdTokenCredential(credential: Credential): GoogleIdTokenCredential? {
    return when (credential.type) {
        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
            GoogleIdTokenCredential.createFrom(credential.data)
        }

        else -> null
    }
}

private fun GoogleIdTokenCredential.toAuthToken(): AuthToken {
    return AuthToken(
        idToken = idToken,
        mail = id,
    )
}