package com.dhkim139.wheretogo.viewmodel

import app.cash.turbine.test
import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.mock.MockErrorHandler
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.LIST_ITEM_ZOOM
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.model.dummy.guideCourse
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
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.HomeBodyBtn
import com.wheretogo.presentation.HomeBodyBtnHighlight
import com.wheretogo.presentation.ViewModelEvent
import com.wheretogo.presentation.ViewModelEventHandler
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.geo.LocationService
import com.wheretogo.presentation.feature.map.DriveMapOverlayService
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.intent.HomeIntent
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.GuideState
import com.wheretogo.presentation.state.HomeScreenState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.toMarkerContainer
import com.wheretogo.presentation.toMarkerInfo
import com.wheretogo.presentation.viewmodel.DriveViewModel
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

        coEvery { viewModelEventHandler.handle(ViewModelEvent.GUIDE_START) } returns Unit
        coEvery { guideMoveStepUseCase.start() } returns Result.success(Unit)

        coEvery { viewModelEventHandler.handle(ViewModelEvent.DRIVE_NAVIGATE) } returns Unit
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

    @Test  // todo 미완성
    fun driveScreenTest() = runTest {
        val courseGroup = listOf(guideCourse)
        val courseOverlayGroup =
            courseGroup.map { it.toMarkerContainer(AppMarker(it.toMarkerInfo())) }
        val listItemGroup =
            courseGroup.map{
                ListState.ListItemState(

                )
            }
        coEvery { observeSettingsUseCase() } returns flow {
            emit(Result.success(Settings(tutorialStep = DriveTutorialStep.MOVE_TO_COURSE)))
            emit(Result.success(Settings(tutorialStep = DriveTutorialStep.DRIVE_LIST_ITEM_CLICK)))
        }

        coEvery { getNearByCourseUseCase(guideCourse.cameraLatLng, 11.0) } returns courseGroup
        coEvery { driveMapOverlayService.addCourseMarkerAndPath(courseGroup) } returns Unit
        coEvery { driveMapOverlayService.showAllOverlays() } returns Unit
        coEvery { driveMapOverlayService.getOverlays() } returns courseOverlayGroup
        coEvery { guideMoveStepUseCase(true) } returns Result.success(Unit)

        val initState = DriveScreenState(
            guideState = GuideState(
                tutorialStep = DriveTutorialStep.MOVE_TO_COURSE
            )
        )
        val viewModel = initDriveViewModel(
            dispatcher = StandardTestDispatcher(testScheduler),
            state = initState
        )

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())
            val targetCamera = CameraState(
                latLng = guideCourse.cameraLatLng,
                zoom = LIST_ITEM_ZOOM,
                updateSource = CameraUpdateSource.GUIDE
            )

            // 가이드 코스 이동
            viewModel.handleIntent(DriveScreenIntent.CameraUpdated(
                cameraState = targetCamera
            ))

            val moveToCamera = initState.run {
                copy(
                    naverMapState = naverMapState.copy(
                        cameraState = targetCamera
                    )
                )
            }

            assertEquals(moveToCamera, awaitItem())

            val screenLoading =  moveToCamera.replaceScreenLoading(true)
            assertEquals(screenLoading, awaitItem())
            val driveListItemClick = screenLoading.run {
                copy(
                    overlayGroup = courseOverlayGroup,
                    listState = listState
                )
            }
            assertEquals(driveListItemClick, awaitItem())
            assertEquals(driveListItemClick.replaceScreenLoading(false), awaitItem())
        }
    }


    private fun initDriveViewModel(
        dispatcher: CoroutineDispatcher,
        state: DriveScreenState
    ): DriveViewModel {
        return DriveViewModel(
            stateInit = state,
            dispatcher = dispatcher,
            errorHandler = MockErrorHandler(),
            observeSettingsUseCase,
            getNearByCourseUseCase,
            getCommentForCheckPointUseCase,
            getCheckPointForMarkerUseCase,
            getImageForPopupUseCase,
            addCheckpointToCourseUseCase,
            addCommentToCheckPointUseCase,
            updateLikeUseCase,
            removeCourseUseCase,
            removeCheckPointUseCase,
            removeCommentToCheckPointUseCase,
            reportCourseUseCase,
            reportCheckPointUseCase,
            reportCommentUseCase,
            searchKeywordUseCase,
            signOutUseCase,
            guideMoveStepUseCase,
            driveMapOverlayService,
            nativeAdService,
            locationService
        )
    }

    private fun initHomeViewModel(
        dispatcher: CoroutineDispatcher,
        state: HomeScreenState
    ): HomeViewModel {
        return HomeViewModel(
            stateInit = state,
            dispatcher = dispatcher,
            observeSettingsUseCase,
            guideMoveStepUseCase,
            viewModelEventHandler
        )
    }
    private val viewModelEventHandler = mockk<ViewModelEventHandler>()

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
    private val driveMapOverlayService = mockk<DriveMapOverlayService>()
    private val nativeAdService = mockk<AdService>()
    private val locationService = mockk<LocationService>()

}