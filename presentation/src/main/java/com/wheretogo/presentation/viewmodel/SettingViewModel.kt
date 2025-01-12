package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.usecase.user.DeleteUserUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.presentation.InfoType
import com.wheretogo.presentation.intent.SettingIntent
import com.wheretogo.presentation.state.SettingScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val signOutUseCase: UserSignOutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {
    private val _settingScreenState = MutableStateFlow(SettingScreenState())
    val settingScreenState: StateFlow<SettingScreenState> = _settingScreenState

    fun handleIntent(intent: SettingIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingIntent.UserDeleteClick -> userDeleteClick()
                is SettingIntent.LogoutClick -> logoutClick()
                is SettingIntent.InfoClick -> infoClick(intent.infoType)
                is SettingIntent.UsernameChangeClick -> userDeleteClick()
            }
        }
    }

    init {
        viewModelScope.launch {
            getUserProfileUseCase().collect {
                _settingScreenState.value = _settingScreenState.value.run {
                    copy(
                        profile = it,
                        isProfile = it.uid.isNotEmpty()
                    )
                }
            }
        }
    }

    private suspend fun userDeleteClick() {
        deleteUserUseCase()
    }

    private suspend fun logoutClick() {
        signOutUseCase()
    }

    private suspend fun infoClick(type: InfoType) {

    }

    private suspend fun userNameChangeClick() {

    }


}