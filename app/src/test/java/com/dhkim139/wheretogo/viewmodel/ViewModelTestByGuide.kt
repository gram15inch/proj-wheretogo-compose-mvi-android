package com.dhkim139.wheretogo.viewmodel

import app.cash.turbine.test
import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.handler.DriveHandler
import com.wheretogo.domain.handler.HomeEvent
import com.wheretogo.domain.handler.HomeHandler
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.usecase.app.GuideMoveStepUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.checkpoint.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.checkpoint.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.checkpoint.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.checkpoint.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.comment.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.comment.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.ReportCommentUseCase
import com.wheretogo.domain.usecase.course.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.course.RemoveCourseUseCase
import com.wheretogo.domain.usecase.course.ReportCourseUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.util.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.util.SearchKeywordUseCase
import com.wheretogo.domain.usecase.util.UpdateLikeUseCase
import com.wheretogo.presentation.HomeBodyBtn
import com.wheretogo.presentation.HomeBodyBtnHighlight
import com.wheretogo.domain.usecase.course.FilterListCourseUseCase
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.map.MapOverlayService
import com.wheretogo.presentation.intent.HomeIntent
import com.wheretogo.presentation.state.GuideState
import com.wheretogo.presentation.state.HomeScreenState
import com.wheretogo.presentation.viewmodel.HomeViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test


class ViewModelTestByGuide {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun homeGuideTest() = runTest{
        coEvery { observeSettingsUseCase() } returns
                flow {
                    emit(Result.success(Settings(tutorialStep = DriveTutorialStep.SKIP)))
                    emit(Result.success(Settings(tutorialStep = DriveTutorialStep.HOME_TO_DRIVE_CLICK)))
                    emit(Result.success(Settings(tutorialStep = DriveTutorialStep.MOVE_TO_COURSE)))
                }

        coEvery { homeHandler.handle(HomeEvent.GUIDE_START) } returns Unit
        coEvery { guideMoveStepUseCase.start() } returns Result.success(Unit)

        coEvery { homeHandler.handle(HomeEvent.DRIVE_NAVIGATE) } returns Unit
        coEvery { guideMoveStepUseCase(true) } returns Result.success(Unit)

        val initState = HomeScreenState(
            guideState = GuideState(
                tutorialStep = DriveTutorialStep.SKIP
            )
        )
        val viewModel = initHomeViewModel(
            dispatcher = StandardTestDispatcher(testScheduler),
            state = initState
        )
        viewModel.uiState.test {
            assertEquals(initState, awaitItem())

            // 드라이브 버튼 하이라이트
            viewModel.handleIntent(HomeIntent.BodyButtonClick(HomeBodyBtn.GUIDE))

            val highlightOn = initState.run {
                copy(
                    bodyBtnHighlight = HomeBodyBtnHighlight.DRIVE,
                    guideState = guideState.copy(
                        tutorialStep = DriveTutorialStep.HOME_TO_DRIVE_CLICK
                    )
                )
            }

            assertEquals(highlightOn, awaitItem())

            // 드라이브 화면 이동
            viewModel.handleIntent(HomeIntent.BodyButtonClick(HomeBodyBtn.DRIVE))

            val moveToDrive = highlightOn.run {
                copy(
                    bodyBtnHighlight = HomeBodyBtnHighlight.NONE,
                    guideState = guideState.copy(
                        tutorialStep = DriveTutorialStep.MOVE_TO_COURSE
                    )
                )
            }

            assertEquals(moveToDrive, awaitItem())
        }
    }

    private fun initHomeViewModel(
        dispatcher: CoroutineDispatcher,
        state: HomeScreenState
    ): HomeViewModel {
        return HomeViewModel(
            stateInit = state,
            handler = homeHandler,
            dispatcher = dispatcher,
            observeSettingsUseCase,
            guideMoveStepUseCase
        )
    }
    private val homeHandler = mockk<HomeHandler>()
    private val driveHandler = mockk<DriveHandler>()

    private val observeSettingsUseCase = mockk<ObserveSettingsUseCase>()
    private val getNearByCourseUseCase = mockk<GetNearByCourseUseCase>()
    private val getCommentForCheckPointUseCase = mockk<GetCommentForCheckPointUseCase>()
    private val getCheckPointForMarkerUseCase = mockk<GetCheckpointForMarkerUseCase>()
    private val getImageForPopupUseCase = mockk<GetImageForPopupUseCase>()
    private val addCheckpointToCourseUseCase = mockk<AddCheckpointToCourseUseCase>()
    private val addCommentToCheckPointUseCase = mockk<AddCommentToCheckPointUseCase>()
    private val updateLikeUseCase = mockk<UpdateLikeUseCase>()
    private val removeCourseUseCase = mockk<RemoveCourseUseCase>()
    private val removeCheckPointUseCase = mockk<RemoveCheckPointUseCase>()
    private val removeCommentToCheckPointUseCase = mockk<RemoveCommentToCheckPointUseCase>()
    private val reportCourseUseCase = mockk<ReportCourseUseCase>()
    private val reportCheckPointUseCase = mockk<ReportCheckPointUseCase>()
    private val reportCommentUseCase = mockk<ReportCommentUseCase>()
    private val searchKeywordUseCase = mockk<SearchKeywordUseCase>()
    private val signOutUseCase = mockk<UserSignOutUseCase>()
    private val guideMoveStepUseCase = mockk<GuideMoveStepUseCase>()
    private val filterListCourseUseCase = mockk<FilterListCourseUseCase>()
    private val mapOverlayService = mockk<MapOverlayService>()
    private val nativeAdServiceOld = mockk<AdService>()

}