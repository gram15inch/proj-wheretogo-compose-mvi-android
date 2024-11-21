package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.presentation.state.MainScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {
    private val _mainScreenState = MutableStateFlow(MainScreenState())
    val mainScreenState: StateFlow<MainScreenState> = _mainScreenState

    init {
        viewModelScope.launch {
            userRepository.isRequestLoginFlow().collect{
                _mainScreenState.value = _mainScreenState.value.run {
                    copy(isRequestLogin = it)
                }
            }
        }
    }

}