package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.R
import com.wheretogo.presentation.ViewModelErrorHandler
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg
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
    private val errorHandler: ViewModelErrorHandler,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val userSignUpAndSignInUseCase: UserSignUpAndSignInUseCase,
    val getGoogleIdOption: GetGoogleIdOption,
) : ViewModel() {
    private val _loginScreenState = MutableStateFlow(LoginScreenState())
    val loginScreenState: StateFlow<LoginScreenState> = _loginScreenState

    fun signUpAndSignIn(authRequest: Result<AuthRequest>) {
        viewModelScope.launch {
            _loginScreenState.update { it.copy(isLoading = true) }
            authRequest.onFailure {
                errorHandler.handle(it.toAppError())
                _loginScreenState.update { it.copy(isLoading = false) }
            }.onSuccess {
                val result =
                    withContext(Dispatchers.IO) { userSignUpAndSignInUseCase(it) }
                when (result.status) {
                    UseCaseResponse.Status.Success -> {
                        _loginScreenState.update { it.copy(isExit = true, isLoading = false) }
                        EventBus.result(AppEvent.SignIn,true)
                        EventBus.send(AppEvent.SnackBar(EventMsg(R.string.welcome_user, result.data ?: "unknown")))
                    }

                    UseCaseResponse.Status.Fail -> {
                        _loginScreenState.update { it.copy(isLoading = false) }
                        EventBus.result(AppEvent.SignIn,false)
                        errorHandler.handle(result)
                    }
                }
            }
        }
    }

    fun signInPass() {
        viewModelScope.launch(dispatcher) {
            EventBus.result(AppEvent.SignIn,false)
            _loginScreenState.update { it.copy(isExit = true) }
        }
    }
}