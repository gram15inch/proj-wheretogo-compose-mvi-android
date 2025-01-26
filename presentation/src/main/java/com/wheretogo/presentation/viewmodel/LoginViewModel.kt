package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.presentation.state.LoginScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSignUpAndSignInUseCase: UserSignUpAndSignInUseCase,
    private val userProfileUseCase: GetUserProfileStreamUseCase
) :
    ViewModel() {
    private val _loginScreenState = MutableStateFlow(LoginScreenState())
    val loginScreenState: StateFlow<LoginScreenState> = _loginScreenState
    private val _toastShare = MutableSharedFlow<Boolean>()
    val toastShare : SharedFlow<Boolean> = _toastShare

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

    fun signUpAndSignIn() {
        viewModelScope.launch(exceptionHandler) {
            _loginScreenState.value = _loginScreenState.value.copy(isLoading = true)
            val result = userSignUpAndSignInUseCase()
            when (result.status) {
                UseCaseResponse.Status.Success -> {
                    val profile = userProfileUseCase().first()
                    _loginScreenState.value = _loginScreenState.value.run {
                        copy(
                            isExit = true,
                            isLoading = false,
                            toastMsg = "반갑습니다 ${profile.public.name}님."
                        )
                    }
                    _toastShare.emit(true)
                }

                UseCaseResponse.Status.Fail -> {
                    _loginScreenState.value = _loginScreenState.value.run {
                        copy(
                            isExit = false,
                            isLoading = false,
                            toastMsg = "로그인 실패"
                        )
                    }
                    _toastShare.emit(true)
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
}