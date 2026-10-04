package com.dhkim139.wheretogo.viewmodel.drive

import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.feature.assertFlows
import com.google.common.truth.Truth.assertThat
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.handler.DriveHandler
import com.wheretogo.domain.handler.DriveMsgEvent
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.map.ContentOperation
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType
import com.wheretogo.domain.repository.DefaultMapId
import com.wheretogo.domain.repository.MapContentRepository
import com.wheretogo.domain.usecase.app.DriveTutorialUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.checkpoint.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.checkpoint.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.course.RemoveCourseUseCase
import com.wheretogo.domain.usecase.report.ReportContentUseCase
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.viewmodel.DriveViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("NonAsciiCharacters")
@OptIn(ExperimentalCoroutinesApi::class)
class BottomSheetTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val initState = DriveScreenState(isObserveSetting = false)
    private val observeSettingsUseCase = mockk<ObserveSettingsUseCase>()
    private val driveHandler = mockk<DriveHandler>()
    private val removeCourseUseCase = mockk<RemoveCourseUseCase>()
    private val removeCheckPointUseCase = mockk<RemoveCheckPointUseCase>()
    private val reportContentUseCase = mockk<ReportContentUseCase>()
    private val mapContentRepository = mockk<MapContentRepository>()
    private val driveTutorialUseCase = mockk<DriveTutorialUseCase>()
    @Before
    fun flowClear() = runTest {
        coEvery { observeSettingsUseCase() } returns flowOf(Result.success(Settings()))
        coEvery { mapContentRepository.selectedCourseState } returns MutableStateFlow(null)
        coEvery { mapContentRepository.selectedCheckPointState } returns MutableStateFlow(null)
        coEvery { mapContentRepository.courseList } returns MutableStateFlow(emptyList())
        coEvery { mapContentRepository.checkPointList } returns MutableStateFlow(emptyList<CheckPoint>())
    }

    private fun createViewModel(
        dispatcher: CoroutineDispatcher,
        state: DriveScreenState
    ): DriveViewModel {
        return DriveViewModel(
            stateInit = state,
            dispatcher = dispatcher,
            handler = driveHandler,
            observeSettingsUseCase = observeSettingsUseCase,
            getCommentForCheckPointUseCase = mockk(),
            getImageUseCase = mockk(),
            addCommentToCheckPointUseCase = mockk(),
            removeCourseUseCase = removeCourseUseCase,
            removeCheckPointUseCase = removeCheckPointUseCase,
            removeCommentToCheckPointUseCase = mockk(),
            reportContentUseCase = reportContentUseCase,
            updateLikeUseCase = mockk(),
            searchKeywordUseCase = mockk(),
            driveTutorialUseCase = driveTutorialUseCase,
            signOutUseCase = mockk(),
            clearCacheUseCase = mockk(),
            nativeAdService = mockk(),
            mapContentRepository = mapContentRepository,
        )
    }

    // ==================== bottomSheetChange 테스트 ====================

    @Test
    fun `코스 정보 바텀시트 변경(닫히는중)시 코스세부 상태로 변경`() = runTest {
        // Arrange: `코스 정보 바텀시트
        val initState = initState.createShowCourseInfoInfoBottomSheet()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        val sheet = SheetVisibleMode.Closing
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 바텀시트 변경(닫히는중)
            viewModel.handleIntent(DriveScreenIntent.BottomSheetChange(sheet))

            // Assert: 코스세부 상태로 변경
            assertCourseDetail()
        }
    }

    @Test
    fun `체크포인트 정보 바텀시트 변경(닫히는중)시 체크포인트 팝업 상태로 변경`() = runTest {
        // Arrange: 체크포인트 정보 바텀시트
        val initState = initState.createShowCheckPointInfoInfoBottomSheet()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        val sheet = SheetVisibleMode.Closing
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 바텀시트 변경(닫히는중)
            viewModel.handleIntent(DriveScreenIntent.BottomSheetChange(sheet))

            // Assert: 체크포인트 팝업 상태로 변경
            assertCheckPointPopup()
        }
    }

    @Test
    fun `댓글 팝업 바텀시트 변경(닫히는중)시 키보드 닫힘 `() = runTest {
        // Arrange: 체크포인트 정보 바텀시트
        val initState = initState.createShowPopupCommentState(true)
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        val sheet = SheetVisibleMode.Closing
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 바텀시트 변경(닫히는중)
            viewModel.handleIntent(DriveScreenIntent.BottomSheetChange(sheet))

            // Assert: 키보드 닫힘
            state.awaitItem().run {
                assertThat(popUpState.commentState.isImeVisible).isEqualTo(false)
            }
        }
    }

    @Test
    fun `댓글 팝업 바텀시트 변경(닫힌후) 체크포인트 팝업 상태로 변경`() = runTest {
        // Arrange: 댓글 팝업 바텀시트
        val initState = initState.createShowPopupCommentState()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        val sheet = SheetVisibleMode.Closed
        coEvery { driveTutorialUseCase(DriveTutorialStep.COMMENT_SHEET_DRAG) } returns Result.success(Unit)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 바텀시트 변경(닫힌후)
            viewModel.handleIntent(DriveScreenIntent.BottomSheetChange(sheet))

            // Assert: 체크포인트 팝업 상태로 변경
            assertCheckPointPopup()
        }
    }

    // ==================== infoReportClick 테스트 ====================
    @Test
    fun `코스 정보 바텀시트 표시후 신고 버튼 클릭시 바텀시트 숨기기 및 코스 갱신`() = runTest {
        // Arrange: 코스 정보 바텀시트 표시
        val reason = ReportReason.INAPPROPRIATE
        val initState = initState.run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.COURSE_INFO
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                )
            )
        }
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { reportContentUseCase.bySelect(ReportType.COURSE,reason) } returns Result.success(Unit)
        coEvery { driveHandler.handle(DriveMsgEvent.REPORT_DONE) } returns Unit

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 신고 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.InfoReportClick(reason))

            // Assert: 로딩 표시
            state.awaitItem().run {
                bottomSheetState.infoState.let {
                    assertThat(it.isLoading).isEqualTo(true)
                }
            }

            // Assert: 코스 삭제
            (event.awaitItem() as DriveEvent.RefreshContent).run {
                assertThat(option.operation).isEqualTo(ContentOperation.DELETE_COURSE)
                assertThat(option.id).isEqualTo(DefaultMapId.SELECT_COURSE_ID.name)
            }
        }
    }

    @Test
    fun `체크포인트 정보 바텀시트 표시후 신고 버튼 클릭시 마커 삭제`() = runTest {
        // Arrange: 체크포인트 정보 바텀시트 표시
        val reason = ReportReason.INAPPROPRIATE
        val initState = initState.run {
            copy(
                stateMode = DriveVisibleMode.BlurCheckpointBottomSheetExpand,
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.CHECKPOINT_INFO
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                )
            )
        }
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { reportContentUseCase.bySelect(ReportType.CHECKPOINT,reason) } returns Result.success(Unit)
        coEvery { driveHandler.handle(DriveMsgEvent.REPORT_DONE) } returns Unit

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 신고 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.InfoReportClick(reason))

            // Assert: 로딩 표시
            state.awaitItem().run {
                bottomSheetState.infoState.let {
                    assertThat(it.isLoading).isEqualTo(true)
                }
            }

            // Assert: 마커 삭제
            (event.awaitItem() as DriveEvent.RefreshContent).run {
                assertThat(option.operation).isEqualTo(ContentOperation.DELETE_CHECKPOINT)
                assertThat(option.id).isEqualTo(DefaultMapId.SELECT_CHECKPOINT_ID.name)
                assertThat(option.groupId).isEqualTo(DefaultMapId.SELECT_COURSE_ID.name)
            }

        }
    }

    // ==================== infoRemoveClick 테스트 ====================
    @Test
    fun `코스 정보 바텀시트 표시후 삭제 버튼 클릭시 코스 삭제 및 전체 코스 갱신`() = runTest {
        // Arrange: 코스 정보 바텀시트 표시
        val reason = ReportReason.INAPPROPRIATE
        val courseId = "CS001"
        val initState = initState.createShowCourseInfoInfoBottomSheet()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { removeCourseUseCase(DefaultMapId.SELECT_COURSE_ID.name) } returns Result.success(Unit)
        coEvery { driveHandler.handle(DriveMsgEvent.REMOVE_DONE) } returns Unit

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 삭제 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.InfoRemoveClick)

            // Assert: 로딩 표시
            state.awaitItem().run {
                bottomSheetState.infoState.let {
                    assertThat(it.isLoading).isEqualTo(true)
                }
            }

            // Assert: 코스 삭제
            (event.awaitItem() as DriveEvent.RefreshContent).run {
                assertThat(option.operation).isEqualTo(ContentOperation.DELETE_COURSE)
                assertThat(option.id).isEqualTo(DefaultMapId.SELECT_COURSE_ID.name)
            }
        }
    }

    @Test
    fun `체크포인트 정보 바텀시트 표시후 삭제 버튼 클릭시 마커 삭제`() = runTest {
        // Arrange: 체크포인트 정보 바텀시트 표시
        val checkPoint = CheckPoint("CP001", courseId = "CS001")
        val initState = initState.createShowCheckPointInfoInfoBottomSheet()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { removeCheckPointUseCase.bySelect() } returns Result.success(checkPoint.checkPointId)
        coEvery { driveHandler.handle(DriveMsgEvent.REMOVE_DONE) } returns Unit

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 삭제 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.InfoRemoveClick)

            // Assert: 로딩 표시
            state.awaitItem().run {
                bottomSheetState.infoState.let {
                    assertThat(it.isLoading).isEqualTo(true)
                }
            }

            // Assert: 마커 삭제
            (event.awaitItem() as DriveEvent.RefreshContent).run {
                assertThat(option.operation).isEqualTo(ContentOperation.DELETE_CHECKPOINT)
                assertThat(option.id).isEqualTo(DefaultMapId.SELECT_CHECKPOINT_ID.name)
                assertThat(option.groupId).isEqualTo(DefaultMapId.SELECT_COURSE_ID.name)
            }
        }
    }

}