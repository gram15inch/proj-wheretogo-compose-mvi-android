package com.wheretogo.presentation.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.state.RootScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
            else -> {}
        }
    }

    fun eventReceive(event: AppEvent, result: Boolean){
        when(event){
            else -> {}
        }
    }

}