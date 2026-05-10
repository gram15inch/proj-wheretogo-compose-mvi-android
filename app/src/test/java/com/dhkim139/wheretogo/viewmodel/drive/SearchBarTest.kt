package com.dhkim139.wheretogo.viewmodel.drive

import com.dhkim139.wheretogo.feature.FlowAssertions
import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.feature.assertFlows
import com.google.common.truth.Truth.assertThat
import com.wheretogo.data.repositoryimpl.MapContentRepositoryImpl
import com.wheretogo.domain.ZOOM
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.model.map.CameraMoveTrigger
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.util.SearchKeywordUseCase
import com.wheretogo.presentation.CLEAR_ADDRESS
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.SEARCH_MARKER
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.AdItem
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.toSearchBarItem
import com.wheretogo.presentation.viewmodel.DriveViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("NonAsciiCharacters")
@OptIn(ExperimentalCoroutinesApi::class)
class SearchBarTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val initState = DriveScreenState(isObserveSetting = false)
    private val observeSettingsUseCase = mockk<ObserveSettingsUseCase>()
    private val searchKeywordUseCase = mockk<SearchKeywordUseCase>()
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
            searchKeywordUseCase = searchKeywordUseCase,
            guideMoveStepUseCase = mockk(),
            signOutUseCase = mockk(),
            clearCacheUseCase = mockk(),
            nativeAdService = nativeAdService,
            mapContentRepository = MapContentRepositoryImpl()
        )
    }

    // ==================== searchBarItemClick 테스트 ====================

    @Test
    fun `CLEAR_ADDRESS 클릭시 서치바 닫힘 및 장소 마커 삭제`() = runTest {
        // Arrange
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState.createShowSearchBarExpandState())
        val clearItem = SearchBarItem(
            label = CLEAR_ADDRESS,
            latlng = null,
            isCourse = false
        )

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {

            // Act: CLEAR_ADDRESS 클릭
            viewModel.handleIntent(DriveScreenIntent.AddressItemClick(clearItem))

            // Assert: 서치바 닫힘
            assertCloseSearchBar()
        }
    }

    @Test
    fun `코스 아이템 클릭시 카메라 이동 및 서치바 닫힘`() = runTest {
        // Arrange
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState.createShowSearchBarExpandState())
        val courseItem = SearchBarItem(
            label = "판교 드라이브",
            latlng = LatLng(37.566, 126.978),
            isCourse = true
        )

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {

            // Act: 코스 아이템 클릭
            viewModel.handleIntent(DriveScreenIntent.AddressItemClick(courseItem))


            // Assert: 카메라 이동
            (event.awaitItem() as DriveEvent.MoveCamera).run {
                assertThat(option.latlng).isEqualTo(courseItem.latlng)
                assertThat(option.zoom).isEqualTo(ZOOM.DISTRICT.level)
                assertThat(option.trigger).isEqualTo(CameraMoveTrigger.SEARCH_BAR)
            }

            // Assert: 서치바 닫힘
            assertCloseSearchBar()
        }
    }

    @Test
    fun `장소 아이템 클릭시 카메라 이동 및 마커 추가`() = runTest {
        // Arrange
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState.createShowSearchBarExpandState())
        val placeItem = SearchBarItem(
            label = "홍대입구",
            latlng = LatLng(37.550, 126.940),
            isCourse = false
        )

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 장소 아이템 클릭
            viewModel.handleIntent(DriveScreenIntent.AddressItemClick(placeItem))

            // Assert: 카메라 이동
            (event.awaitItem() as DriveEvent.MoveCamera).run {
                assertThat(option.latlng).isEqualTo(placeItem.latlng)
                assertThat(option.zoom).isEqualTo(ZOOM.DISTRICT.level)
                assertThat(option.trigger).isEqualTo(CameraMoveTrigger.SEARCH_BAR)
            }

            // Assert: 마커 추가
            (event.awaitItem() as DriveEvent.RefreshOverlay).run {
                assertThat(option.markerInfo).isEqualTo(placeItem.toMarkerInfo())
            }
        }
    }


    // ==================== searchBarClick 테스트 ====================

    @Test
    fun `서치바 클릭(광고표시)시 서치바 열기 및 광고갱신`() = runTest {
        // Arrange
        val loadAd = AdItem(createAt = 1)

        coEvery { nativeAdService.getAd() } returns Result.success(listOf(loadAd))
        val viewModel= createViewModel(StandardTestDispatcher(testScheduler), initState)
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {

            // Act: 서치바 클릭
            viewModel.handleIntent(DriveScreenIntent.SearchBarClick(false))

            // Assert: 서치바 열기
            state.awaitItem().run {
                assertThat(stateMode).isEqualTo(DriveVisibleMode.SearchBarExpand)
                assertTrue(searchBarState.isActive)
                assertTrue(searchBarState.isAdVisible)
            }

            // Assert: 광고 갱신
            state.awaitItem().run {
                assertThat(searchBarState.adItemGroup.firstOrNull()?.createAt).isEqualTo(loadAd.createAt)
            }
        }
    }

    // ==================== searchSubmit 테스트 ====================


    @Test
    fun `서치바 입력 제출(빈값)시 서치바 닫기`() = runTest {
        // Arrange
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState.createShowSearchBarExpandState())
        val query = ""

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 서치바 클릭
            viewModel.handleIntent(DriveScreenIntent.SearchSubmit(query))

            // Assert: 서치바 닫기
            assertCloseSearchBar()
        }
    }

    @Test
    fun `서치바 입력 제출(키워드)시 로딩 표시 및 서치 아이템 갱신`() = runTest {
        // Arrange
        val viewModel = createViewModel(StandardTestDispatcher(testScheduler), initState.createShowSearchBarExpandState())
        val query = "파노라마"
        val address = SimpleAddress(query, "addr1", LatLng())

        coEvery { searchKeywordUseCase(query) } returns Result.success(listOf(address))


        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 서치바 클릭
            viewModel.handleIntent(DriveScreenIntent.SearchSubmit(query))

            // Assert: 로딩 시작
            state.awaitItem().run { assertThat(searchBarState.isLoading).isEqualTo(true) }

            // Assert: 서치 아이템 갱신
            state.awaitItem().run {
                assertThat(searchBarState.searchBarItemGroup).isEqualTo(listOf(address.toSearchBarItem()))
                assertThat(searchBarState.isEmptyVisible).isEqualTo(false)
            }

            // Assert: 로딩 중지
            state.awaitItem().run { assertThat(searchBarState.isLoading).isEqualTo(false) }
        }
    }

    // ==================== Assert Helper ====================

    // Assert : 서치바 닫힘
    private suspend fun FlowAssertions<DriveScreenState, DriveEvent>.assertCloseSearchBar() {
        state.awaitItem().run {
            assertThat(stateMode).isEqualTo(DriveVisibleMode.Explorer)
            assertThat(searchBarState.isActive).isEqualTo(false)
        }
        (event.awaitItem() as DriveEvent.RefreshOverlay).run {
            assertThat(option.markerId).isEqualTo(SEARCH_MARKER)
        }
    }
}