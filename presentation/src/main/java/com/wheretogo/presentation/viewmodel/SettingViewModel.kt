package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.usecase.user.DeleteUserUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.presentation.SettingInfoType
import com.wheretogo.presentation.intent.SettingIntent
import com.wheretogo.presentation.state.SettingScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val signOutUseCase: UserSignOutUseCase,
    private val getUserProfileStreamUseCase: GetUserProfileStreamUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {
    private val _settingScreenState = MutableStateFlow(SettingScreenState())
    val settingScreenState: StateFlow<SettingScreenState> = _settingScreenState

    fun handleIntent(intent: SettingIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingIntent.UserDeleteClick -> userDeleteClick()
                is SettingIntent.LogoutClick -> logoutClick()
                is SettingIntent.InfoClick -> infoClick(intent.settingInfoType)
                is SettingIntent.UsernameChangeClick -> usernameChangeClick()
                is SettingIntent.DialogAnswer -> dialogAnswer(intent.answer)
            }
        }
    }

    init {
        viewModelScope.launch {
            getUserProfileStreamUseCase().collect {
                when (it.status) {
                    UseCaseResponse.Status.Success -> {
                        _settingScreenState.value = _settingScreenState.value.run {
                            copy(
                                profile = it.data!!,
                                isProfile = true
                            )
                        }
                    }

                    else -> {
                        _settingScreenState.value = _settingScreenState.value.run {
                            copy(profile = Profile(), isProfile = false)
                        }
                    }
                }
            }
        }
    }

    private fun userDeleteClick() {
        _settingScreenState.value = _settingScreenState.value.run {
            copy(
                isLoading = false,
                isDialog = true
            )
        }
    }

    private suspend fun logoutClick() {
        withContext(Dispatchers.IO){ signOutUseCase() }
    }


    private suspend fun infoClick(type: SettingInfoType) {

    }

    private suspend fun usernameChangeClick() {

    }

    private suspend fun dialogAnswer(answer:Boolean) {
        if(answer){
            _settingScreenState.value = _settingScreenState.value.run {
                copy(
                    isLoading = true,
                    isDialog = true
                )
            }
            withContext(Dispatchers.IO){ deleteUserUseCase()}
        }
        _settingScreenState.value = _settingScreenState.value.run {
            copy(
                isLoading = false,
                isDialog = false
            )
        }
    }
}