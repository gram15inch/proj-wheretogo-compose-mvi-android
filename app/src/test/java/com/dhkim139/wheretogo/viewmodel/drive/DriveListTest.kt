package com.dhkim139.wheretogo.viewmodel.drive

import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.feature.assertFlows
import com.google.common.truth.Truth.assertThat
import com.wheretogo.data.repositoryimpl.MapContentRepositoryImpl
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.usecase.app.DriveTutorialUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.domain.model.course.CourseDirectionItem
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.viewmodel.DriveViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("NonAsciiCharacters")
@OptIn(ExperimentalCoroutinesApi::class)
class DriveListTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val initState = DriveScreenState(isObserveSetting = false)
    private val observeSettingsUseCase = mockk<ObserveSettingsUseCase>()
    private val driveTutorialUseCase = mockk<DriveTutorialUseCase>()

    @Before
    fun flowClear() = runTest {
        coEvery { observeSettingsUseCase() } returns flowOf(Result.success(Settings()))
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
            getCommentForCheckPointUseCase = mockk(),
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
            nativeAdService = mockk(),
            mapContentRepository = MapContentRepositoryImpl(),
        )
    }

    // ==================== driveListItemClick 테스트 ====================
    @Test
    fun `드라이브 목록 아이템 클릭시 코스 포커스`() = runTest {
        // Arrange
        val item = CourseDirectionItem(
            course =  Course("CS001", cameraLatLng = LatLng(127.0, 35.0))
        )
        coEvery { driveTutorialUseCase(DriveTutorialStep.DRIVE_LIST_ITEM_CLICK) } returns Result.success(Unit)
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 목록 아이템 클릭
            viewModel.handleIntent(DriveScreenIntent.DriveListItemClick(item))

            // Assert: 코스 포커스 표시
            (event.awaitItem() as DriveEvent.Focus).run {
                assertThat(item.course.courseId).isEqualTo(item.course.courseId)
            }
            state.awaitItem().run {
                assertThat(stateMode).isEqualTo(DriveVisibleMode.CourseDetail)
                assertThat(floatingButtonState.stateMode).isEqualTo(DriveFloatingVisibleMode.Default)
                assertThat(isLoading).isEqualTo(true)
            }
        }
    }
}