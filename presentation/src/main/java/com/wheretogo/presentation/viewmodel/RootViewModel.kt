package com.wheretogo.presentation.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.presentation.AdLifecycle
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.IoDispatcher
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.state.RootScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val adService: AdService
) :
    ViewModel() {
    private val _rootScreenState = MutableStateFlow(RootScreenState())
    val rootScreenState: StateFlow<RootScreenState> = _rootScreenState
    val snackbarHostState = SnackbarHostState()

    fun eventSend(event: AppEvent){
        when(event){
            is AppEvent.SignIn->{
                adService.lifeCycleChange(AdLifecycle.onPause)
            }
            else -> {}
        }
    }

    fun eventReceive(event: AppEvent, result: Boolean){
        when(event){
            is AppEvent.SignIn->{
                adService.lifeCycleChange(AdLifecycle.onResume)
            }
            else -> {}
        }
    }

}