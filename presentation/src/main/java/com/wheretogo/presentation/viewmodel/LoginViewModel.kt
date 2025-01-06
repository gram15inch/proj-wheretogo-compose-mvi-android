package com.wheretogo.presentation.viewmodel

import androidx.credentials.Credential
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.wheretogo.domain.AUTH_COMPANY
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import com.wheretogo.domain.model.user.SignInRequest
import com.wheretogo.domain.model.user.SignUpRequest
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.presentation.state.LoginScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSignUpAndSignInUseCase: UserSignUpAndSignInUseCase
) :
    ViewModel() {
    private val _loginScreenState = MutableStateFlow(LoginScreenState())
    val loginScreenState: StateFlow<LoginScreenState> = _loginScreenState

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            else -> {
                _loginScreenState.value = _loginScreenState.value.copy(
                    error = exception.message
                )
                exception.printStackTrace()
            }
        }
    }

    fun signUpAndSignIn(result: GetCredentialResponse) {
        viewModelScope.launch(exceptionHandler) {
            val googleCredential = getGoogleIdTokenCredential(result.credential)
            if (googleCredential != null) {
                val signInRequest = SignInRequest(token = googleCredential.idToken)
                val signUpRequest = SignUpRequest(
                    profile = Profile(
                        public = ProfilePublic(
                            name = googleCredential.displayName ?: "익명의드라이버",
                        ),
                        private = ProfilePrivate(
                            mail = googleCredential.id,
                            authCompany = AUTH_COMPANY.GOOGLE.name,
                            lastVisited = System.currentTimeMillis(),
                            accountCreation = System.currentTimeMillis(),
                            isAdRemove = false
                        )

                    ),
                    token = googleCredential.idToken
                )
                val result = userSignUpAndSignInUseCase(signUpRequest, signInRequest)
                when (result.status) {

                    UseCaseResponse.Status.Success -> {

                        _loginScreenState.value = _loginScreenState.value.run {
                            copy(
                                isExit = true,
                                isToast = true,
                                toastMsg = "반갑습니다 ${googleCredential.displayName}님."
                            )
                        }
                    }

                    UseCaseResponse.Status.Fail -> {
                        _loginScreenState.value = _loginScreenState.value.run {
                            copy(
                                isExit = false,
                                isToast = true,
                                toastMsg = "로그인 실패"
                            )
                        }
                        _loginScreenState.value = _loginScreenState.value.run {
                            copy(isToast = false)
                        }
                    }

                    UseCaseResponse.Status.Error -> {
                        _loginScreenState.value = _loginScreenState.value.run {
                            copy(
                                isExit = false,
                                isToast = true,
                                toastMsg = "로그인 오류 ${result.msg}"
                            )
                        }
                        _loginScreenState.value = _loginScreenState.value.run {
                            copy(isToast = false)
                        }
                    }
                }
            }
        }
    }

    fun signInPass() {
        viewModelScope.launch(exceptionHandler) {
            userSignUpAndSignInUseCase.signInPass()
            _loginScreenState.value = _loginScreenState.value.run {
                copy(isExit = true)
            }
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
}