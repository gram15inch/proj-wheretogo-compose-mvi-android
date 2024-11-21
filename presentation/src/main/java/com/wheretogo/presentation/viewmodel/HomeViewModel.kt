package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.usecase.UserSignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val userSignOutUseCase: UserSignOutUseCase) :
    ViewModel() {

    fun settingClick() {
        viewModelScope.launch {
            userSignOutUseCase()
        }
    }
}