package com.wheretogo.presentation.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.FcmMsg
import com.wheretogo.domain.handler.RootEvent
import com.wheretogo.domain.handler.RootHandler
import com.wheretogo.domain.usecase.app.AppCheckBySignatureUseCase
import com.wheretogo.domain.usecase.app.ObserveMsgUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.state.RootScreenState
import com.wheretogo.presentation.toAppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val handler: RootHandler,
    private val appCheckBySignatureUseCase: AppCheckBySignatureUseCase,
    private val userSignOutUseCase: UserSignOutUseCase,
    private val observeMsgUseCase: ObserveMsgUseCase,
) :
    ViewModel() {
    private val _rootScreenState = MutableStateFlow(RootScreenState())
    val rootScreenState: StateFlow<RootScreenState> = _rootScreenState
    val snackbarHostState = SnackbarHostState()

    suspend fun handleError(error: Throwable) {
        handler.handle(error.toAppError())
    }
    init {
        viewModelScope.launch {
            launch {
                appCheckBySignatureUseCase().onSuccess {
                    if (it)
                        handler.handle(RootEvent.APP_CHECK_SUCCESS)
                }.onFailure {
                    handleError(it)
                }
            }

            launch {
                observeMsgUseCase().collect { msg->
                    when(msg.type){
                        FcmMsg.BAN -> {
                            userSignOutUseCase()
                            handler.handle(RootEvent.ACCOUNT_VALID_EXPIRE, msg.data)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun setSignInScreenVisible(isVisible: Boolean){
        viewModelScope.launch {
            _rootScreenState.update {
                it.copy(isSignInScreenVisible = isVisible)
            }
        }
    }

    fun eventSend(event: AppEvent){
        when(event){
            else -> {}
        }
    }

    fun eventReceive(event: AppEvent, result: Boolean){
        when(event){
            else -> {}
        }
    }

}