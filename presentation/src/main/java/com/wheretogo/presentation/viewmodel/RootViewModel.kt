package com.wheretogo.presentation.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.presentation.AdLifecycle
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.state.RootScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val adService: AdService
) :
    ViewModel() {
    private val _rootScreenState = MutableStateFlow(RootScreenState())
    val rootScreenState: StateFlow<RootScreenState> = _rootScreenState
    val snackbarHostState = SnackbarHostState()
    private val isRequestLoginState = userRepository.isRequestLoginStream().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3000),
        false,
    )
    init {
        viewModelScope.launch(Dispatchers.IO) {
            isRequestLoginState.collect {
                val event = if(it) AdLifecycle.onPause else AdLifecycle.onResume
                adService.lifeCycleChange(event = event)
                _rootScreenState.value = _rootScreenState.value.run {
                    copy(isRequestLogin = it)
                }
            }
        }
    }

}