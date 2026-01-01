package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.usecase.app.GuideMoveStepUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.HomeBodyBtn
import com.wheretogo.presentation.HomeBodyBtnHighlight
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.ViewModelEvent
import com.wheretogo.presentation.ViewModelEventHandler
import com.wheretogo.presentation.intent.HomeIntent
import com.wheretogo.presentation.state.HomeScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    stateInit: HomeScreenState,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val guideMoveStepUseCase: GuideMoveStepUseCase,
    private val eventHandler: ViewModelEventHandler
) : ViewModel() {
    private val _uiState = MutableStateFlow(stateInit)
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        observe()
    }

    fun handleIntent(intent: HomeIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {
                is HomeIntent.BodyButtonClick -> buttonClick(intent.btn)
                is HomeIntent.LifeCycleChange -> lifecycleChange(intent.lifeCycle)
            }
        }
    }

    private suspend fun buttonClick(btn: HomeBodyBtn) {
        when (btn) {
            HomeBodyBtn.DRIVE -> {
                guideMoveStepUseCase(true)
                eventHandler.handle(ViewModelEvent.DRIVE_NAVIGATE)
            }

            HomeBodyBtn.COURSE_ADD -> {
                eventHandler.handle(ViewModelEvent.COURSE_ADD_NAVIGATE)
            }

            HomeBodyBtn.GUIDE -> {
                if (_uiState.value.guideState.tutorialStep == DriveTutorialStep.SKIP) {
                    eventHandler.handle(ViewModelEvent.GUIDE_START)
                    guideMoveStepUseCase.start()
                }
            }

            HomeBodyBtn.CREATER_REQUEST -> {
                // 컴포즈에서 url 직접 오픈
            }
        }
    }

    private suspend fun lifecycleChange(life: AppLifecycle) {
        when (life) {
            AppLifecycle.onResume -> {
                when (_uiState.value.guideState.tutorialStep) {
                    DriveTutorialStep.SKIP, DriveTutorialStep.MOVE_TO_COURSE -> {

                    }

                    else -> {
                        eventHandler.handle(ViewModelEvent.GUIDE_STOP)
                        guideMoveStepUseCase.skip()
                    }
                }
            }

            else -> {}
        }
    }

    private fun setTutorialUi(step: DriveTutorialStep) {
        _uiState.update {
            it.copy(
                guideState = it.guideState.copy(
                    tutorialStep = step
                )
            ).run {
                when (step) {
                    DriveTutorialStep.HOME_TO_DRIVE_CLICK -> {
                        copy(bodyBtnHighlight = HomeBodyBtnHighlight.DRIVE)
                    }

                    else -> {
                        copy(bodyBtnHighlight = HomeBodyBtnHighlight.NONE)
                    }
                }
            }
        }
    }

    private fun observe() {
        viewModelScope.launch(dispatcher) {
            observeSettingsUseCase()
                .collect { settings ->
                    settings.onSuccess { set ->
                        val step = set.tutorialStep
                        setTutorialUi(step)
                    }
                }
        }
    }
}