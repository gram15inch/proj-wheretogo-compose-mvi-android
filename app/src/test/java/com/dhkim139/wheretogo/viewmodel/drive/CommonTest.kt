package com.dhkim139.wheretogo.viewmodel.drive

import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.feature.assertFlows
import com.google.common.truth.Truth.assertThat
import com.wheretogo.data.repositoryimpl.MapContentRepositoryImpl
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.AdItem
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
class CommonTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val initState = DriveScreenState(isObserveSetting = false)
    private val observeSettingsUseCase = mockk<ObserveSettingsUseCase>()
    private val nativeAdService = mockk<AdService>()

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
            guideMoveStepUseCase = mockk(),
            signOutUseCase = mockk(),
            clearCacheUseCase = mockk(),
            nativeAdService = nativeAdService,
            mapContentRepository = MapContentRepositoryImpl(),
        )
    }

    // ==================== eventReceive 테스트 ====================
    @Test
    fun `앱 이벤트(로그인 성공)수신시 화면 정리`() = runTest {
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Arrange: 초기 상태 이외의 임의 상태(서치바 확장)
            viewModel.handleIntent(DriveScreenIntent.SearchBarClick(true))
            assertThat(state.awaitItem().stateMode).isEqualTo(DriveVisibleMode.SearchBarExpand)

            // Act: 앱 이벤트(로그인 성공)수신
            viewModel.handleIntent(DriveScreenIntent.EventReceive(AppEvent.SignInScreen, true))

            // Assert: 화면 정리
            assert(event.awaitItem() is DriveEvent.ClearMap)
            state.awaitItem().run {
                assertThat(stateMode).isEqualTo(DriveVisibleMode.Explorer)
            }
        }
    }

    // ==================== lifecycleChange 테스트 ====================
    @Test
    fun `광고 갱신후 화면 생명주기 OnPause 진입시 광고 제거`() = runTest {
        // Arrange
        val adItem = AdItem(createAt = 1)
        val initState = initState
            .createShowSearchBarExpandState(listOf(adItem))
            .createExportMapAppFloatExpandState(listOf(adItem))
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 화면 생명주기 OnPause 진입
            viewModel.handleIntent(DriveScreenIntent.LifecycleChange(AppLifecycle.onPause))

            // Assert: 광고 제거
            state.awaitItem().run {
                assertThat(searchBarState.adItemGroup.size).isEqualTo(0)
                assertThat(floatingButtonState.adItemGroup.size).isEqualTo(0)
            }
        }
    }

    @Test
    fun `서치바 확장후 화면 생명주기 OnResume 진입시 광고 갱신`() = runTest {
        // Arrange: 서치바 확장
        val adItem =  AdItem(createAt = 1)
        val initState = initState.createShowSearchBarExpandState()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { nativeAdService.getAd() } returns Result.success(listOf(adItem))

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 화면 생명주기 OnResume 진입
            viewModel.handleIntent(DriveScreenIntent.LifecycleChange(AppLifecycle.onResume))

            // Assert: 광고 갱신
            state.awaitItem().run {
                assertThat(searchBarState.adItemGroup.firstOrNull()?.createAt).isEqualTo(1)
            }
        }
    }

    @Test
    fun `외부지도 플로팅 확장후 생명주기 OnResume 진입시 광고 갱신`() = runTest {
        // Arrange: 외부지도 플로팅 확장
        val adItem =  AdItem(createAt = 1)
        val initState = initState.createExportMapAppFloatExpandState()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { nativeAdService.getAd() } returns Result.success(listOf(adItem))

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 화면 생명주기 OnResume 진입
            viewModel.handleIntent(DriveScreenIntent.LifecycleChange(AppLifecycle.onResume))

            // Assert: 광고 갱신
            state.awaitItem().run {
                assertThat(floatingButtonState.adItemGroup.firstOrNull()?.createAt).isEqualTo(1)
            }
        }
    }



    // ==================== blurClick 테스트 ====================


    @Test
    fun `코스 정보 바텀시트 표시후 블러 클릭시 코스 세부사항 상태로 변경`() = runTest {
        // Arrange: 코스 정보 바텀시트 표시
        val initState = initState.createShowCourseInfoInfoBottomSheet()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 블러 클릭
            viewModel.handleIntent(DriveScreenIntent.BlurClick)

            // Assert: 체크포인트 팝업으로 변경
            assertCourseDetail()
        }
    }

    @Test
    fun `체크포인트 정보 바텀시트 표시후 블러 클릭시 체크포인트 팝업상태로 변경`() = runTest {
        // Arrange: 체크포인트 바텀시트 표시
        val initState = initState.createShowCheckPointInfoInfoBottomSheet()
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 블러 클릭
            viewModel.handleIntent(DriveScreenIntent.BlurClick)

            // Assert: 체크포인트 팝업상태로 변경
            assertCheckPointPopup()
        }
    }
}