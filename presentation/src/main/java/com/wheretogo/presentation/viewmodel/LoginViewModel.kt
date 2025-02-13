package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.state.LoginScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
                _loginScreenState.value = _loginScreenState.value.copy(
                    error = exception.message
                )
                exception.printStackTrace()
            }
        }
    }

    fun signUpAndSignIn(authRequest: AuthRequest?) {
        viewModelScope.launch(exceptionHandler) {
            _loginScreenState.value = _loginScreenState.value.copy(isLoading = true)
            if(authRequest==null) {
                EventBus.sendMsg(EventMsg(R.string.login_cancel))
                _loginScreenState.value = _loginScreenState.value.run {
                    copy(
                        isLoading = false
                    )
                }
                return@launch
            }
            val result = withContext(Dispatchers.IO){ userSignUpAndSignInUseCase(authRequest) }
            when (result.status) {
                UseCaseResponse.Status.Success -> {
                    _loginScreenState.value = _loginScreenState.value.run {
                        copy(
                            isExit = true,
                            isLoading = false
                        )
                    }
                    EventBus.sendMsg(EventMsg(R.string.welcome_user, result.data?:"unknown"))
                }

                UseCaseResponse.Status.Fail -> {
                    _loginScreenState.value = _loginScreenState.value.run {
                        copy(
                            isLoading = false
                        )
                    }
                    EventBus.sendMsg(EventMsg(R.string.login_fail))
                }
            }
        }
    }

    fun signInPass() {
        viewModelScope.launch(exceptionHandler) {
            withContext(Dispatchers.IO){ userSignUpAndSignInUseCase.signInPass() }
            _loginScreenState.value = _loginScreenState.value.run {
                copy(isExit = true)
            }
        }
    }
}