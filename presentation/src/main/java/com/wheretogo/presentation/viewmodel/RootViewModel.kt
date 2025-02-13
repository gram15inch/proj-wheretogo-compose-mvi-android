package com.wheretogo.presentation.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.presentation.state.RootScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {
    private val _mainScreenState = MutableStateFlow(RootScreenState())
    val rootScreenState: StateFlow<RootScreenState> = _mainScreenState
    val snackbarHostState = SnackbarHostState()

    init {
        viewModelScope.launch {
            userRepository.isRequestLoginStream().collect {
                _mainScreenState.value = _mainScreenState.value.run {
                    copy(isRequestLogin = it)
                }
            }
        }
    }

}