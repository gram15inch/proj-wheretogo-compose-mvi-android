package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.wheretogo.presentation.state.SettingScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SettingViewModel @Inject constructor() : ViewModel() {
    private val _settingScreenState = MutableStateFlow(SettingScreenState())
    val settingScreenState: StateFlow<SettingScreenState> = _settingScreenState


}