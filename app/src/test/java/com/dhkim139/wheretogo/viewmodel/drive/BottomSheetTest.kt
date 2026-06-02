package com.dhkim139.wheretogo.viewmodel.drive

import com.dhkim139.wheretogo.feature.FlowAssertions
import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.feature.assertFlows
import com.google.common.truth.Truth.assertThat
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.MarkerType
import com.wheretogo.domain.handler.DriveHandler
import com.wheretogo.domain.handler.DriveMsgEvent
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseDirectionItem
import com.wheretogo.domain.model.map.CameraMoveTrigger
import com.wheretogo.domain.model.map.ContentOperation
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType
import com.wheretogo.domain.model.util.ImageInfo
import com.wheretogo.domain.repository.DefaultMapId
import com.wheretogo.domain.repository.MapContentRepository
import com.wheretogo.domain.usecase.app.DriveTutorialUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.checkpoint.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.checkpoint.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.course.RemoveCourseUseCase
import com.wheretogo.domain.usecase.report.ReportContentUseCase
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.event.DriveEvent.RefreshOverlay
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.toContent
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
    private val addCheckpointToCourseUseCase = mockk<AddCheckpointToCourseUseCase>()
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
        coEvery { mapContentRepository.courseList } returns MutableStateFlow(emptyList<Course>())
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
            addCheckpointToCourseUseCase = addCheckpointToCourseUseCase,
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
    fun `체크포인트 추가 바텀시트 변경(열린후)시 해당 코스로 카메라 이동`() = runTest {
        // Arrange: 체크포인트 추가 바텀시트
        val initState= initState.createShowCheckPointAddBottomSheetState()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        val sheet = SheetVisibleMode.Opened
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 바텀시트 변경(열린후)
            viewModel.handleIntent(DriveScreenIntent.BottomSheetChange(sheet))

            // Assert: 해당 코스 위치로 카메라 이동
            (event.awaitItem() as DriveEvent.MoveCamera).run {
                assertThat(option.targetId).isEqualTo(DefaultMapId.SELECT_COURSE_ID.name)
                assertThat(option.trigger).isEqualTo(CameraMoveTrigger.BOTTOM_SHEET_UP)
            }
        }
    }

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
    fun `체크포인트 추가 바텀시트 변경(닫힌후) 추가용 마커 삭제 및 코스세부 상태로 변경`() = runTest {
        // Arrange:  체크포인트 추가 바텀시트
        val initState = initState.createShowCheckPointAddBottomSheetState()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        val sheet = SheetVisibleMode.Closed
        coEvery { driveTutorialUseCase(DriveTutorialStep.COMMENT_SHEET_DRAG) } returns Result.success(Unit)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 바텀시트 변경(닫힌후)
            viewModel.handleIntent(DriveScreenIntent.BottomSheetChange(sheet))

            // Assert: 추가용 마커 삭제
            (event.awaitItem() as DriveEvent.RefreshOverlay).run {
                assertThat(option.markerId).isEqualTo(CHECKPOINT_ADD_MARKER)
            }

            // Assert: 코스세부 상태로 변경
            assertCourseDetail()
            (event.awaitItem() as DriveEvent.MoveCamera).run {
                assertThat(option.targetId).isEqualTo(DefaultMapId.SELECT_COURSE_ID.name)
                assertThat(option.trigger).isEqualTo(CameraMoveTrigger.BOTTOM_SHEET_DOWN)
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

    // ==================== checkpointLocationSliderChange 테스트 ====================
    @Test
    fun `위치 슬라이더 변경시 전체 좌표중 가장 가까운 위치로 추가 마커 이동`() = runTest {
        // Arrange
        val latlng1 = LatLng(1.0,1.0)
        val latlng2 = LatLng(2.0,2.0)
        val latlng3 = LatLng(3.0,3.0)
        val course = CourseDirectionItem(Course("CS001", points = listOf(latlng1,latlng2,latlng3)))
        val initState = initState.createShowCheckPointAddBottomSheetState(0.1f)
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { mapContentRepository.selectedCourseState } returns MutableStateFlow(course)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 위치 슬라이더 변경(0%)
            viewModel.handleIntent(DriveScreenIntent.CheckpointLocationSliderChange(0f))

            // Assert: 전체 좌표중 가장 가까운 위치로 추가 마커 이동
            assertMoveCheckPointAddMarker(0f,latlng1)


            // Act: 위치 슬라이더 변경(50%)
            viewModel.handleIntent(DriveScreenIntent.CheckpointLocationSliderChange(0.5f))

            // Assert: 전체 좌표중 가장 가까운 위치로 추가 마커 이동
            assertMoveCheckPointAddMarker(0.5f,latlng2)


            // Act: 위치 슬라이더 변경(100%)
            viewModel.handleIntent(DriveScreenIntent.CheckpointLocationSliderChange(1f))

            // Assert: 전체 좌표중 가장 가까운 위치로 추가 마커 이동
            assertMoveCheckPointAddMarker(1f,latlng3)
        }
    }


    private suspend fun FlowAssertions<DriveScreenState, DriveEvent>.assertMoveCheckPointAddMarker(
        percent: Float,
        latLng: LatLng
    ) {
        (event.awaitItem() as DriveEvent.RefreshOverlay).run {
            assertThat(option.markerInfo?.contentId).isEqualTo(CHECKPOINT_ADD_MARKER)
            assertThat(option.markerInfo?.position).isEqualTo(latLng)
            assertThat(option.markerInfo?.type).isEqualTo(MarkerType.DEFAULT)
        }
        state.awaitItem().run {
            bottomSheetState.checkPointAddState.let {
                assertThat(it.latLng).isEqualTo(latLng)
                assertThat(it.sliderPercent).isEqualTo(percent)
            }
        }
    }

    // ==================== checkpointDescriptionEnterClick 테스트 ====================
    @Test
    fun `나머지 입력후 설명 텍스트 입력 버튼 클릭시 제출버튼 (비)활성화`() = runTest {
        // Arrange: 나머지 입력
        val initState = initState.createShowCheckPointAddBottomSheetState(
            sliderPercent = 0.1f,
            imgUriString = "uri.jpg",
            description = "",
            isSubmitActive = false
        )
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        val text = "설명 입력입니다."
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 설명 텍스트(텍스트) 입력박스 제출 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CheckpointDescriptionEnterClick(text))
            // Assert: 제출버튼 활성화
            state.awaitItem().run {
                bottomSheetState.checkPointAddState.let {
                    assertThat(it.description).isEqualTo(text)
                    assertThat(it.isSubmitActive).isEqualTo(true)
                }
            }

            // Act: 설명 텍스트(빈 값) 입력박스 제출 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CheckpointDescriptionEnterClick(""))
            // Assert: 제출버튼 비활성화
            state.awaitItem().run {
                bottomSheetState.checkPointAddState.let {
                    assertThat(it.description).isEqualTo("")
                    assertThat(it.isSubmitActive).isEqualTo(false)
                }
            }
        }
    }

    // ==================== checkpointImageChange 테스트 ====================
    @Test
    fun `나머지 입력후 이미지 선택시 이미지 선택 완료 표시 및 제출 버튼 활성화`() = runTest {
        // Arrange: 나머지 입력
        val initState = initState.createShowCheckPointAddBottomSheetState(
            sliderPercent = 0.1f,
            imgUriString = "",
            description = "안녕",
            isSubmitActive = false
        )
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        val imageInfo = ImageInfo("img.jpg", "fileName.jpg",10L)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 이미지 선택
            viewModel.handleIntent(DriveScreenIntent.CheckpointImageChange(imageInfo))
            // Assert: 이미지 선택 완료 표시 및 제출 버튼 활성화
            state.awaitItem().run {
                bottomSheetState.checkPointAddState.let {
                    assertThat(it.imgUriString).isEqualTo(imageInfo.uriString)
                    assertThat(it.imgInfo?.fileName).isEqualTo(imageInfo.fileName)
                    assertThat(it.imgInfo?.byte).isEqualTo(imageInfo.byte)
                    assertThat(it.isSubmitActive).isEqualTo(true)
                }
            }
        }
    }

    // ==================== checkpointSubmitClick 테스트 ====================
    @Test
    fun `나머지 입력후 제출버튼 클릭시 체크포인트 마커 추가 및 코스 세부로 변경`() = runTest {
        // Arrange: 나머지 입력
        val initState = initState.createShowCheckPointAddBottomSheetState(
            sliderPercent = 0.1f,
            imgUriString = "img.jpg",
            description = "안녕",
            isSubmitActive = true
        )
        val checkPoint = CheckPoint("CP001", courseId = "CS001")
        val content = initState.bottomSheetState.checkPointAddState
            .toContent(DefaultMapId.SELECT_COURSE_ID.name)
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { addCheckpointToCourseUseCase(content) } returns Result.success(checkPoint)
        coEvery { driveHandler.handle(DriveMsgEvent.ADD_DONE) } returns Unit

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 제출버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CheckpointSubmitClick)

            // Assert: 로딩 표시
            state.awaitItem().run {
                bottomSheetState.checkPointAddState.let {
                    assertThat(it.isButtonLoading).isEqualTo(true)
                }
            }
            // Assert: 체크포인트 마커 추가
            (event.awaitItem() as RefreshOverlay).run {
                option.checkPoint!!.let {
                    assertThat(it.checkPointId).isEqualTo(checkPoint.checkPointId)
                    assertThat(it.courseId).isEqualTo(checkPoint.courseId)
                }
            }

            // Assert: 코스 세부로 변경
            assertCourseDetail()
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


            // Assert: 코스 갱신
            (event.awaitItem() as DriveEvent.RefreshContent).run {
                assertThat(option.operation).isEqualTo(ContentOperation.REFRESH_COURSES)
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

        coEvery { removeCourseUseCase(DefaultMapId.SELECT_COURSE_ID.name) } returns Result.success(courseId)
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

            // Assert: 전체 코스 갱신
            (event.awaitItem() as DriveEvent.RefreshContent).run {
                assertThat(option.operation).isEqualTo(ContentOperation.REFRESH_COURSES)
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