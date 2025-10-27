package com.wheretogo.presentation.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.usecase.app.AppCheckBySignatureUseCase
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.ViewModelErrorHandler
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.state.RootScreenState
import com.wheretogo.presentation.toAppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val errorHandler: ViewModelErrorHandler,
    private val adService: AdService,
    private val appCheckBySignatureUseCase: AppCheckBySignatureUseCase
) :
    ViewModel() {
    private val _rootScreenState = MutableStateFlow(RootScreenState())
    val rootScreenState: StateFlow<RootScreenState> = _rootScreenState
    val snackbarHostState = SnackbarHostState()

    suspend fun handleError(error: Throwable) {
        errorHandler.handle(error.toAppError())
    }
    init {
        viewModelScope.launch {
            appCheckBySignatureUseCase().onSuccess {
                if (it)
                    EventBus.send(AppEvent.SnackBar(EventMsg(R.string.app_check_success)))
            }.onFailure {
                handleError(it)
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