package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.usecase.user.DeleteUserUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.presentation.AdLifecycle
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SettingInfoType
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.intent.SettingIntent
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.state.SettingScreenState
import com.wheretogo.presentation.toItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val signOutUseCase: UserSignOutUseCase,
    private val getUserProfileStreamUseCase: GetUserProfileStreamUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val adService: AdService
) : ViewModel() {
    private val _settingScreenState = MutableStateFlow(SettingScreenState())
    val settingScreenState: StateFlow<SettingScreenState> = _settingScreenState
    private var _loadAdSkip = false
    private var _screenLifecycleSkip = false

    init {
        profileInit()
        adInit()
    }

    override fun onCleared() {
        super.onCleared()
        _settingScreenState.value.destroyAd()
    }

    fun handleIntent(intent: SettingIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingIntent.UserDeleteClick -> userDeleteClick()
                is SettingIntent.LogoutClick -> logoutClick()
                is SettingIntent.InfoClick -> infoClick(intent.settingInfoType)
                is SettingIntent.UsernameChangeClick -> usernameChangeClick()
                is SettingIntent.DialogAnswer -> dialogAnswer(intent.answer)

                //공통
                is SettingIntent.LifecycleChange -> lifecycleChange(intent.event)
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

    private fun lifecycleChange(event:AppLifecycle){
        if(!_screenLifecycleSkip){
            when (event) {
                AppLifecycle.onResume->{
                    if(!_loadAdSkip) {
                        viewModelScope.launch(Dispatchers.IO) {
                            delay(50)
                            loadAd()
                        }
                    }

                    _loadAdSkip = false
                }

                AppLifecycle.onPause->{
                    clearAd()
                }

                AppLifecycle.onDispose->{
                    viewModelScope.launch {
                        clearAd()
                    }
                }

                else->{}
            }
        }
    }

    // 초기화
    private fun profileInit(){
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

    private fun adInit(){
        viewModelScope.launch(Dispatchers.IO) {
            _loadAdSkip = true
            launch {
                delay(350)
                loadAd()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            adService.adLifeCycle.collect{
                when(it){
                    AdLifecycle.onPause-> {
                        _screenLifecycleSkip = true
                        clearAd()
                    }
                    AdLifecycle.onResume-> {
                        _screenLifecycleSkip= false
                        delay(150)
                        loadAd()
                    }
                }
            }
        }
    }

    // 유틸
    private fun loadAd(){
        viewModelScope.launch(Dispatchers.IO){
            adService.getAd()
                .onSuccess { newAdGroup->
                if(!_screenLifecycleSkip)
                    _settingScreenState.update {
                        it.copy(
                            adItemGroup = newAdGroup.toItem()
                        )
                    }
                else
                    clearAd()
            }.onFailure {
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.network_error)))
            }
        }
    }

    private fun clearAd(){
        viewModelScope.launch(Dispatchers.IO) {
            _settingScreenState.value.destroyAd()
            _settingScreenState.update {
                it.copy(adItemGroup = emptyList())
            }
        }
    }

    private fun SettingScreenState.destroyAd(){
        adItemGroup.forEach {
            it.nativeAd.destroy()
        }
    }

}