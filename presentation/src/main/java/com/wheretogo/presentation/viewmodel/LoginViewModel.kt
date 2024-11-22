package com.wheretogo.presentation.viewmodel

import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.wheretogo.domain.usecase.UserSignInUseCase
import com.wheretogo.presentation.exceptions.MapNotInitializedException
import com.wheretogo.presentation.exceptions.UnexpectedTypeOfCredentialException
import com.wheretogo.presentation.state.LoginScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val userSignUseCase: UserSignInUseCase) :
    ViewModel() {
    private val _loginScreenState = MutableStateFlow(LoginScreenState())
    val loginScreenState: StateFlow<LoginScreenState> = _loginScreenState

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            is MapNotInitializedException -> {
                _loginScreenState.value = _loginScreenState.value.copy(
                    error = exception.message
                )
            }

            is GoogleIdTokenParsingException -> {

            }

            is UnexpectedTypeOfCredentialException -> {

            }

            else -> {
                exception.printStackTrace()
            }
        }
    }

    fun signIn(result: GetCredentialResponse) {
        viewModelScope.launch(exceptionHandler) {
            val credential = result.credential
            when (credential.type) {
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    userSignUseCase(googleIdTokenCredential.idToken)
                    _loginScreenState.value = _loginScreenState.value.run {
                        copy(isExit = true)
                    }
                }

                else -> {
                    throw UnexpectedTypeOfCredentialException()
                }
            }
        }
    }


    fun noSign() {
        viewModelScope.launch(exceptionHandler) {
            userSignUseCase.signPass()
        }

        _loginScreenState.value = _loginScreenState.value.run {
            copy(isExit = true)
        }
    }
}