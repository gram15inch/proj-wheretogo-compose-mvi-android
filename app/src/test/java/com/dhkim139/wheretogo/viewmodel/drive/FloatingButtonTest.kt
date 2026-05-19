package com.dhkim139.wheretogo.viewmodel.drive

import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.feature.assertFlows
import com.google.common.truth.Truth.assertThat
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseDirectionItem
import com.wheretogo.domain.model.map.CameraMoveTrigger
import com.wheretogo.domain.repository.MapContentRepository
import com.wheretogo.domain.usecase.app.DriveTutorialUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.comment.GetCommentForCheckPointUseCase
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.event.DriveEvent.RefreshOverlay
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.AdItem
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
class FloatingButtonTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val initState = DriveScreenState(isObserveSetting = false)
    private val observeSettingsUseCase = mockk<ObserveSettingsUseCase>()
    private val mapContentRepository = mockk<MapContentRepository>()
    private val getCommentForCheckPointUseCase = mockk<GetCommentForCheckPointUseCase>()
    private val driveTutorialUseCase = mockk<DriveTutorialUseCase>()
    private val nativeAdService = mockk<AdService>()


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
            handler = mockk(),
            observeSettingsUseCase = observeSettingsUseCase,
            getCommentForCheckPointUseCase = getCommentForCheckPointUseCase,
            getImageForPopupUseCase = mockk(),
            addCheckpointToCourseUseCase = mockk(),
            addCommentToCheckPointUseCase = mockk(),
            removeCourseUseCase = mockk(),
            removeCheckPointUseCase = mockk(),
            removeCommentToCheckPointUseCase = mockk(),
            reportContentUseCase = mockk(),
            updateLikeUseCase = mockk(),
            searchKeywordUseCase = mockk(),
            driveTutorialUseCase = driveTutorialUseCase,
            signOutUseCase = mockk(),
            clearCacheUseCase = mockk(),
            nativeAdService = nativeAdService,
            mapContentRepository = mapContentRepository
        )
    }

    // ==================== commentFloatingButtonClick 테스트 ====================
    @Test
    fun `댓글 플로팅 버튼 클릭시 댓글 팝업 표시 및 댓글 목록 갱신`() = runTest {
        // Arrange
        val checkPoint = CheckPoint("CP001")
        val comment = Comment("CM001")
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

         coEvery { driveTutorialUseCase(DriveTutorialStep.COMMENT_FLOAT_CLICK) } returns Result.success(Unit)
         coEvery { mapContentRepository.selectedCheckPointState } returns MutableStateFlow(checkPoint)
         coEvery { getCommentForCheckPointUseCase(checkPoint.checkPointId) } returns Result.success(listOf(comment))

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 댓글 플로팅 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentFloatingButtonClick)
            // Assert: 코스 세부 표시
            state.awaitItem().run {
                popUpState.commentState.let {
                    assertThat(it.isContentVisible).isEqualTo(true)
                    assertThat(it.isImeVisible).isEqualTo(true)
                }

                assertThat(floatingButtonState.stateMode).isEqualTo(DriveFloatingVisibleMode.Hide)
            }
            // Assert: 댓글 목록 갱신
            state.awaitItem().run {
                assertThat(popUpState.commentState.commentItemGroup?.firstOrNull()?.data?.commentId)
                    .isEqualTo(comment.commentId)
            }
        }
    }

    // ==================== commentFloatingButtonClick 테스트 ====================
    @Test
    fun `체크포인트 플로팅 버튼 클릭시 체크포인트 마커 추가 및 바텀시트 표시`() = runTest {
        // Arrange
        val latlng = LatLng(127.0,35.0)
        val course = CourseDirectionItem(Course("CS001", waypoints = listOf(latlng)))

        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { mapContentRepository.selectedCourseState } returns MutableStateFlow(course)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 체크포인트 플로팅 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CheckpointAddFloatingButtonClick)

            // Assert: 체크포인트 마커 추가
            (event.awaitItem() as RefreshOverlay).run {
                option.markerInfo.let {
                    assertThat(it?.contentId).isEqualTo(CHECKPOINT_ADD_MARKER)
                    assertThat(it?.position).isEqualTo(latlng)
                }
            }

            // Assert: 바텀시트 표시
            state.awaitItem().run {
                assertThat(bottomSheetState.content)
                    .isEqualTo(DriveBottomSheetContent.CHECKPOINT_ADD)
                assertThat(stateMode).isEqualTo(DriveVisibleMode.BottomSheetExpand)
            }
        }
    }

    // ==================== infoFloatingButtonClick 테스트 ====================
    @Test
    fun `정보(코스) 플로팅 버튼 클릭시 코스 정보 바텀시트 표시`() = runTest {
        // Arrange
        val course = CourseDirectionItem(Course("CS001", userName = "USER01", isUserCreated = true))
        val infoContent = DriveBottomSheetContent.COURSE_INFO
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { mapContentRepository.selectedCourseState } returns MutableStateFlow(course)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 정보(코스) 플로팅 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.InfoFloatingButtonClick(infoContent))

            // Assert: 코스 정보 바텀시트 표시
            state.awaitItem().run {
                assertThat(popUpState.commentState.isContentVisible).isEqualTo(false)
                assertThat(bottomSheetState.content).isEqualTo(infoContent)
                assertThat(floatingButtonState.stateMode)
                    .isEqualTo(DriveFloatingVisibleMode.Hide)
                assertThat(stateMode).isEqualTo(DriveVisibleMode.BlurBottomSheetExpand)

                bottomSheetState.infoState.let {
                    assertThat(it.isRemoveButton).isEqualTo(true)
                    assertThat(it.isReportButton).isEqualTo(true)
                    assertThat(it.createdBy).isEqualTo(course.course.userName)
                }
            }
            (event.awaitItem() as DriveEvent.MoveCamera).let {
                assertThat(it.option.trigger).isEqualTo(CameraMoveTrigger.BOTTOM_SHEET_DOWN)
            }
        }
    }

    @Test
    fun `정보(체크포인트) 플로팅 버튼 클릭시 체크 포인트 바텀시트 표시`() = runTest {
        // Arrange
        val checkPoint = CheckPoint("CP001", userName = "USER01", isUserCreated = true)
        val infoContent = DriveBottomSheetContent.CHECKPOINT_INFO
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { mapContentRepository.selectedCheckPointState } returns MutableStateFlow(checkPoint)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 정보(체크포인트) 플로팅 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.InfoFloatingButtonClick(infoContent))

            // Assert: 체크 포인트 정보 바텀시트 표시
            state.awaitItem().run {
                assertThat(popUpState.commentState.isContentVisible).isEqualTo(false)
                assertThat(bottomSheetState.content).isEqualTo(infoContent)
                assertThat(floatingButtonState.stateMode)
                    .isEqualTo(DriveFloatingVisibleMode.Hide)
                assertThat(stateMode).isEqualTo(DriveVisibleMode.BlurCheckpointBottomSheetExpand)

                bottomSheetState.infoState.let {
                    assertThat(it.isRemoveButton).isEqualTo(true)
                    assertThat(it.isReportButton).isEqualTo(true)
                    assertThat(it.createdBy).isEqualTo(checkPoint.userName)
                }
            }
            (event.awaitItem() as DriveEvent.MoveCamera).let {
                assertThat(it.option.trigger).isEqualTo(CameraMoveTrigger.BOTTOM_SHEET_DOWN)
            }

        }
    }

    // ==================== exportMapFloatingButtonClick 테스트 ====================
    @Test
    fun `확장전 외부 지도 플로팅 버튼 클릭시 플로팅 버튼 확장 및 광고 표시`() = runTest {
        // Arrange: 확장전
        val adItem = AdItem(createAt = 1)
        val initState = initState.run {
            copy(
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Default
                ),
                stateMode = DriveVisibleMode.CourseDetail
            )
        }
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { nativeAdService.getAd() } returns Result.success(listOf(adItem))
        coEvery { driveTutorialUseCase(DriveTutorialStep.EXPORT_FLOAT_CLICK) } returns Result.success(Unit)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 외부 지도 플로팅 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick)

            // Assert: 플로팅 버튼 확장
            state.awaitItem().run {
                assertThat(floatingButtonState.stateMode)
                    .isEqualTo(DriveFloatingVisibleMode.ExportExpand)
                assertThat(stateMode)
                    .isEqualTo(DriveVisibleMode.BlurCourseDetail)
            }

            // Assert: 광고 갱신
            state.awaitItem().run {
                assertThat(floatingButtonState.adItemGroup.firstOrNull()?.createAt)
                    .isEqualTo(adItem.createAt)
            }
        }
    }

    @Test
    fun `확장후 외부지도 플로팅 버튼 클릭시 플로팅 버튼 축소 및 광고 숨기기`() = runTest {
        // Arrange: 확장후
        val adItem = AdItem(createAt = 1)
        val initState = initState.run {
            copy(
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.ExportExpand,
                    adItemGroup = listOf(adItem)
                )
            )
        }
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 외부 지도 플로팅 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick)

            // Assert: 플로팅 버튼 축소
            state.awaitItem().run {
                floatingButtonState.let {
                    assertThat(it.stateMode)
                        .isEqualTo(DriveFloatingVisibleMode.Default)
                    assertThat(it.adItemGroup.size).isEqualTo(0)
                }

                assertThat(stateMode)
                    .isEqualTo(DriveVisibleMode.CourseDetail)
            }
        }
    }

    // ==================== exportMapAppButtonClick 테스트 ====================
    @Test
    fun `외부지도 앱 버튼 클릭시 아무것도 안함`() = runTest {
        // Arrange
        val result = Result.success(Unit)
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 외부지도 앱 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.ExportMapAppButtonClick(result))

            // Assert: 아무것도 안함
        }
    }

    // ==================== foldFloatingButtonClick 테스트 ====================
    @Test
    fun `접기 플로팅 버튼 클릭시 코스 포커스 해제`() = runTest {
        // Arrange
        val initState = initState.run {
            copy(
                stateMode = DriveVisibleMode.CourseDetail
            )
        }
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 접기 플로팅 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.FoldFloatingButtonClick)

            // Assert: 코스 포커스 해제
            assertThat(event.awaitItem() is DriveEvent.Release)
        }
    }
}