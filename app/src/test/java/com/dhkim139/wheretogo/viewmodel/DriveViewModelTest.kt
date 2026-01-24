package com.dhkim139.wheretogo.viewmodel

import app.cash.turbine.test
import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.LIST_ITEM_ZOOM
import com.wheretogo.domain.MarkerType
import com.wheretogo.domain.handler.DriveEvent
import com.wheretogo.domain.handler.DriveHandler
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointContent
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.util.ImageInfo
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
import com.wheretogo.domain.usecase.course.FilterListCourseUseCase
import com.wheretogo.domain.usecase.course.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.course.RemoveCourseUseCase
import com.wheretogo.domain.usecase.course.ReportCourseUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.util.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.util.SearchKeywordUseCase
import com.wheretogo.domain.usecase.util.UpdateLikeUseCase
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.COURSE_DETAIL_MIN_ZOOM
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.DRIVE_LIST_MIN_ZOOM
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.SEARCH_MARKER
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.map.MapOverlayService
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.AdItem
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.model.TypeEditText
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.FloatingButtonState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.state.ListState.ListItemState
import com.wheretogo.presentation.state.NaverMapState
import com.wheretogo.presentation.state.PopUpState
import com.wheretogo.presentation.state.SearchBarState
import com.wheretogo.presentation.toAppError
import com.wheretogo.presentation.toCommentContent
import com.wheretogo.presentation.toCommentItemState
import com.wheretogo.presentation.toMarkerInfo
import com.wheretogo.presentation.toSearchBarItem
import com.wheretogo.presentation.viewmodel.DriveViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class DriveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun initViewModel() = runTest {
        coEvery { observeSettingsUseCase() } returns flowOf(Result.success(Settings()))
        every { mapOverlayService.overlays } returnsMany listOf(emptyList())
    }

    // 가이드
    @Test
    fun guidePopupClick() = runTest {
        val initState = DriveScreenState()
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { guideMoveStepUseCase(true) } returns Result.success(Unit)
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 가이드 팝업 클릭(마지막 단계시)
            viewModel.handleIntent(DriveScreenIntent.GuidePopupClick(DriveTutorialStep.DRIVE_GUIDE_DONE))
        }
    }


    // 서치바
    @Test
    fun searchBarItemClick() = runTest {
        val courseItem = SearchBarItem(
            "label1", "addr1", LatLng(1.0, 1.0), isCourse = true
        )
        val placeItem = SearchBarItem(
            "label2", "addr2", LatLng(2.0, 2.0), isCourse = false
        )
        val placeInfo = MarkerInfo(
            contentId = SEARCH_MARKER, position = placeItem.latlng!!
        )
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.SearchBarExpand, searchBarState = SearchBarState(
                    searchBarItemGroup = listOf(courseItem, placeItem)
                )
            )
        }
        val viewModel1 = initViewModel(StandardTestDispatcher(testScheduler), initState)
        val viewModel2 = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER)) } returnsMany listOf(
            Unit, Unit
        )
        coEvery { mapOverlayService.addOneTimeMarker(listOf(placeInfo)) } returns Unit

        viewModel1.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 서치바 주소 아이템(코스) 클릭
            viewModel1.handleIntent(DriveScreenIntent.AddressItemClick(courseItem))

            // 카메라 이동
            val cameraExpect = initState.run {
                copy(
                    naverMapState = naverMapState.copy(
                        requestCameraState = naverMapState.latestCameraState.copy(
                            latLng = courseItem.latlng!!,
                            zoom = LIST_ITEM_ZOOM,
                            updateSource = CameraUpdateSource.SEARCH_BAR,
                            moveAnimation = MoveAnimation.APP_EASING
                        )
                    ),
                )
            }
            assertEquals(cameraExpect, awaitItem())

            // 서치바 정리(닫기)
            val closeExpect = cameraExpect.run {
                copy(
                    stateMode = DriveVisibleMode.Explorer,
                    searchBarState = searchBarState.copy(
                        isActive = false,
                        isLoading = false,
                        isEmptyVisible = false,
                        searchBarItemGroup = emptyList(),
                        adItemGroup = emptyList()
                    )
                )
            }
            assertEquals(closeExpect, awaitItem())

        }

        viewModel2.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 서치바 주소 아이템(장소) 클릭
            viewModel2.handleIntent(DriveScreenIntent.AddressItemClick(placeItem))

            // 카메라 이동
            val cameraExpect = initState.run {
                copy(
                    naverMapState = naverMapState.copy(
                        requestCameraState = naverMapState.latestCameraState.copy(
                            latLng = placeItem.latlng!!,
                            zoom = LIST_ITEM_ZOOM,
                            updateSource = CameraUpdateSource.SEARCH_BAR,
                            moveAnimation = MoveAnimation.APP_EASING
                        )
                    )
                )
            }
            assertEquals(cameraExpect, awaitItem())

            // 일회용 마커 추가
        }
    }

    @Test
    fun searchBarClick() = runTest {
        val initState = DriveScreenState().run {
            copy(

            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER)) } returns Unit
        coEvery { nativeAdService.getAd() } returns Result.success(emptyList())

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())


            // @ 서치바 클릭(광고있음)
            viewModel.handleIntent(DriveScreenIntent.SearchBarClick(true))
            val activeExpect= initState.run {
                copy(
                    stateMode = DriveVisibleMode.SearchBarExpand,
                    searchBarState = searchBarState.copy(
                        isActive = true
                    )
                )
            }

            assertEquals(activeExpect, awaitItem())

            // 광고 로드

        }
    }

    @Test
    fun searchBarClose() = runTest {
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.SearchBarExpand,
                searchBarState = searchBarState.copy(
                    isActive = true
                )
            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER)) } returns Unit

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 서치바 닫기 클릭
            viewModel.handleIntent(DriveScreenIntent.SearchBarClose)
            val inActiveExpect= initState.run {
                copy(
                    stateMode = DriveVisibleMode.Explorer,
                    searchBarState = searchBarState.copy(
                        isActive = false
                    )
                )
            }
            assertEquals(inActiveExpect, awaitItem())
        }
    }

    @Test
    fun searchSubmit() = runTest {
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.SearchBarExpand,
                searchBarState = searchBarState.copy(
                    isActive = true
                )
            )
        }
        val searchedAddress = SimpleAddress(
            title = "title1",
            address = "address1",
            latlng = LatLng(1.0, 1.0)
        )
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { searchKeywordUseCase("query") } returnsMany listOf(
            Result.success(emptyList()),
            Result.success(listOf(searchedAddress))
        )
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 검색어 입력
            viewModel.handleIntent(DriveScreenIntent.SearchSubmit("query"))
            // 로딩 시작
            val loadingExpect = initState.replaceSearchBarLoading(true)
            assertEquals(loadingExpect, awaitItem())

            // 주소 검색 시도 (성공: 빈값) : 주소 주입 및 ui 변경
            val addressExpect = loadingExpect.copy(
                searchBarState = loadingExpect.searchBarState.copy(
                    isEmptyVisible = true,
                    searchBarItemGroup = emptyList()
                )
            )
            assertEquals(addressExpect, awaitItem())

            // 로딩 중지
            val loadingExpect2 = addressExpect.replaceSearchBarLoading(false)
            assertEquals(loadingExpect2, awaitItem())



            viewModel.handleIntent(DriveScreenIntent.SearchSubmit("query"))
            // 로딩 시작
            val loadingExpect3 = loadingExpect2.replaceSearchBarLoading(true)
            assertEquals(loadingExpect3, awaitItem())

            // 주소 검색 시도 (성공: 결과 있음) : 주소 주입 및 ui 변경
            val addressExpect2 = loadingExpect3.copy(
                searchBarState = loadingExpect.searchBarState.copy(
                    isEmptyVisible = false,
                    searchBarItemGroup = listOf(searchedAddress).map { it.toSearchBarItem() }
                )
            )
            assertEquals(addressExpect2, awaitItem())

            // 로딩 중지
            val loadingExpect4 = addressExpect2.replaceSearchBarLoading(false)
            assertEquals(loadingExpect4, awaitItem())
        }
    }


    // 지도
    @Test
    fun mapAsync() = runTest {
        val initState = DriveScreenState()
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)
        val fpFlow = MutableStateFlow<Int>(0)

        every { mapOverlayService.fingerPrintFlow } returns fpFlow

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 맵 초기화 완료
            viewModel.handleIntent(DriveScreenIntent.MapAsync)

            // 카메라 이동 - 내위치
            val cameraExpect = initState.run {
                copy(
                    naverMapState = naverMapState.copy(
                        requestCameraState = naverMapState.requestCameraState.copy(
                            isMyLocation = true
                        )
                    )
                )
            }
            assertEquals(cameraExpect, awaitItem())

            // 오버레이 할당 - 뷰모델 생성시 주입

            // 오버레이 변경 감지
            fpFlow.update { 3 }
            assertEquals(cameraExpect.copy(fingerPrint = 3), awaitItem())
        }
    }

   @Test
    fun cameraUpdated() = runTest {
        // 지도 탐색 상태
       val latest = CameraState(LatLng(1.0, 1.0), 0.0)
       val current = CameraState(LatLng(2.0, 2.0), DRIVE_LIST_MIN_ZOOM)
       val nearCourse = Course(
            courseId = "cs1",
            waypoints = listOf(current.latLng),
            points = listOf(current.latLng),
            cameraLatLng = LatLng(3.0, 3.0)
        )
        val initState = DriveScreenState(
            stateMode = DriveVisibleMode.Explorer,
            naverMapState = NaverMapState(
                latestCameraState = latest
            )
        )
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { getNearByCourseUseCase(current.latLng, current.zoom) } returns Result.success(listOf(nearCourse))
        coEvery { filterListCourseUseCase(
                current.viewport,
                current.zoom,
                listOf(nearCourse)
            ) } returns Result.success(listOf(nearCourse))
        coEvery { mapOverlayService.addCourseMarkerAndPath(listOf(nearCourse)) } returns Unit
        coEvery { mapOverlayService.showAllOverlays() } returns Unit

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 지도 타일 이동 (카메라 업데이트)
            viewModel.handleIntent(DriveScreenIntent.CameraUpdated(current))

            // 로딩 시작
            val loadingStart = initState.copy(isLoading = true)
            assertEquals(loadingStart, awaitItem())

            // 이동된 위치의 코스를 목록, 오버레이에 표시 (성공)
            val updatedContentItem = loadingStart.run {
                copy(
                    listState = listState.copy(
                        listItemGroup = listOf(
                            ListItemState(
                                course = nearCourse
                            )
                        )
                    )
                )
            }
            assertEquals(updatedContentItem, awaitItem())

            // 로딩 중지
            val loadingStop = updatedContentItem.copy(isLoading = false)
            assertEquals(loadingStop, awaitItem())

            // 카메라 업데이트
            val cameraExpect = loadingStop.run {
                copy(
                    naverMapState = naverMapState.copy(
                        latestCameraState = current
                    )
                )
            }
            assertEquals(cameraExpect, awaitItem())

        }


       // 코스 세부보기 상태
       val moveCanera = CameraState(latLng = LatLng(3.0,3.0), zoom = 11.0)
       val scaleCpId = "cp1"
       val seletedCourse = Course("cs1", checkpointIdGroup = listOf(scaleCpId, "cp2"))
       val initState2 = DriveScreenState(
           stateMode = DriveVisibleMode.CourseDetail,
           naverMapState = NaverMapState(
               latestCameraState = latest
           ),
           selectedCourse = seletedCourse
       )
       val viewModel2 = initViewModel(StandardTestDispatcher(testScheduler), initState2)
       coEvery {
           mapOverlayService.scaleToPointLeafInCluster(seletedCourse.courseId, moveCanera.latLng)
       } returns Result.success(scaleCpId)

       coEvery { mapOverlayService.removeCheckPointCluster(seletedCourse.courseId) } returns Unit
       coEvery { mapOverlayService.showAllOverlays() } returns Unit

       viewModel2.driveScreenState.test {
           assertEquals(initState2, awaitItem())

           // @ 지도 타일 이동 (현재위치)
           viewModel2.handleIntent(DriveScreenIntent.CameraUpdated(moveCanera))

           // 클러스터 리프 스케일

           // 카메라 이동 업데이트
           val cameraExpect = initState2.run {
               copy(
                   naverMapState = naverMapState.copy(
                       latestCameraState = moveCanera
                   )
               )
           }
           assertEquals(cameraExpect, awaitItem())

           // @ 지도 줌아웃 (코스 세부 최소줌)
           val zoomOutCamera = moveCanera.copy(zoom= COURSE_DETAIL_MIN_ZOOM)
           viewModel2.handleIntent(DriveScreenIntent.CameraUpdated(zoomOutCamera))

           // 클러스터 리프 스케일

           // 지도 탐험 돌아가기
           val exploreExpect = cameraExpect.run {
               copy(
                   stateMode = DriveVisibleMode.Explorer,
                   searchBarState = SearchBarState(),
                   popUpState = PopUpState(),
                   bottomSheetState = BottomSheetState(),
                   floatingButtonState = FloatingButtonState(),
                   selectedCourse = Course(),
                   selectedCheckPoint = CheckPoint()
               )
           }
           assertEquals(exploreExpect, awaitItem())

           // 카메라 줌 레벨 업데이트
           val zoomExpect = exploreExpect.run {
               copy(
                   naverMapState = naverMapState.copy(
                       latestCameraState = zoomOutCamera
                   )
               )
           }
           assertEquals(zoomExpect, awaitItem())
       }

    }

    @Test
    fun checkPointLeafClick() = runTest {
        val cs = Course(courseId = "cs1")
        val cp = CheckPoint(
            checkPointId = "cp1",
            imageId = "img1",
            thumbnail = "small/img1.jpg"
        )
        val marker = MarkerInfo(
            contentId = cp.checkPointId,
            type = MarkerType.CHECKPOINT
        )
        val initState = DriveScreenState().run {
            copy(
                selectedCourse = cs
            )
        }
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { getCheckPointForMarkerUseCase(cs.courseId) } returns Result.success(listOf(cp))
        coEvery { getImageForPopupUseCase(cp.imageId) } returns cp.thumbnail

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 체크포인트 마커 클릭
            viewModel.handleIntent(DriveScreenIntent.MarkerClick(marker))

            // 체크포인트 ui 변경
            val clickExpect = initState.run {
                copy(
                    stateMode = DriveVisibleMode.BlurCheckpointDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Popup
                    )
                )
            }
            val clickActual = awaitItem()
            assertEquals(clickExpect, clickActual)

            // 체크포인트 가져오기 시도 (성공)
            val cpExpect = clickExpect.run {
                copy(
                    selectedCheckPoint = cp
                )
            }
            assertEquals(cpExpect, awaitItem())

            // 체크포인트 썸네일 가져오기 시도 (성공)
            val imgExpect = cpExpect.run {
                copy(
                    popUpState = popUpState.copy(
                        imagePath = cp.thumbnail
                    )
                )
            }
            assertEquals(imgExpect, awaitItem())
        }
    }


    // 목록
    @Test
    fun driveListItemClick() = runTest {

        val focus = Pair(
            Course(
                courseId = "cs1",
                waypoints = listOf(LatLng(1.0, 1.0)),
                cameraLatLng = LatLng(1.0, 1.0),
                zoom = (LIST_ITEM_ZOOM - 1).toString()
            ),
            CheckPoint(
                checkPointId = "cp1",
                imageId = "img1",
                thumbnail = "small/img1.jpg",
                latLng = LatLng(1.0, 1.0),
            )
        )

        val normal = Pair(
            Course(
                courseId = "cs2",
                waypoints = listOf(LatLng(1.0, 1.0)),
                cameraLatLng = LatLng(1.0, 1.0),
                zoom = (LIST_ITEM_ZOOM - 1).toString()
            ),
            CheckPoint(
                checkPointId = "cp2",
                imageId = "img1",
                thumbnail = "small/img1.jpg",
                latLng = LatLng(1.0, 1.0),
            )
        )

        val focusItem = ListItemState(course = focus.first)
        val normalItem = ListItemState(course = normal.first)
        val listItemGroup = listOf(focusItem, normalItem)
        val initState = DriveScreenState().run {
            copy(
                listState = listState.copy(
                    listItemGroup = listItemGroup
                )
            )
        }

        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { mapOverlayService.focusAndHideOthers(focus.first) } returns Unit
        coEvery { getCheckPointForMarkerUseCase(focus.first.courseId) } returns
                Result.success(listOf(focus.second))
        coEvery {
            mapOverlayService.addCheckPointCluster(
                courseId = focus.first.courseId,
                checkPointGroup = listOf(focus.second),
                onLeafRendered = any(),
                onLeafClick = any()
            )
        } returns Result.success(Unit)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 목록 아이템 클릭
            viewModel.handleIntent(DriveScreenIntent.DriveListItemClick(focusItem))

            // 코스 클릭 ui 변경후 해당 코스로 카메라 이동
            val listItemExpect = initState.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    isLoading = true,
                    naverMapState = naverMapState.copy(
                        requestCameraState = naverMapState.latestCameraState.copy(
                            latLng = focus.first.cameraLatLng,
                            zoom = LIST_ITEM_ZOOM,
                            updateSource = CameraUpdateSource.LIST_ITEM,
                            moveAnimation = MoveAnimation.APP_LINEAR
                        )
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    selectedCourse = focus.first
                )
            }
            val listItemActual = awaitItem()
            assertEquals(listItemExpect, listItemActual)


            // 체크포인트 가져오기 시도 (성공) : 체크포인트 마커 오버레이 추가
            val cpExpect = listItemExpect.run {
                copy(
                    isLoading = false
                )
            }
            val cpActual = awaitItem()
            assertEquals(cpExpect, cpActual)

            // 체크포인트 확대 (생략)
        }
    }


    // 팝업
    @Test
    fun dismissCommentPopUp() = runTest {
        val initState = DriveScreenState()
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 댓글 팝업 닫기
            viewModel.handleIntent(DriveScreenIntent.DismissPopupComment)

            val closeExpect = initState.run {
                copy(
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Popup
                    ),
                    popUpState = popUpState.copy(
                        commentState = CommentState()
                    )
                )
            }
            assertEquals(closeExpect, awaitItem())
        }
    }

    @Test
    fun commentListItemClick() = runTest {
        // 접힌 댓글(세부o)
        val cm1 = CommentState.CommentItemState(
            data = Comment(commentId = "cm1", detailedReview = "detail1"),
            isFold = true
        )
        // 접힌 댓글(세부x)
        val cm2 = CommentState.CommentItemState(
            data = Comment(commentId = "cm2"),
            isFold = true
        )

        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentItemGroup = listOf(cm1,cm2)
                    )
                )
            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 댓글 아이템 클릭(cm1)
            viewModel.handleIntent(DriveScreenIntent.CommentListItemClick(cm1))

            // 댓글 펼처짐 - 세부o
            val unFoldExpect = initState.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = listOf(cm1.copy(isFold = false) ,cm2)
                        )
                    )
                )
            }

            assertEquals(unFoldExpect, awaitItem())


            // @ 댓글 아이템 재클릭(cm1)
            viewModel.handleIntent(DriveScreenIntent.CommentListItemClick(cm1))


            // 댓글 접힘
            val foldExpect = unFoldExpect.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = listOf(cm1.copy(isFold = true) ,cm2)
                        )
                    )
                )
            }
            assertEquals(foldExpect, awaitItem())

            // @ 댓글 아이템 클릭(cm2)
            viewModel.handleIntent(DriveScreenIntent.CommentListItemClick(cm2))

            // 댓글 펼쳐지지 않음(세부x)
        }
    }

    @Test
    fun commentListItemLongClick() = runTest {
        // 설정이 켜지지 않은 댓글
        val comment = Comment(
            commentId = "cm1"
        )
        val initState = DriveScreenState().run {
            copy(
               popUpState =popUpState.copy(
                   commentState = popUpState.commentState.copy(
                       commentSettingState = popUpState.commentState.commentSettingState.copy(
                           isVisible = false
                       )
                   )
               )
            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())
            viewModel.handleIntent(DriveScreenIntent.CommentListItemLongClick(comment))

            // 댓글 설정화면 보임
            val showExpect= initState.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentSettingState = popUpState.commentState.commentSettingState.copy(
                                isVisible = true,
                                comment = comment
                            )
                        ),
                    )
                )
            }
            assertEquals(showExpect, awaitItem())
        }
    }

    @Test
    fun commentLikeClick() = runTest {
        // 좋아요가 없는 두개의 댓글
        val likeCommentItemState = CommentState.CommentItemState(
            Comment(
                commentId = "cm1",
                like = 0,
                isUserLiked = false
            ),
            isLoading = false,
        )
        val normalCommentItemState = CommentState.CommentItemState(
            Comment(
                commentId = "cm2",
                like = 0,
                isUserLiked = false
            ),
            isLoading = false
        )
        val domainError = DomainError.InternalError("comment like error")
        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = true,
                        commentItemGroup = listOf(likeCommentItemState, normalCommentItemState)
                    )
                )
            )
        }
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { updateLikeUseCase(likeCommentItemState.data, true) } returnsMany
                listOf(Result.failure(domainError), Result.success(Unit))
        coEvery { driveHandler.handle(domainError.toAppError()) } returns domainError.toAppError()

        viewModel.driveScreenState.test {
            assertEquals(initState.popUpState, awaitItem().popUpState)

            // @ 좋아요 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentLikeClick(likeCommentItemState))

            // 좋아요 변경 시도 (실패)
            val commentLikeExpect = initState.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = listOf(
                                likeCommentItemState.copy(
                                    isLoading = true,
                                    data = likeCommentItemState.data.copy(
                                        like = 1,
                                        isUserLiked = true
                                    )
                                ), normalCommentItemState
                            )
                        )
                    )
                )
            }
            // 미리 스위칭
            assertEquals(commentLikeExpect, awaitItem())

            //실패시 좋아요 되돌리기
            assertEquals(initState, awaitItem())


            // @좋아요 변경 시도(성공)
            viewModel.handleIntent(DriveScreenIntent.CommentLikeClick(likeCommentItemState))
            assertEquals(commentLikeExpect, awaitItem())

            // 로딩 중지
            val loadingExpect = commentLikeExpect.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = popUpState.commentState.commentItemGroup.map {
                                if (it.data.commentId == likeCommentItemState.data.commentId)
                                    it.copy(isLoading = false)
                                else it
                            }
                        )
                    )
                )
            }
            assertEquals(loadingExpect, awaitItem())
        }
    }

    @Test
    fun commentAddClick() = runTest {
        val course = Course("cs1")
        val checkPoint = CheckPoint("cp1", courseId = course.courseId)
        val editText = "hi"
        val commentAddState = CommentState.CommentAddState(
            commentType = CommentType.ONE,
        )
        val commentContent = commentAddState.toCommentContent(checkPoint.checkPointId, editText)
        val addedComment = Comment(
            commentId = "cm1",
            groupId = checkPoint.checkPointId,
            emoji = commentContent.emoji,
            oneLineReview = commentContent.oneLineReview
        )

        val refreshedCheckPoint = checkPoint.copy(
            checkPointId = checkPoint.checkPointId,
            courseId = course.courseId,
            caption = "caption1"
        )
        val domainError = DomainError.InternalError("comment add error")
        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = true,
                        commentItemGroup = emptyList(),
                        commentAddState = commentAddState
                    )
                ),
                selectedCourse = course,
                selectedCheckPoint = checkPoint
            )
        }

        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { addCommentToCheckPointUseCase(commentContent) } returnsMany
                listOf(Result.failure(domainError), Result.success(addedComment))
        coEvery {
            getCheckPointForMarkerUseCase(
                course.courseId,
                listOf(addedComment.groupId)
            )
        } returns Result.success(listOf(refreshedCheckPoint))
        coEvery {
            mapOverlayService.updateCheckPointLeafCaption(
                refreshedCheckPoint.courseId,
                refreshedCheckPoint.checkPointId,
                refreshedCheckPoint.caption
            )
        } returns Unit
        coEvery { driveHandler.handle(domainError.toAppError()) } returns domainError.toAppError()
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 추가 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentAddClick(editText))

            // 로딩 시작
            val loadingExpect = initState.replaceCommentAddStateLoading(true)
            assertEquals(loadingExpect, awaitItem())

            //추가 시도(실패) : 로딩 중지 및 추가 스테이트 초기화
            val stateInitExpect = loadingExpect.run {
                initCommentAddState()
            }
            assertEquals(stateInitExpect, awaitItem())


            // @ 추가 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentAddClick(editText))

            // 로딩 시작
            assertEquals(loadingExpect, awaitItem())

            // 추가 시도(성공) : 코멘트 아이템 추가
            val commentAddExpect = loadingExpect.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = listOf(addedComment.toCommentItemState())
                        )
                    )
                )
            }
            assertEquals(commentAddExpect, awaitItem())

            // 체크포인트 캡션 리프레시 시도 (성공) : 체크포인트 오버레이 업데이트 - <생략>

            // 로딩 중지 및 추가 스테이트 초기화
            val stateInitExpect2 = commentAddExpect.initCommentAddState()
            assertEquals(stateInitExpect2, awaitItem())
        }
    }

    @Test
    fun commentRemoveClick() = runTest {
        val course = Course(courseId = "cs1")
        val checkPoint = CheckPoint(courseId = course.courseId, checkPointId = "cp1")
        val defaultComment =
            Comment(commentId = "cm1", groupId = checkPoint.checkPointId, oneLineReview = "hello")
        val removeComment =
            Comment(commentId = "cm2", groupId = checkPoint.checkPointId, oneLineReview = "hi")
        val refreshedCheckPoint =
            CheckPoint(
                courseId = course.courseId,
                checkPointId = checkPoint.checkPointId,
                caption = "hello"
            )

        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = true,
                        commentItemGroup = listOf(
                            defaultComment.toCommentItemState(),
                            removeComment.toCommentItemState()
                        ),
                        commentSettingState = popUpState.commentState.commentSettingState.copy(
                            isVisible = true,
                            comment = removeComment,
                        )
                    )
                ),
                selectedCourse = course,
                selectedCheckPoint = checkPoint
            )
        }
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery {
            removeCommentToCheckPointUseCase(
                removeComment.groupId,
                removeComment.commentId
            )
        } returns
                Result.success(Unit)
        coEvery { getCheckPointForMarkerUseCase(course.courseId) } returns Result.success(
            listOf(refreshedCheckPoint)
        )
        coEvery {
            mapOverlayService.updateCheckPointLeafCaption(
                refreshedCheckPoint.courseId,
                refreshedCheckPoint.checkPointId,
                refreshedCheckPoint.caption
            )
        } returns Unit
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 삭제 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentRemoveClick(removeComment))

            // 로딩 시작
            val loadingExpect = initState.replaceCommentSettingLoading(true)
            assertEquals(loadingExpect, awaitItem())

            // 삭제 시도 (성공) : 코멘트 아이템 삭제
            val commentRemoveExpect = loadingExpect.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = listOf(defaultComment.toCommentItemState())
                        )
                    )
                )
            }
            assertEquals(commentRemoveExpect, awaitItem())

            // 체크포인트 캡션 리프레시 시도 (성공) : 체크포인트 오버레이 업데이트 - <생략>

            // 셋팅 숨기기
            val visibleExpect = commentRemoveExpect.replaceCommentSettingVisible(false)
            assertEquals(visibleExpect, awaitItem())


            // 로딩 중지
            val loadingExpect2 = visibleExpect.replaceCommentSettingLoading(false)
            assertEquals(loadingExpect2, awaitItem())
        }
    }

    @Test
    fun commentReportClick() = runTest {
        val course = Course(courseId = "cs1")
        val checkPoint = CheckPoint(courseId = course.courseId, checkPointId = "cp1")
        val defaultComment =
            Comment(commentId = "cm1", groupId = checkPoint.checkPointId, oneLineReview = "hello")
        val reportComment =
            Comment(commentId = "cm2", groupId = checkPoint.checkPointId, oneLineReview = "hi")
        val refreshedCheckPoint = checkPoint.copy(caption = defaultComment.oneLineReview)
        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = true,
                        commentItemGroup = listOf(
                            defaultComment.toCommentItemState(),
                            reportComment.toCommentItemState()
                        ),
                        commentSettingState = popUpState.commentState.commentSettingState.copy(
                            isVisible = true,
                            comment = reportComment,
                        )
                    )
                ),
                selectedCourse = course,
                selectedCheckPoint = checkPoint
            )
        }
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { reportCommentUseCase(reportComment) } returns Result.success("rp1")
        coEvery { getCheckPointForMarkerUseCase(course.courseId) } returns
                Result.success(listOf(refreshedCheckPoint))
        coEvery {
            mapOverlayService.updateCheckPointLeafCaption(
                refreshedCheckPoint.courseId,
                refreshedCheckPoint.checkPointId,
                refreshedCheckPoint.caption
            )
        } returns Unit
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 신고 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentReportClick(reportComment))

            // 로딩 시작
            val loadingExpect = initState.replaceCommentSettingLoading(true)
            assertEquals(loadingExpect, awaitItem())

            // 신고 시도 (성공) : 코멘트 아이템 삭제
            val commentReportExpect = loadingExpect.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = listOf(defaultComment.toCommentItemState())
                        )
                    )
                )
            }
            assertEquals(commentReportExpect, awaitItem())

            // 체크포인트 캡션 리프레시 시도 (성공) : 체크포인트 오버레이 업데이트 - <생략>

            // 셋팅 숨기기
            val visibleExpect = commentReportExpect.replaceCommentSettingVisible(false)
            assertEquals(visibleExpect, awaitItem())


            // 로딩 중지
            val loadingExpect2 = visibleExpect.replaceCommentSettingLoading(false)
            assertEquals(loadingExpect2, awaitItem())
        }
    }

    @Test
    fun commentEmogiPress() = runTest {
        val initState = DriveScreenState()
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 이모지 클릭
            val seletedEmogi = "\uD83D\uDE42"
            viewModel.handleIntent(DriveScreenIntent.CommentEmogiPress(seletedEmogi))

            // 댓글 추가 대표 이모지 변경
             val largeExpect= initState.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = popUpState.commentState.commentAddState.copy(
                                titleEmoji = seletedEmogi
                            )
                        )
                    )
                )
            }
            assertEquals(largeExpect, awaitItem())
        }
    }

    @Test
    fun commentTypePress() = runTest {
        // 한줄평 입력 폼인 상태
        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentAddState = popUpState.commentState.commentAddState.copy(
                            commentType = CommentType.ONE,
                            oneLineReview = "",
                            isOneLinePreview = false,
                        )
                    )
                )
            )
        }
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 세부사항 타입 클릭(detail) - 한줄평 입력후
            val detailTypeClick = TypeEditText(CommentType.DETAIL, "한줄평")
            viewModel.handleIntent(DriveScreenIntent.CommentTypePress(detailTypeClick))


            // 한줄평 미리보기 표시
            val showExpect= initState.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = popUpState.commentState.commentAddState.copy(
                                commentType = CommentType.DETAIL,
                                oneLineReview = detailTypeClick.editText,
                                detailReview = "",
                                isOneLinePreview = true,
                            )
                        )
                    )
                )
            }
            assertEquals(showExpect, awaitItem())


            // @  한줄평 타입 클릭(one) - 상세설명 입력후
            val oneTypeClick = TypeEditText(
                CommentType.ONE,
                editText = "상세설명 상세설명 상세설명 상세설명"
            )
            viewModel.handleIntent(DriveScreenIntent.CommentTypePress(oneTypeClick))

            // 한줄평 미리보기 숨기기
            val hideExpect= showExpect.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = popUpState.commentState.commentAddState.copy(
                                commentType = CommentType.ONE,
                                oneLineReview = "",
                                detailReview = oneTypeClick.editText,
                                isOneLinePreview = false,
                            )
                        )
                    )
                )
            }

            assertEquals(hideExpect, awaitItem())
        }
    }

    // 플로팅
    @Test
    fun commentFloatingButtonClick() = runTest {
        val course = Course(courseId = "cs1")
        val checkpoint = CheckPoint(checkPointId = "cp1")
        val refreshedComment = Comment(commentId = "cm1", groupId = "cp1", oneLineReview = "hello")
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurCheckpointDetail,
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = false,
                        commentItemGroup = emptyList(),
                    )
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Popup
                ),
                selectedCourse = course,
                selectedCheckPoint = checkpoint
            )
        }
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { getCommentForCheckPointUseCase(refreshedComment.groupId) } returns Result.success(
            listOf(refreshedComment)
        )
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 코멘트 플로팅 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentFloatingButtonClick)

            // 코맨트 플로팅 클릭 ui 변경
            val clickUiExpect = initState.run {
                copy(
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Hide
                    ),
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isContentVisible = true,
                            isImeVisible = true,
                            isLoading = true
                        )
                    )
                )
            }
            assertEquals(clickUiExpect, awaitItem())

            // 코멘트 가져오기 (성공) : 코멘트 아이템 그룹 추가 및 로딩중지
            val commentAddExpect = clickUiExpect.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isLoading = false,
                            commentItemGroup = listOf(refreshedComment.toCommentItemState())
                        )
                    )
                )
            }

            val commentAddActual = awaitItem()
            assertEquals(commentAddExpect, commentAddActual)

        }
    }

    @Test
    fun checkpointAddFloatingButtonClick() = runTest {
        // 코스 세부 보기 상태
        val selectedCourse = Course(courseId = "cs1", points = listOf(LatLng(1.0,1.0)))
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.CourseDetail,
                selectedCourse = selectedCourse
            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        val addMarkerInfo = MarkerInfo(contentId = CHECKPOINT_ADD_MARKER, position = selectedCourse.points.first())
         coEvery { mapOverlayService.addOneTimeMarker(listOf(addMarkerInfo)) } returns Unit

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 체크포인트 추가 플로팅 클릭
            viewModel.handleIntent(DriveScreenIntent.CheckpointAddFloatingButtonClick)

            // 바텀시트 표시 - 체크포인트 추가
            val showExpect = initState.run {
                copy(
                    stateMode = DriveVisibleMode.BottomSheetExpand,
                    bottomSheetState = bottomSheetState.copy(
                        content = DriveBottomSheetContent.CHECKPOINT_ADD,
                    )
                ).initCheckPointAddState()
            }

            assertEquals(showExpect, awaitItem())
        }
    }

    @Test
    fun infoFloatingButtonClick() = runTest {
        // 코스 세부 보기 상태
        val initState1 = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.CourseDetail,
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Default
                ),
            )
        }
        val viewModel1 = initViewModel(StandardTestDispatcher(testScheduler), initState1)
        viewModel1.driveScreenState.test {
            assertEquals(initState1, awaitItem())

            // @ 정보 플로팅 버튼 클릭(코스)
            viewModel1.handleIntent(DriveScreenIntent.InfoFloatingButtonClick(DriveBottomSheetContent.COURSE_INFO))

            // 바텀시트 보이기 - 코스 정보
            val showExpect = initState1.run {
                copy(
                    stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                    bottomSheetState = bottomSheetState.copy(
                        content = DriveBottomSheetContent.COURSE_INFO,
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Hide
                    ),
                ).initInfoState()
            }

            assertEquals(showExpect, awaitItem())
        }

        // 체크포인트 세부보기중 코맨트 팝업이 표시된 상태
        val initState2 = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurCheckpointDetail, popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = true
                    )
                ), floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Popup
                )
            )
        }
        val viewModel2 = initViewModel(StandardTestDispatcher(testScheduler), initState2)
        viewModel2.driveScreenState.test {
            assertEquals(initState2, awaitItem())

            // @ 정보 플로팅 버튼 클릭(체크포인트)
            viewModel2.handleIntent(DriveScreenIntent.InfoFloatingButtonClick(DriveBottomSheetContent.CHECKPOINT_INFO))

            // 바텀시트 보이기 - 체크포인트 정보
            val showExpect = initState2.run {
                copy(
                    stateMode = DriveVisibleMode.BlurCheckpointBottomSheetExpand,
                    bottomSheetState = bottomSheetState.copy(
                        content = DriveBottomSheetContent.CHECKPOINT_INFO,
                    ),
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isContentVisible = false
                        )
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Hide
                    ),
                ).initInfoState()
            }
            assertEquals(showExpect, awaitItem())
        }
    }

    @Test
    fun exportMapFloatingButtonClick() = runTest {
        // 코스 세부 보기 상태
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.CourseDetail,
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Default
                )
            )
        }

        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { nativeAdService.getAd() } returns Result.success(emptyList())

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 외부 지도 플로팅 클릭
            viewModel.handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick)

            // 외부 지도 앱 목록 표시
            val appShow = initState.run {
                copy(
                    stateMode = DriveVisibleMode.BlurCourseDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.ExportExpand
                    )
                )
            }
            assertEquals(appShow, awaitItem())

            // 광고 로드

            // @ 외부 지도 플로팅 재클릭
            viewModel.handleIntent(DriveScreenIntent.ExportMapFloatingButtonClick)

            // 외부 지도 앱 목록 숨기기
            val appHide = appShow.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    )
                )
            }
            assertEquals(appHide, awaitItem())
        }
    }

    @Test
    fun exportMapAppButtonClick() = runTest {
        val initState = DriveScreenState()
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)
        val error = AppError.MapNotSupportExcludeLocation()

        coEvery { driveHandler.handle(error) } returns error
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 외부앱 호출 클릭 - 실패
            viewModel.handleIntent(DriveScreenIntent.ExportMapAppButtonClick(Result.failure(error)))

            // 에러 실패 처리
        }
    }

    @Test
    fun foldFloatingButtonClick() = runTest {
        val selectedCourse = Course("cs1")
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.CourseDetail,
                selectedCourse = selectedCourse
            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { mapOverlayService.removeCheckPointCluster(selectedCourse.courseId) } returns Unit
        coEvery { mapOverlayService.showAllOverlays() } returns Unit

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 폴드 플로팅 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.FoldFloatingButtonClick)

            // 코스 탐색 모드로 돌아가기
            val hideExpect = initState.run {
                copy(
                    stateMode = DriveVisibleMode.Explorer,
                    popUpState = PopUpState(),
                    bottomSheetState = BottomSheetState(),
                    floatingButtonState = FloatingButtonState(),
                    selectedCourse = Course(),
                    selectedCheckPoint = CheckPoint()
                )
            }

            assertEquals(hideExpect, awaitItem())
        }
    }



    // 바텀시트
    @Test
    fun bottomSheetChange() = runTest {
        val seltedCourse = Course("cs1", cameraLatLng = LatLng(1.0,1.0))

        // 추가(체크포인트) 바텀시트 닫힌 상태
        val initState1 = DriveScreenState().run {
            copy(
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.CHECKPOINT_ADD
                ),
                selectedCourse = seltedCourse
            )
        }
        val viewModel1= initViewModel(StandardTestDispatcher(testScheduler), initState1)
        coEvery { mapOverlayService.removeOneTimeMarker(listOf(CHECKPOINT_ADD_MARKER)) } returns Unit
        viewModel1.driveScreenState.test {
            assertEquals(initState1, awaitItem())

            // @ 바텀시트 변경(열림)
            viewModel1.handleIntent(DriveScreenIntent.BottomSheetChange(SheetVisibleMode.Opened))

            // 카메라 이동 - 바텀시트 높이만큼 상승
            val cameraUpExpect = initState1.run{
                copy(
                    naverMapState = naverMapState.copy(
                        requestCameraState = naverMapState.latestCameraState.copy(
                            latLng = seltedCourse.cameraLatLng,
                            updateSource = CameraUpdateSource.BOTTOM_SHEET_UP,
                        )
                    )
                )
            }
            assertEquals(cameraUpExpect, awaitItem())


            // @ 바텀시트 변경(닫힘)
            viewModel1.handleIntent(DriveScreenIntent.BottomSheetChange(SheetVisibleMode.Closed))

            // 카메라 이동 및 코스 세부 정보 돌아가기 - 바텀시트 높이만큼 하강
            val cameraDown = cameraUpExpect.run{
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    naverMapState = naverMapState.copy(
                        requestCameraState = naverMapState.latestCameraState.copy(
                            latLng = seltedCourse.cameraLatLng,
                            updateSource = CameraUpdateSource.BOTTOM_SHEET_DOWN,
                        )
                    ),
                    bottomSheetState = BottomSheetState(),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    )
                )
            }
            assertEquals(cameraDown, awaitItem())
        }

        // 정보(코스) 바텀시트 확장된 상태
        val initState2 = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.COURSE_INFO
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                ),
                selectedCourse = seltedCourse
            )
        }
        val viewModel2= initViewModel(StandardTestDispatcher(testScheduler), initState2)
        viewModel2.driveScreenState.test {
            assertEquals(initState2, awaitItem())

            // @ 바텀시트 변경(닫히는중)
            viewModel2.handleIntent(DriveScreenIntent.BottomSheetChange(SheetVisibleMode.Closing))

            // 코스 세부 정보로 돌아가기
            val courseExpect = initState2.run{
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    )
                )
            }
            assertEquals(courseExpect, awaitItem())

            // @ 바텀시트 변경(닫힘)
            viewModel2.handleIntent(DriveScreenIntent.BottomSheetChange(SheetVisibleMode.Closed))

            // 바텀시트 초기화
            val clearExpect = courseExpect.run{
                copy(
                    bottomSheetState = BottomSheetState()
                )
            }
            assertEquals(clearExpect, awaitItem())
        }

        // 정보(체크포인트) 바텀시트 확장된 상태
        val initState3 = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.CHECKPOINT_INFO
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                ),
                selectedCourse = seltedCourse
            )
        }
        val viewModel3= initViewModel(StandardTestDispatcher(testScheduler), initState3)
        viewModel3.driveScreenState.test {
            assertEquals(initState3, awaitItem())

            // @ 바텀시트 변경(닫히는중)
            viewModel3.handleIntent(DriveScreenIntent.BottomSheetChange(SheetVisibleMode.Closing))

            // 체크포인트 세부정보로 돌아가기
            val checkExpect = initState3.run{
                copy(
                    stateMode = DriveVisibleMode.BlurCheckpointDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Popup
                    )
                )
            }
            assertEquals(checkExpect, awaitItem())

            // @ 바텀시트 변경(닫힘)
            viewModel3.handleIntent(DriveScreenIntent.BottomSheetChange(SheetVisibleMode.Closed))

            // 바텀시트 초기화
            val clearExpect = checkExpect.run{
                copy(
                    bottomSheetState = BottomSheetState()
                )
            }
            assertEquals(clearExpect, awaitItem())
        }

        // 댓글-팝업 바텀시트 확장된 상태
        val initState4 = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurCheckpointDetail,
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                ),
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = true,
                        isImeVisible = true
                    )
                ),
                selectedCourse = seltedCourse
            )
        }
        val viewModel4= initViewModel(StandardTestDispatcher(testScheduler), initState4)
        viewModel4.driveScreenState.test {
            assertEquals(initState4, awaitItem())

            // @ 바텀시트 변경(닫히는중)
            viewModel4.handleIntent(DriveScreenIntent.BottomSheetChange(SheetVisibleMode.Closing))

            // Ime 입력창 숨기기
            val imeExpect = initState4.run{
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isImeVisible = false
                        )
                    ),
                )
            }
            assertEquals(imeExpect, awaitItem())

            // @ 바텀시트 변경(닫힘)
            viewModel4.handleIntent(DriveScreenIntent.BottomSheetChange(SheetVisibleMode.Closed))

            // 플로팅 버튼 보이기
            val floatExpect = imeExpect.run{
                copy(
                    stateMode = DriveVisibleMode.BlurCheckpointDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Popup
                    ),
                    popUpState = popUpState.copy(
                        commentState = CommentState()
                    )
                )
            }
            assertEquals(floatExpect, awaitItem())
        }
    }

     @Test
    fun checkpointLocationSliderChange() = runTest {
        // 두개의 포인트를 가진 코스
        val latLng1 = LatLng(1.0,1.0)
        val latLng2 = LatLng(2.0,2.0)
         val seletedCourse = Course(
             "cs1",
             points = listOf(latLng1,latLng2)
         )
        val initState = DriveScreenState().run {
            copy(
                selectedCourse = seletedCourse
            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

         coEvery { mapOverlayService.updateOneTimeMarker(
             MarkerInfo(contentId = CHECKPOINT_ADD_MARKER, type = MarkerType.DEFAULT, position = latLng1))
         } returns Unit

        coEvery { mapOverlayService.updateOneTimeMarker(
            MarkerInfo(contentId = CHECKPOINT_ADD_MARKER, type = MarkerType.DEFAULT, position = latLng2))
        } returns Unit

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 슬라이더 위치조정(0%)
            viewModel.handleIntent(DriveScreenIntent.CheckpointLocationSliderChange(0.0f))

            // 마커 위치 이동 - 제일 처음
            val move0Expect = initState.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        checkPointAddState = bottomSheetState.checkPointAddState.copy(
                            latLng = latLng1,
                            sliderPercent = 0f,
                            isSubmitActive = false
                        )
                    )
                )
            }
            assertEquals(move0Expect, awaitItem())

            // @ 슬라이더 위치조정(100%)
            viewModel.handleIntent(DriveScreenIntent.CheckpointLocationSliderChange(100.0f))

            // 마커 위치 이동 - 제일 마지막
            val move100Expect = move0Expect.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        checkPointAddState = bottomSheetState.checkPointAddState.copy(
                            latLng = latLng2,
                            sliderPercent = 100.0f,
                            isSubmitActive = false
                        )
                    )
                )
            }
            assertEquals(move100Expect, awaitItem())
        }
    }

    @Test
    fun checkpointDescriptionEnterClick() = runTest {
        val initState = DriveScreenState()
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 텍스트 입력(안녕)
            viewModel.handleIntent(DriveScreenIntent.CheckpointDescriptionEnterClick("안녕"))

            // 텍스트 수정 상태 변경
            val helloExpect = initState.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        checkPointAddState = bottomSheetState.checkPointAddState.copy(
                            description = "안녕",
                            isSubmitActive = false
                        )
                    )
                )
            }
            assertEquals(helloExpect, awaitItem())
        }
    }

    @Test
    fun checkpointImageChange() = runTest {
        val initState = DriveScreenState()
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            val image1 = ImageInfo(
                uriString = "uri1",
                fileName = "name1",
                byte = 1000L
            )
            // @ 이미지 변경(image1)
            viewModel.handleIntent(DriveScreenIntent.CheckpointImageChange(image1))

            // 이미지 추가 상태 변경
            val imageExpect = initState.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        checkPointAddState = bottomSheetState.checkPointAddState.copy(
                            imgUriString = image1.uriString,
                            imgInfo = image1,
                            isSubmitActive = false
                        )
                    )
                )
            }
            assertEquals(imageExpect, awaitItem())
        }
    }

    @Test
    fun checkpointSubmitClick() = runTest {
        val course = Course(
            courseId = "cs1",
            waypoints = listOf(LatLng(1.0, 1.0)),
            points = listOf(LatLng(1.0, 1.0))
        )
        val checkPointAddState = CheckPointAddState(
            latLng = LatLng(2.0, 2.0),
            isSubmitActive = true
        )
        val checkpointContent = CheckPointContent(
            courseId = course.courseId,
            latLng = checkPointAddState.latLng
        )
        val addedCheckPoint =
            CheckPoint("cp1", courseId = course.courseId, latLng = checkpointContent.latLng)
        val domainError = DomainError.InternalError("checkpoint add fail")
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BottomSheetExpand,
                selectedCourse = course,
                bottomSheetState = bottomSheetState.copy(
                    checkPointAddState = checkPointAddState
                )
            )
        }

        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { addCheckpointToCourseUseCase(checkpointContent) } returnsMany
                listOf(Result.failure(domainError), Result.success(addedCheckPoint))
        coEvery { mapOverlayService.addOneTimeMarker(listOf(addedCheckPoint.toMarkerInfo())) } returns Unit
        coEvery { driveHandler.handle(DriveEvent.ADD_DONE) } returns Unit
        coEvery { mapOverlayService.removeOneTimeMarker(listOf(CHECKPOINT_ADD_MARKER)) } returns Unit
        coEvery { driveHandler.handle(DriveEvent.REMOVE_DONE) } returns Unit
        coEvery {
            mapOverlayService.addCheckPointLeaf(
                courseId = addedCheckPoint.courseId,
                checkPoint = addedCheckPoint,
                onLeafClick = any()
            )
        } returns Result.success(Unit)
        coEvery { driveHandler.handle(domainError.toAppError()) } returns domainError.toAppError()

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 체크포인트 제출 클릭
            viewModel.handleIntent(DriveScreenIntent.CheckpointSubmitClick)

            // 로딩 시작
            val loadingExpect1 = initState.replaceCheckpointAddLoading(true)
            assertEquals(loadingExpect1, awaitItem())

            // 체크포인트 생성 시도 (실패) : 로딩 중지
            val loadingExpect2 = loadingExpect1.replaceCheckpointAddLoading(false)
            assertEquals(loadingExpect2, awaitItem())


            // @ 체크포인트 제출 클릭
            viewModel.handleIntent(DriveScreenIntent.CheckpointSubmitClick)

            // 로딩 시작
            val loadingExpect3 = initState.replaceCheckpointAddLoading(true)
            assertEquals(loadingExpect1, awaitItem())

            // 체크포인트 생성 시도 (성공) :  생성된 체크포인트 주입 및 ui 변경
            val checkpointAddExpect = loadingExpect3.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    selectedCourse = selectedCourse.copy(
                        checkpointIdGroup = listOf(addedCheckPoint.checkPointId)
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                )
            }
            assertEquals(checkpointAddExpect, awaitItem())
        }
    }

    @Test
    fun infoReportClickByCourse() = runTest {
        val centerCamera = CameraState(LatLng(3.0, 3.0), zoom = DRIVE_LIST_MIN_ZOOM)
        val normalCourse = Course(
            courseId = "cs1",
            isUserCreated = true,
            cameraLatLng = LatLng(1.0, 1.0),
            waypoints = listOf(LatLng(1.0, 1.0))
        )
        val reportCourse = Course("cs1", isUserCreated = true, checkpointIdGroup = listOf("cp1"))
        val checkpoint =
            CheckPoint(courseId = reportCourse.courseId, checkPointId = "cp1", isUserCreated = true)
        val normalItemState = ListItemState(course = normalCourse)
        val removeItemState = ListItemState(course = reportCourse)

        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                naverMapState = naverMapState.copy(
                    latestCameraState = centerCamera
                ),
                listState = listState.copy(listOf(normalItemState, removeItemState)),
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.COURSE_INFO,
                ),
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = false
                    )
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                ),
                selectedCourse = reportCourse,
                selectedCheckPoint = checkpoint
            ).initInfoState()
        }

        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { reportCourseUseCase(reportCourse, "reason") } returns Result.success("rp1")
        coEvery { driveHandler.handle(DriveEvent.REPORT_DONE) } returns Unit
        coEvery { mapOverlayService.removeCourseMarkerAndPath(listOf(reportCourse.courseId)) } returns Unit
        coEvery { mapOverlayService.removeCheckPointCluster(reportCourse.courseId) } returns Unit
        coEvery { mapOverlayService.showAllOverlays() } returns Unit
        coEvery {
            getNearByCourseUseCase(
                centerCamera.latLng,
                centerCamera.zoom
            )
        } returns Result.success(listOf(normalCourse))
        coEvery {
            filterListCourseUseCase(
                centerCamera.viewport,
                centerCamera.zoom,
                listOf(normalCourse)
            )
        } returns Result.success(listOf(normalCourse))
        coEvery { mapOverlayService.addCourseMarkerAndPath(listOf(normalCourse)) } returns Unit

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 인포 신고 클릭 (by 코스)
            viewModel.handleIntent(DriveScreenIntent.InfoReportClick("reason"))

            // 로딩 시작
            val loadingExpect = initState.replaceInfoLoading(true)
            assertEquals(loadingExpect, awaitItem())

            // 신고 시도 (성공) : ui 변경 및 코스 리프레시
            val reportExpect = loadingExpect.run {
                copy(
                    stateMode = DriveVisibleMode.Explorer,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    selectedCourse = Course(),
                )
            }
            assertEquals(reportExpect, awaitItem())

            // 코스 리프래시
            val refreshExpect = reportExpect.run {
                copy(
                    listState = listState.copy(
                        listItemGroup = listOf(normalItemState)
                    )
                )
            }
            assertEquals(refreshExpect, awaitItem())

            val loadingExpect2 = refreshExpect.replaceInfoLoading(false)
            // 로딩 중지
            assertEquals(loadingExpect2, awaitItem())

        }
    }

    @Test
    fun infoReportClickByCheckpoint() = runTest {
        val course = Course("cs1")
        val reprotCheckpoint = CheckPoint(courseId = course.courseId, checkPointId = "cp1")
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.CHECKPOINT_INFO,
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                ),
                selectedCourse = course,
                selectedCheckPoint = reprotCheckpoint
            ).initInfoState()
        }

        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery {
            reportCheckPointUseCase(
                reprotCheckpoint,
                "reason"
            )
        } returns Result.success("rp1")
        coEvery { driveHandler.handle(DriveEvent.REPORT_DONE) } returns Unit
        coEvery {
            mapOverlayService.removeCheckPointLeaf(
                reprotCheckpoint.courseId,
                reprotCheckpoint.checkPointId
            )
        } returns Unit
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 인포 신고 클릭 (by 체크포인트)
            viewModel.handleIntent(DriveScreenIntent.InfoReportClick("reason"))
            // 로딩 시작
            val loadingExpect = initState.replaceInfoLoading(true)
            assertEquals(loadingExpect, awaitItem())

            // 신고 시도(성공) : ui 변경 및 체크포인트 초기화
            val reprotExpect = loadingExpect.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    selectedCheckPoint = CheckPoint()
                )
            }
            assertEquals(reprotExpect, awaitItem())

            val loadingExpect2 = reprotExpect.replaceInfoLoading(false)
            // 로딩 중지
            assertEquals(loadingExpect2, awaitItem())
        }
    }

    @Test
    fun infoRemoveClickByCourse() = runTest {
        val centerCamera = CameraState(LatLng(3.0, 3.0), zoom = DRIVE_LIST_MIN_ZOOM)
        val normalCourse = Course(
            courseId = "cs1",
            isUserCreated = true,
            cameraLatLng = LatLng(1.0, 1.0),
            waypoints = listOf(LatLng(1.0, 1.0))
        )
        val removeCourse = Course("cs1", isUserCreated = true, checkpointIdGroup = listOf("cp1"))
        val checkpoint =
            CheckPoint(courseId = removeCourse.courseId, checkPointId = "cp1", isUserCreated = true)
        val normalItemState = ListItemState(course = normalCourse)
        val removeItemState = ListItemState(course = removeCourse)
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                naverMapState = naverMapState.copy(
                    latestCameraState = centerCamera
                ),
                listState = listState.copy(listOf(normalItemState, removeItemState)),
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.COURSE_INFO,
                ),
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isContentVisible = false
                    )
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                ),
                selectedCourse = removeCourse,
                selectedCheckPoint = checkpoint
            ).initInfoState()
        }

        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { removeCourseUseCase(courseId = removeCourse.courseId) } returns Result.success(
            removeCourse.courseId
        )
        coEvery { driveHandler.handle(DriveEvent.REMOVE_DONE) } returns Unit
        coEvery { mapOverlayService.removeCourseMarkerAndPath(listOf(removeCourse.courseId)) } returns Unit
        coEvery { mapOverlayService.removeCheckPointCluster(removeCourse.courseId) } returns Unit
        coEvery { mapOverlayService.showAllOverlays() } returns Unit

        coEvery {
            getNearByCourseUseCase(centerCamera.latLng, centerCamera.zoom)
        } returns Result.success(listOf(normalCourse))
        coEvery {
            filterListCourseUseCase(
                centerCamera.viewport,
                centerCamera.zoom,
                listOf(normalCourse)
            )
        } returns Result.success(listOf(normalCourse))
        coEvery { mapOverlayService.addCourseMarkerAndPath(listOf(normalCourse)) } returns Unit

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 인포 삭제 클릭 (by 코스)
            viewModel.handleIntent(DriveScreenIntent.InfoRemoveClick)

            // 로딩 시작
            val loadingExpect = initState.replaceInfoLoading(true)
            assertEquals(loadingExpect, awaitItem())

            // 삭제시도(성공) : ui 변경
            val removeExpect = loadingExpect.run {
                copy(
                    stateMode = DriveVisibleMode.Explorer,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    selectedCourse = Course()
                )
            }
            assertEquals(removeExpect, awaitItem())

            // 코스 리프래시
            val refreshExpect = removeExpect.run {
                copy(
                    listState = listState.copy(
                        listItemGroup = listOf(normalItemState)
                    ),
                )
            }
            assertEquals(refreshExpect, awaitItem())

            val loadingExpect2 = refreshExpect.replaceInfoLoading(false)
            // 로딩 중지
            assertEquals(loadingExpect2, awaitItem())

        }
    }

    @Test
    fun infoRemoveClickByCheckPoint() = runTest {
        val course = Course("cs1")
        val removeCheckpoint = CheckPoint(courseId = course.courseId, checkPointId = "cp1")
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.CHECKPOINT_INFO,
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Hide
                ),
                selectedCourse = course,
                selectedCheckPoint = removeCheckpoint
            ).initInfoState()
        }

        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery {
            removeCheckPointUseCase(removeCheckpoint.courseId, removeCheckpoint.checkPointId)
        } returns Result.success(removeCheckpoint.checkPointId)
        coEvery { driveHandler.handle(DriveEvent.REMOVE_DONE) } returns Unit
        coEvery {
            mapOverlayService.removeCheckPointLeaf(
                removeCheckpoint.courseId,
                removeCheckpoint.checkPointId
            )
        } returns Unit
        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 인포 삭제 클릭 (by 체크포인트)
            viewModel.handleIntent(DriveScreenIntent.InfoRemoveClick)
            // 로딩 시작
            val loadingExpect = initState.replaceInfoLoading(true)
            assertEquals(loadingExpect, awaitItem())

            // 삭제시도(성공) : ui 변경 및 체크포인트 초기화
            val removeExpect = loadingExpect.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    selectedCheckPoint = CheckPoint()
                )
            }
            assertEquals(removeExpect, awaitItem())

            val loadingExpect2 = removeExpect.replaceInfoLoading(false)
            // 로딩 중지
            assertEquals(loadingExpect2, awaitItem())
        }
    }


    @Test
    fun lifecycleChange() = runTest {
        val loadedAd = listOf(AdItem(null, createAt = 1))
        /*
        * loadAd() , clearAd() item 확인은 여기서만
        * */
        // 외부 지도앱 연동 화면
        val initState = DriveScreenState().run {
            copy(
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.ExportExpand
                )
            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { nativeAdService.getAd() } returns Result.success(loadedAd)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 라이프사이클 변경(화면 재개)
            viewModel.handleIntent(DriveScreenIntent.LifecycleChange(AppLifecycle.onResume))

            // 광고 로드
            val loadExpect = initState.run {
                copy(
                    floatingButtonState = floatingButtonState.copy(
                        adItemGroup = loadedAd
                    )
                )
            }
            assertEquals(loadExpect, awaitItem())

            // @ 라이프사이클 변경(화면 정지)
            viewModel.handleIntent(DriveScreenIntent.LifecycleChange(AppLifecycle.onPause))

            // 광고 삭제
            val clearExpect = loadExpect.run {
                copy(
                    floatingButtonState = floatingButtonState.copy(
                        adItemGroup = emptyList()
                    )
                )
            }
            assertEquals(clearExpect, awaitItem())

        }
    }

    @Test
    fun eventReceive() = runTest {
        // 정리 되지 않은 스크린 상태
        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.CourseDetail,
                searchBarState = SearchBarState(
                    isActive = true
                ),
                popUpState = PopUpState(
                    imagePath = "img1"
                ),
                bottomSheetState = BottomSheetState(
                    content = DriveBottomSheetContent.CHECKPOINT_ADD
                ),
                floatingButtonState = FloatingButtonState(
                    stateMode = DriveFloatingVisibleMode.ExportExpand
                ),
                selectedCourse = Course("cs1"),
                selectedCheckPoint = CheckPoint("cp1"),
                listState = ListState(
                    listOf(ListItemState(course = Course("cs1")))
                )
            )
        }
        val viewModel= initViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { nativeAdService.getAd() } returns Result.success(emptyList())
        coEvery { mapOverlayService.clear() } returns Unit

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 로그인창 에서 성공후 돌아옴
            viewModel.handleIntent(DriveScreenIntent.EventReceive(AppEvent.SignInScreen, true))

            // 광고 로드

            // 스크린 정리
            val clearExpect = initState.run {
                copy(
                    stateMode = DriveVisibleMode.Explorer,
                    searchBarState = SearchBarState(),
                    popUpState = PopUpState(),
                    bottomSheetState = BottomSheetState(),
                    floatingButtonState = FloatingButtonState(),
                    selectedCourse = Course(),
                    selectedCheckPoint = CheckPoint(),
                    listState = ListState()
                )
            }
            assertEquals(clearExpect, awaitItem())
        }
    }

    @Test
    fun blurClick() = runTest {

        // 바텀시트 블러 상태
        val initState1 = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand
            )
        }
        val viewModel1= initViewModel(StandardTestDispatcher(testScheduler), initState1)
        viewModel1.driveScreenState.test {
            assertEquals(initState1, awaitItem())

            // @ 블러 클릭
            viewModel1.handleIntent(DriveScreenIntent.BlurClick)

            // 코스 세부 상태로 이동
            val courseExpect = initState1.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail
                )
            }
            assertEquals(courseExpect, awaitItem())

        }

        // 체크포인트 상세 블러 상태
        val initState2 = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurCheckpointBottomSheetExpand
            )
        }
        val viewModel2= initViewModel(StandardTestDispatcher(testScheduler), initState2)
        viewModel2.driveScreenState.test {
            assertEquals(initState2, awaitItem())

            // @ 블러 클릭
            viewModel2.handleIntent(DriveScreenIntent.BlurClick)

            // 체크포인트 세부 상태로 이동
            val checkpointExpect = initState2.run {
                copy(
                    stateMode = DriveVisibleMode.BlurCheckpointDetail,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Popup
                    )
                )
            }
            assertEquals(checkpointExpect, awaitItem())
        }
    }



    private fun initViewModel(
        dispatcher: CoroutineDispatcher,
        state: DriveScreenState
    ): DriveViewModel {
        return DriveViewModel(
            stateInit = state,
            dispatcher = dispatcher,
            handler = driveHandler,
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
            filterListCourseUseCase,
            nativeAdService,
            mapOverlayService
        )
    }

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
    private val nativeAdService = mockk<AdService>()

}