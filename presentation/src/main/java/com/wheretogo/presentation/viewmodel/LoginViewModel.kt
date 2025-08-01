package com.wheretogo.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.state.LoginScreenState
import com.wheretogo.presentation.toAppError
import com.wheretogo.presentation.toStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSignUpAndSignInUseCase: UserSignUpAndSignInUseCase,
    val getGoogleIdOption : GetGoogleIdOption
) : ViewModel() {
    private val _loginScreenState = MutableStateFlow(LoginScreenState())
    val loginScreenState: StateFlow<LoginScreenState> = _loginScreenState
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            else -> {
                _loginScreenState.update { it.copy(error = exception.message) }
                exception.printStackTrace()
            }
        }
    }

    fun signUpAndSignIn(authRequest: Result<AuthRequest>) {
        viewModelScope.launch(exceptionHandler) {
            _loginScreenState.update { it.copy(isLoading = true) }

            authRequest.onFailure {
                it.toAppError().toStringRes()?.let {
                    EventBus.send(AppEvent.SnackBar(EventMsg(it)))
                }
                _loginScreenState.update { it.copy(isLoading = false) }
            }.onSuccess {
                val result = withContext(Dispatchers.IO) { userSignUpAndSignInUseCase(it) }
                when (result.status) {
                    UseCaseResponse.Status.Success -> {
                        _loginScreenState.update {
                            it.copy(
                                isExit = true,
                                isLoading = false
                            )
                        }
                        EventBus.send(AppEvent.SnackBar(EventMsg(R.string.welcome_user, result.data ?: "unknown")))
                    }

                    UseCaseResponse.Status.Fail -> {
                        _loginScreenState.update { it.copy(isLoading = false) }
                        if (result.failType != null)
                            EventBus.send(AppEvent.SnackBar(EventMsg(result.failType!!.toStringRes())))
                        else
                            EventBus.send(AppEvent.SnackBar(EventMsg(R.string.login_fail)))
                    }
                }
            }
        }
    }

    fun signInPass() {
        viewModelScope.launch(exceptionHandler) {
            withContext(Dispatchers.IO){ userSignUpAndSignInUseCase.signInPass() }
            _loginScreenState.update { it.copy(isExit = true) }
        }
    }
}