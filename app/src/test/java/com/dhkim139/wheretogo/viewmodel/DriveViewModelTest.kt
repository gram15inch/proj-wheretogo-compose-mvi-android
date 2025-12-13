package com.dhkim139.wheretogo.viewmodel

import app.cash.turbine.test
import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.mock.MockErrorHandler
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.LIST_ITEM_ZOOM
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointContent
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.course.Course
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
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.DRIVE_LIST_MIN_ZOOM
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.geo.LocationService
import com.wheretogo.presentation.feature.map.DriveMapOverlayService
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.AppCluster
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.ListState.ListItemState
import com.wheretogo.presentation.state.NaverMapState
import com.wheretogo.presentation.toClusterContainer
import com.wheretogo.presentation.toCommentContent
import com.wheretogo.presentation.toCommentItemState
import com.wheretogo.presentation.toMarkerContainer
import com.wheretogo.presentation.toMarkerInfo
import com.wheretogo.presentation.toSearchBarItem
import com.wheretogo.presentation.viewmodel.DriveViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class DriveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 서치바
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
    fun cameraUpdated() = runTest {

        val latest = CameraState(LatLng(1.0, 1.0), 0.0)
        val current = CameraState(LatLng(2.0, 2.0), DRIVE_LIST_MIN_ZOOM)
        val nearCourse = Course(
            courseId = "cs1",
            waypoints = listOf(current.latLng),
            points = listOf(current.latLng),
            cameraLatLng = LatLng(3.0, 3.0)
        )
        val marker = MapOverlay.MarkerContainer(
            nearCourse.courseId, MarkerType.SPOT,
            AppMarker(
                nearCourse.toMarkerInfo(),
                coreMarker = null
            )
        )
        val initState = DriveScreenState(
            naverMapState = NaverMapState(
                cameraState = latest
            )
        )
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { locationService.distance(latest.latLng, current.latLng) } returns 5
        coEvery { locationService.distance(current.latLng, nearCourse.cameraLatLng) } returns 50
        coEvery { getNearByCourseUseCase(current.latLng, current.zoom) } returns listOf(nearCourse)
        coEvery { driveMapOverlayService.addCourseMarkerAndPath(listOf(nearCourse)) } returns Unit
        coEvery { driveMapOverlayService.showAllOverlays() } returns Unit
        coEvery { driveMapOverlayService.getOverlays() } returns listOf(marker)

        viewModel.driveScreenState.test {

            assertEquals(initState, awaitItem())

            // @ 지도 타일 이동 (카메라 업데이트)
            viewModel.handleIntent(DriveScreenIntent.CameraUpdated(current))

            // 카메라 이동
            val camera = initState.run {
                copy(
                    naverMapState = naverMapState.copy(
                        cameraState = current
                    )
                )
            }
            assertEquals(camera, awaitItem())

            // 이동된 위치의 코스를 목록, 오버레이에 표시 (성공)
            assertEquals(camera.copy(isLoading = true), awaitItem())
            val updatedContentItem = camera.run {
                copy(
                    isLoading = true,
                    overlayGroup = listOf(marker),
                    listState = listState.copy(
                        listItemGroup = listOf(
                            ListItemState(
                                distanceFromCenter = 50,
                                course = nearCourse
                            )
                        )
                    )
                )
            }
            assertEquals(updatedContentItem, awaitItem())
            assertEquals(updatedContentItem.copy(isLoading = false), awaitItem())
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
        val marker = AppMarker(
            coreMarker = null,
            markerInfo = MarkerInfo(
                contentId = cp.checkPointId,
                type = MarkerType.CHECKPOINT
            )
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
        data class Item(
            val course: Course,
            val checkpoint: CheckPoint,
            val listItemState: ListItemState,
            val csOverlay: MapOverlay,
            val cpOverlay: MapOverlay
        )

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
        ).run {
            Item(
                course = first,
                checkpoint = second,
                listItemState = ListItemState(course = first),
                csOverlay = first.toMarkerContainer(
                    AppMarker(first.toMarkerInfo())
                ),
                cpOverlay = second.toClusterContainer(
                    AppCluster(second.courseId)
                )
            )
        }

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
        ).run {
            Item(
                course = first,
                checkpoint = second,
                listItemState = ListItemState(course = first),
                csOverlay = first.toMarkerContainer(
                    AppMarker(first.toMarkerInfo().copy(isVisible = false))
                ),
                cpOverlay = second.toClusterContainer(
                    AppCluster(second.courseId)
                )
            )
        }

        val listItemGroup = listOf(focus.listItemState, normal.listItemState)
        val focusCsOverlayGroup = listOf(focus.csOverlay, normal.csOverlay)
        val focusCsCpOverlayGroup = listOf(focus.csOverlay, focus.cpOverlay, normal.csOverlay)
        val initState = DriveScreenState().run {
            copy(
                overlayGroup = focusCsOverlayGroup,
                listState = listState.copy(
                    listItemGroup = listItemGroup
                )
            )
        }

        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { driveMapOverlayService.focusAndHideOthers(focus.course) } returns Unit
        coEvery { driveMapOverlayService.getOverlays() } returnsMany
                listOf(focusCsOverlayGroup, focusCsCpOverlayGroup)
        coEvery { getCheckPointForMarkerUseCase(focus.course.courseId) } returns
                Result.success(listOf(focus.checkpoint))
        coEvery {
            driveMapOverlayService.addCheckPointCluster(
                courseId = focus.course.courseId,
                checkPointGroup = listOf(focus.checkpoint),
                onLeafRendered = any(),
                onLeafClick = any()
            )
        } returns Result.success(Unit)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 목록 아이템 클릭
            viewModel.handleIntent(DriveScreenIntent.DriveListItemClick(focus.listItemState))

            // 코스 클릭 ui 변경후 해당 코스로 카메라 이동
            val listItemExpect = initState.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    isLoading = true,
                    overlayGroup = focusCsOverlayGroup,
                    naverMapState = naverMapState.copy(
                        cameraState = naverMapState.cameraState.copy(
                            latLng = focus.course.cameraLatLng,
                            zoom = LIST_ITEM_ZOOM,
                            updateSource = CameraUpdateSource.LIST_ITEM,
                            moveAnimation = MoveAnimation.APP_LINEAR
                        )
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    selectedCourse = focus.course
                )
            }
            val listItemActual = awaitItem()
            assertEquals(listItemExpect, listItemActual)


            // 체크포인트 가져오기 시도 (성공) : 체크포인트 마커 오버레이 추가
            val cpExpect = listItemExpect.run {
                copy(
                    isLoading = false,
                    overlayGroup = focusCsCpOverlayGroup,
                )
            }
            val cpActual = awaitItem()
            assertEquals(cpExpect, cpActual)

            // 체크포인트 확대 (생략)
        }
    }


    // 팝업
    @Test
    fun commentLikeClick() = runTest {
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

        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isVisible = true,
                        commentItemGroup = listOf(likeCommentItemState, normalCommentItemState)
                    )
                )
            )
        }
        val viewModel = initViewModel(StandardTestDispatcher(testScheduler), initState)
        coEvery { updateLikeUseCase(likeCommentItemState.data, true) } returnsMany
                listOf(Result.failure(DomainError.InternalError()), Result.success(Unit))

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
        val commentAddState = CommentAddState(
            commentType = CommentType.ONE
        )
        val commentContent = commentAddState.toCommentContent(checkPoint.checkPointId, editText)
        val addedComment = Comment(
            commentId = "cm1",
            groupId = checkPoint.checkPointId,
            emoji = commentContent.emoji,
            oneLineReview = commentContent.oneLineReview
        )

        val refreshedCheckPoint = checkPoint.copy(
            caption = commentAddState.oneLinePreview
        )

        val updateMarkerInfo = MarkerInfo(
            contentId = checkPoint.checkPointId,
            type = MarkerType.CHECKPOINT,
            caption = commentAddState.oneLinePreview
        )

        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isVisible = true,
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
                listOf(Result.failure(DomainError.InternalError()), Result.success(addedComment))
        coEvery {
            getCheckPointForMarkerUseCase(
                course.courseId,
                listOf(addedComment.groupId)
            )
        } returns
                Result.success(listOf(refreshedCheckPoint))
        coEvery {
            driveMapOverlayService.updateCheckPointLeafCaption(
                refreshedCheckPoint.courseId,
                refreshedCheckPoint.checkPointId,
                refreshedCheckPoint.caption
            )
        } returns Unit
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
        val course = Course(courseId ="cs1")
        val checkPoint = CheckPoint(courseId = course.courseId, checkPointId = "cp1")
        val defaultComment =
            Comment(commentId = "cm1", groupId = checkPoint.checkPointId, oneLineReview = "hello")
        val removeComment =
            Comment(commentId = "cm2", groupId = checkPoint.checkPointId, oneLineReview = "hi")
        val refreshedCheckPoint =
            CheckPoint(courseId = course.courseId, checkPointId = checkPoint.checkPointId, caption = "hello")

        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isVisible = true,
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
            driveMapOverlayService.updateCheckPointLeafCaption(
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
        val course = Course(courseId ="cs1")
        val checkPoint = CheckPoint(courseId =course.courseId, checkPointId = "cp1")
        val defaultComment =
            Comment(commentId = "cm1", groupId = checkPoint.checkPointId, oneLineReview = "hello")
        val reportComment =
            Comment(commentId = "cm2", groupId = checkPoint.checkPointId, oneLineReview = "hi")
        val refreshedCheckPoint = checkPoint.copy(caption = defaultComment.oneLineReview)
        val initState = DriveScreenState().run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isVisible = true,
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
            driveMapOverlayService.updateCheckPointLeafCaption(
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
                        isVisible = false,
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
                            isVisible = true,
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


    // 바텀시트
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
        val checkpointOverlay = MapOverlay.ClusterContainer(
            contentId = addedCheckPoint.courseId,
            cluster = AppCluster(course.courseId)
        )
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
                listOf(Result.failure(DomainError.InternalError()), Result.success(addedCheckPoint))
        coEvery { driveMapOverlayService.addOneTimeMarker(listOf(addedCheckPoint.toMarkerInfo())) } returns Unit
        coEvery { driveMapOverlayService.removeOneTimeMarker(listOf(CHECKPOINT_ADD_MARKER)) } returns Unit
        coEvery {
            driveMapOverlayService.addCheckPointLeaf(
                courseId = addedCheckPoint.courseId,
                checkPoint =  addedCheckPoint,
                onLeafClick = any()
            )
        } returns Result.success(Unit)
        coEvery { driveMapOverlayService.getOverlays() } returns listOf(checkpointOverlay)

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
                    overlayGroup = listOf(checkpointOverlay),
                    stateMode = DriveVisibleMode.CourseDetail,
                    selectedCourse = selectedCourse.copy(
                        checkpointIdGroup = listOf(addedCheckPoint.checkPointId)
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        isUserControl = false
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
        val normalItemState = ListItemState(course = normalCourse, distanceFromCenter = 50)
        val removeItemState = ListItemState(course = reportCourse)
        val normalCourseOverlay =
            normalCourse.toMarkerContainer(AppMarker(normalCourse.toMarkerInfo()))

        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                naverMapState = naverMapState.copy(
                    cameraState = centerCamera
                ),
                listState = listState.copy(listOf(normalItemState, removeItemState)),
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.COURSE_INFO,
                ),
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isVisible = false
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
        coEvery { driveMapOverlayService.removeCourseMarkerAndPath(listOf(reportCourse.courseId)) } returns Unit
        coEvery { driveMapOverlayService.removeCheckPointCluster(reportCourse.courseId) } returns Unit
        coEvery { driveMapOverlayService.showAllOverlays() } returns Unit

        coEvery { getNearByCourseUseCase(centerCamera.latLng, centerCamera.zoom) } returns listOf(
            normalCourse
        )
        coEvery {
            locationService.distance(
                centerCamera.latLng,
                normalCourse.cameraLatLng
            )
        } returns 50
        coEvery { driveMapOverlayService.addCourseMarkerAndPath(listOf(normalCourse)) } returns Unit
        coEvery { driveMapOverlayService.getOverlays() } returns listOf(normalCourseOverlay)

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
                    overlayGroup = listOf(normalCourseOverlay),
                    listState = listState.copy(
                        listItemGroup = listOf(normalItemState)
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        isUserControl = false
                    ),
                    selectedCourse = Course(),
                )
            }
            assertEquals(reportExpect, awaitItem())

            val loadingExpect2 = reportExpect.replaceInfoLoading(false)
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
                checkPoint = reprotCheckpoint,
                reason = "reason"
            )
        } returns Result.success("rp1")
        coEvery { driveMapOverlayService.removeCheckPointLeaf(reprotCheckpoint.courseId, reprotCheckpoint.checkPointId) } returns Unit
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
                    bottomSheetState = bottomSheetState.copy(
                        isUserControl = false
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
        val normalItemState = ListItemState(course = normalCourse, distanceFromCenter = 50)
        val removeItemState = ListItemState(course = removeCourse)
        val normalCourseOverlay =
            normalCourse.toMarkerContainer(AppMarker(normalCourse.toMarkerInfo()))

        val initState = DriveScreenState().run {
            copy(
                stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                naverMapState = naverMapState.copy(
                    cameraState = centerCamera
                ),
                listState = listState.copy(listOf(normalItemState, removeItemState)),
                bottomSheetState = bottomSheetState.copy(
                    content = DriveBottomSheetContent.COURSE_INFO,
                ),
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isVisible = false
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
        coEvery { driveMapOverlayService.removeCourseMarkerAndPath(listOf(removeCourse.courseId)) } returns Unit
        coEvery { driveMapOverlayService.removeCheckPointCluster(removeCourse.courseId) } returns Unit
        coEvery { driveMapOverlayService.showAllOverlays() } returns Unit

        coEvery { getNearByCourseUseCase(centerCamera.latLng, centerCamera.zoom) } returns listOf(
            normalCourse
        )
        coEvery {
            locationService.distance(
                centerCamera.latLng,
                normalCourse.cameraLatLng
            )
        } returns 50
        coEvery { driveMapOverlayService.addCourseMarkerAndPath(listOf(normalCourse)) } returns Unit
        coEvery { driveMapOverlayService.getOverlays() } returns listOf(normalCourseOverlay)

        viewModel.driveScreenState.test {
            assertEquals(initState, awaitItem())

            // @ 인포 삭제 클릭 (by 코스)
            viewModel.handleIntent(DriveScreenIntent.InfoRemoveClick)

            // 로딩 시작
            val loadingExpect = initState.replaceInfoLoading(true)
            assertEquals(loadingExpect, awaitItem())

            // 삭제시도(성공) : ui 변경 및 코스 리프레시
            val removeExpect = loadingExpect.run {
                copy(
                    stateMode = DriveVisibleMode.Explorer,
                    overlayGroup = listOf(normalCourseOverlay),
                    listState = listState.copy(
                        listItemGroup = listOf(normalItemState)
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        isUserControl = false
                    ),
                    selectedCourse = Course()
                )
            }
            assertEquals(removeExpect, awaitItem())

            val loadingExpect2 = removeExpect.replaceInfoLoading(false)
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
            removeCheckPointUseCase(
                courseId = removeCheckpoint.courseId,
                removeCheckpoint.checkPointId
            )
        } returns Result.success(removeCheckpoint.checkPointId)
        coEvery { driveMapOverlayService.removeCheckPointLeaf(removeCheckpoint.courseId,removeCheckpoint.checkPointId) } returns Unit
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
                    bottomSheetState = bottomSheetState.copy(
                        isUserControl = false
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

    private fun initViewModel(
        dispatcher: CoroutineDispatcher,
        state: DriveScreenState
    ): DriveViewModel {
        return DriveViewModel(
            stateInit = state,
            dispatcher = dispatcher,
            errorHandler = MockErrorHandler(),
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
            driveMapOverlayService,
            nativeAdService,
            locationService
        )
    }

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
    private val driveMapOverlayService = mockk<DriveMapOverlayService>()
    private val nativeAdService = mockk<AdService>()
    private val locationService = mockk<LocationService>()

}