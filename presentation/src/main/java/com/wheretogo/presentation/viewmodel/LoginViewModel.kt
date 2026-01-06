package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.wheretogo.domain.handler.LoginEvent
import com.wheretogo.domain.handler.LoginHandler
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.state.LoginScreenState
import com.wheretogo.presentation.toAppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val handler: LoginHandler,
    private val userSignUpAndSignInUseCase: UserSignUpAndSignInUseCase,
    val getGoogleIdOption: GetGoogleIdOption,
) : ViewModel() {
    private val _loginScreenState = MutableStateFlow(LoginScreenState())
    val loginScreenState: StateFlow<LoginScreenState> = _loginScreenState

    suspend fun handleError(error: AppError) {
        when(handler.handle(error)){
            else -> {}
        }
    }

    fun signUpAndSignIn(authRequest: Result<AuthRequest>) {
        viewModelScope.launch {
            _loginScreenState.update { it.copy(isLoading = true) }
            authRequest.onFailure {
                handleError(it.toAppError())
                _loginScreenState.update { it.copy(isLoading = false) }
            }.onSuccess {
                val result =
                    withContext(Dispatchers.IO) { userSignUpAndSignInUseCase(it) }

                result.onSuccess { name->
                    _loginScreenState.update { it.copy(isExit = true, isLoading = false) }
                    handler.handle(LoginEvent.SIGN_IN_SUCCESS, name)
                }.onFailure {
                    _loginScreenState.update { it.copy(isLoading = false) }
                    handleError(it.toAppError())
                }
            }
        }
    }

    fun signInPass() {
        viewModelScope.launch(dispatcher) {
            handler.handle(LoginEvent.SIGN_IN_FAIL)
            _loginScreenState.update { it.copy(isExit = true) }
        }
    }
}