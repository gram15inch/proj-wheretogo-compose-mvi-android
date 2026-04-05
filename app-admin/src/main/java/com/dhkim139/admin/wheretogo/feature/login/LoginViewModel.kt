package com.dhkim139.admin.wheretogo.feature.login

import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.AuthType
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.model.auth.SignToken
import com.wheretogo.domain.repository.AuthRepository
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false,
    val userName: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signUpAndSignInUseCase: UserSignUpAndSignInUseCase,
    private val getCredentialRequest: GetCredentialRequest,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun buildCredentialRequest() = getCredentialRequest

    fun handleCredentialResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            _uiState.update { it.copy(errorMessage = "지원하지 않는 로그인 방식입니다.") }
            return
        }

        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
        signIn(googleCredential.idToken)
    }

    private fun signIn(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val authRequest = AuthRequest(
                authType = AuthType.TOKEN,
                signToken = SignToken(
                    token = idToken,
                    authCompany = AuthCompany.GOOGLE
                ),
            )

            signUpAndSignInUseCase(authRequest)
                .onSuccess { userName ->

                    _uiState.update {
                        it.copy(isLoading = false, isSignedIn = true, userName = userName)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "로그인 실패")
                    }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
