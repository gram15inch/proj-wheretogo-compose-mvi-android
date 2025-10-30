package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.usecase.user.DeleteUserUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.SettingInfoType
import com.wheretogo.presentation.ViewModelErrorHandler
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.intent.SettingIntent
import com.wheretogo.presentation.state.SettingScreenState
import com.wheretogo.presentation.toAppError
import com.wheretogo.presentation.toItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val errorHandler: ViewModelErrorHandler,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val signOutUseCase: UserSignOutUseCase,
    private val getUserProfileStreamUseCase: GetUserProfileStreamUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val adService: AdService
) : ViewModel() {
    private val _settingScreenState = MutableStateFlow(SettingScreenState())
    val settingScreenState: StateFlow<SettingScreenState> = _settingScreenState
    private var _loadAdSkipOnResume = false
    private var _isCoverScreen = false

    init {
        profileInit()
        adInit()
    }

    fun handleIntent(intent: SettingIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {
                is SettingIntent.EmptyProfileClick -> emptyProfileClick()
                is SettingIntent.UserDeleteClick -> userDeleteClick()
                is SettingIntent.LogoutClick -> logoutClick()
                is SettingIntent.InfoClick -> infoClick(intent.settingInfoType)
                is SettingIntent.UsernameChangeClick -> usernameChangeClick()
                is SettingIntent.DialogAnswer -> dialogAnswer(intent.answer)

                //공통
                is SettingIntent.LifecycleChange -> lifecycleChange(intent.event)
                is SettingIntent.EventReceive -> eventReceive(intent.event, intent.result)
            }
        }
    }

    suspend fun handleError(error: Throwable) {
        when(errorHandler.handle(error.toAppError())){
            is AppError.NeedSignIn->{
                _isCoverScreen = true
                clearAd()
                signOutUseCase()
            }
            else -> {}
        }
    }


    private suspend fun emptyProfileClick() {
        refreshProfile()
    }

    private fun userDeleteClick() {
        _settingScreenState.update {
            it.copy(
                isLoading = false,
                isDialog = true
            )
        }
    }

    private suspend fun logoutClick() {
        withContext(Dispatchers.IO) { signOutUseCase() }.onSuccess {
            _settingScreenState.update {
                it.copy(
                    isProfile = false,
                    profile = Profile()
                )
            }
        }
    }


    private suspend fun infoClick(type: SettingInfoType) {

    }

    private suspend fun usernameChangeClick() {

    }

    private suspend fun dialogAnswer(answer: Boolean) {
        if (answer) {
            _settingScreenState.update {
                it.copy(
                    isLoading = true,
                    isDialog = true,
                )
            }
            withContext(Dispatchers.IO) { deleteUserUseCase() }.onFailure {
                handleError(it)
            }.onSuccess {
                _settingScreenState.update {
                    it.copy(
                        isProfile = false,
                        profile = Profile()
                    )
                }
            }
        }
        _settingScreenState.update {
            it.copy(
                isLoading = false,
                isDialog = false
            )
        }
    }

    private fun lifecycleChange(event: AppLifecycle) {
        when (event) {
            AppLifecycle.onResume -> {
                if(_loadAdSkipOnResume) {
                    _loadAdSkipOnResume = false
                    return
                }

                if (_settingScreenState.value.adItemGroup.isEmpty()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        loadAd()
                    }
                }
            }

            AppLifecycle.onPause -> {
                clearAd()
            }

            else -> {}
        }
    }

    private suspend fun eventReceive(event: AppEvent, result: Boolean) {
        when (event) {
            AppEvent.SignInScreen -> {
                _isCoverScreen = false
                if(result) {
                    refreshProfile()
                }
                viewModelScope.launch(Dispatchers.IO){ loadAd() }
            }
            else -> {}
        }

    }


    // 초기화
    private fun profileInit() {
        viewModelScope.launch(dispatcher) {
            refreshProfile(false)
        }
    }

    private fun adInit() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                loadAd()
                _loadAdSkipOnResume = true
            }
        }
    }

    // 유틸
    private suspend fun loadAd() {
        if(_loadAdSkipOnResume || _isCoverScreen)
            return
        adService.getAd()
            .onSuccess { newAdGroup ->
                _settingScreenState.update {
                    it.copy(adItemGroup = newAdGroup.toItem())
                }
            }.onFailure {
                if(it !is AppError.NeedSignIn)
                    handleError(it)
            }
    }

    private fun clearAd() {
        viewModelScope.launch(Dispatchers.IO) {
            _settingScreenState.value.destroyAd()
            _settingScreenState.update {
                it.copy(adItemGroup = emptyList())
            }
        }
    }

    private fun SettingScreenState.destroyAd() {
        adItemGroup.forEach {
            it.nativeAd.destroy()
        }
    }

    private suspend fun refreshProfile(isRequestLogin: Boolean = true){
        getUserProfileStreamUseCase().first()
            .onSuccess { profile ->
                _settingScreenState.update {
                    it.copy(
                        profile = profile,
                        isProfile = true
                    )
                }
            }.onFailure {
                _settingScreenState.update {
                    it.copy(
                        profile = Profile(),
                        isProfile = false
                    )
                }

                if (!isRequestLogin && it is DomainError.UserInvalid)
                    return@onFailure
                handleError(it)
            }
    }

}