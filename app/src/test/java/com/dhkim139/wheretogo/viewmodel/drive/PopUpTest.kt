package com.dhkim139.wheretogo.viewmodel.drive

import com.dhkim139.wheretogo.feature.MainDispatcherRule
import com.dhkim139.wheretogo.feature.assertFlows
import com.google.common.truth.Truth.assertThat
import com.wheretogo.data.repositoryimpl.MapContentRepositoryImpl
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.map.ContentOperation
import com.wheretogo.domain.model.map.SlideItem
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.comment.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.comment.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.repository.DefaultMapId
import com.wheretogo.domain.usecase.app.DriveTutorialUseCase
import com.wheretogo.domain.usecase.report.ReportContentUseCase
import com.wheretogo.domain.usecase.util.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.util.UpdateLikeUseCase
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.event.DriveEvent.RefreshContent
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.TypeEditText
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.toItemState
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
class PopUpTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val initState = DriveScreenState(isObserveSetting = false)
    private val observeSettingsUseCase = mockk<ObserveSettingsUseCase>()
    private val getImageForPopupUseCase = mockk<GetImageForPopupUseCase>()
    private val getCommentForCheckPointUseCase = mockk<GetCommentForCheckPointUseCase>()
    private val removeCommentToCheckPointUseCase = mockk<RemoveCommentToCheckPointUseCase>()
    private val reportContentUseCase = mockk<ReportContentUseCase>()
    private val updateLikeUseCase = mockk<UpdateLikeUseCase>()
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
            getCommentForCheckPointUseCase = getCommentForCheckPointUseCase,
            getImageForPopupUseCase = getImageForPopupUseCase,
            addCheckpointToCourseUseCase = mockk(),
            addCommentToCheckPointUseCase = mockk(),
            removeCourseUseCase = mockk(),
            removeCheckPointUseCase = mockk(),
            removeCommentToCheckPointUseCase = removeCommentToCheckPointUseCase,
            reportContentUseCase = reportContentUseCase,
            updateLikeUseCase = updateLikeUseCase,
            searchKeywordUseCase = mockk(),
            driveTutorialUseCase = driveTutorialUseCase,
            signOutUseCase = mockk(),
            clearCacheUseCase = mockk(),
            nativeAdService = mockk(),
            mapContentRepository = MapContentRepositoryImpl()
        )
    }

    // ==================== dismissCommentPopUp 테스트 ====================
    @Test
    fun `댓글 팝업 닫기시 플로팅 버튼 보이기`() = runTest {
        // Arrange
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState.createShowPopupCommentState())

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 댓글 팝업 닫기
            viewModel.handleIntent(DriveScreenIntent.DismissPopupComment)

            // Assert: 플로팅 버튼 보이기
            state.awaitItem().run {
                assertThat(floatingButtonState.stateMode).isEqualTo(DriveFloatingVisibleMode.Popup)
                assertThat(popUpState.commentState.commentItemGroup).isEqualTo(null)
            }
        }
    }


    // ==================== popupImageSlide 테스트 ====================
    @Test
    fun `팝업 이미지 슬라이드시 이미지 및 댓글 갱신`() = runTest {
        // Arrange
        val slideItem0 = SlideItem("CP000", imageId = "IM000", url = "https://CP000.jpg")
        val slideItem1 = SlideItem("CP001", imageId = "IM001")
        val item1Url = "https://CP001.jpg"

        val showPopupImageAndCommentState = initState.run {
            copy(
                popUpState = popUpState.copy(
                    initPage = 0,
                    slideItems = listOf(slideItem0,slideItem1),
                    commentState = popUpState.commentState.copy(
                        isContentVisible = true
                    )
                ),
                floatingButtonState = floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Popup
                )
            )
        }

        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), showPopupImageAndCommentState)

        coEvery { getImageForPopupUseCase(slideItem1.imageId!!) } returns item1Url
        coEvery { getCommentForCheckPointUseCase(slideItem1.contentId!!) } returns Result.success(emptyList())

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 팝업 이미지 슬라이드
            viewModel.handleIntent(DriveScreenIntent.PopupImageSlide(1))

            // Assert: 이미지 및 댓글 갱신
            state.awaitItem().run {
                assertThat(popUpState.slideItems).isEqualTo(listOf(slideItem0, slideItem1.copy(url=item1Url)))
                assertThat(popUpState.commentState.commentItemGroup).isEqualTo(emptyList<CommentState.CommentItemState>())
            }
        }
    }

    // ==================== commentListItemClick 테스트 ====================
    @Test
    fun `댓글 클릭(디테일 O)후 재클릭시 댓글 더보기 확장,축소`() = runTest {

        // Arrange
        val commentItem = CommentState.CommentItemState(
            data = Comment(
                commentId = "CP001",
                detailedReview = "더보기"
            ),
            isFold = false
        )
        val initState= initState.createCommentStateWithCommentItem(listOf(commentItem))
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)


        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 댓글 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentListItemClick(commentItem))

            // Assert: 더보기 확장
            state.awaitItem().run {
                assertThat(popUpState.commentState.commentItemGroup?.firstOrNull()?.isFold).isEqualTo(true)
            }

            // Act: 댓글 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentListItemClick(commentItem))

            // Assert: 더보기 축소
            state.awaitItem().run {
                assertThat(popUpState.commentState.commentItemGroup?.firstOrNull()?.isFold).isEqualTo(false)
            }
        }
    }

    @Test
    fun `댓글 클릭(디테일 X)시 댓글 더보기 확장 하지 않음`() = runTest {
        // Arrange
        val commentItem = CommentState.CommentItemState(
            data = Comment(
                commentId = "CP001",
                detailedReview = ""
            ),
            isFold = false
        )
        val initState= initState.createCommentStateWithCommentItem(listOf(commentItem))

        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)


        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 댓글 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentListItemClick(commentItem))

            // Assert: 더보기 확장하지 않음
        }
    }

    // ==================== commentListItemLongClick 테스트 ====================
    @Test
    fun `댓글 길게 클릭시 댓글 설정 표시`() = runTest {
        // Arrange
        val comment = Comment(
            commentId = "CP001"
        )

        val initState = initState.createCommentStateWithCommentItem(listOf(comment.toItemState()))

        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 댓글 길게 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentListItemLongClick(comment))
            // Assert: 댓글 설정표시
            state.awaitItem().run {
                assertThat(popUpState.commentState.commentSettingState?.comment?.commentId)
                    .isEqualTo(comment.commentId)
            }
        }
    }

    // ==================== commentLikeClick 테스트 ====================
    @Test
    fun `댓글 좋아요 아이콘(off) 클릭시 하트색 채우기 및 좋아요 상승 `() = runTest {
        // Arrange
        val commentItem = CommentState.CommentItemState(
            data = Comment("CM001", like = 1, isUserLiked = false)
        )
        val initState = initState.createCommentStateWithCommentItem(listOf(commentItem))
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { updateLikeUseCase(commentItem.data, true) } returns Result.success(Unit)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 댓글 좋아요 아이콘 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentLikeClick(commentItem))
            // Assert: 하트색 채우기 및 좋아요 상승
            state.awaitItem().run {
                val commentItem = popUpState.commentState.commentItemGroup?.firstOrNull()!!
                assertThat(commentItem.data.isUserLiked).isEqualTo(true)
                assertThat(commentItem.data.like).isEqualTo(2)
                assertThat(commentItem.isLoading).isEqualTo(true)
            }

            state.awaitItem().run {
                assertThat(popUpState.commentState.commentItemGroup?.firstOrNull()?.isLoading)
                    .isEqualTo(false)
            }
        }
    }

    @Test
    fun `댓글 좋아요 아이콘(on) 클릭시 하트색 비우기 및 좋아요 하락 `() = runTest {
        // Arrange
        val commentItem = CommentState.CommentItemState(
            data = Comment("CM001", like = 3, isUserLiked = true)
        )
        val initState = initState.createCommentStateWithCommentItem(listOf(commentItem))
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { updateLikeUseCase(commentItem.data, false) } returns Result.success(Unit)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 댓글 좋아요 아이콘 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentLikeClick(commentItem))
            // Assert: 하트색 채우기 및 좋아요 상승
            state.awaitItem().run {
                val commentItem = popUpState.commentState.commentItemGroup?.firstOrNull()!!
                assertThat(commentItem.data.isUserLiked).isEqualTo(false)
                assertThat(commentItem.data.like).isEqualTo(2)
                assertThat(commentItem.isLoading).isEqualTo(true)
            }

            state.awaitItem().run {
                assertThat(popUpState.commentState.commentItemGroup?.firstOrNull()?.isLoading)
                    .isEqualTo(false)
            }
        }
    }

    // ==================== commentAddClick 테스트 ====================
    @Test
    fun `댓글 추가 클릭시 댓글 갱신 `() = runTest {
        // Arrange
        val commentItem = CommentState.CommentItemState(
            data = Comment("CM001", like = 3, isUserLiked = true)
        )
        val initState = initState.createCommentStateWithCommentItem(listOf(commentItem))
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act: 댓글 좋아요 아이콘 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentLikeClick(commentItem))
            // Assert: 하트색 채우기 및 좋아요 상승
            state.awaitItem().run {
                val comment = popUpState.commentState.commentItemGroup?.firstOrNull()?.data!!
                assertThat(comment.isUserLiked).isEqualTo(false)
                assertThat(comment.like).isEqualTo(2)
            }
        }
    }

    // ==================== commentRemoveClick 테스트 ====================
    @Test
    fun `댓글 삭제 버튼 클릭시 댓글 아이템 삭제 및 클러스터 갱신`() = runTest {
        // Arrange
        val comment1 = Comment("CM001","CPOO1") // 삭제
        val comment2 = Comment("CM002","CPOO1")

        val initState = initState.createCommentStateWithCommentItem(listOf(comment1.toItemState(),comment2.toItemState()))
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { removeCommentToCheckPointUseCase(comment1.groupId,comment1.commentId) } returns Result.success(Unit)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act:  댓글 삭제 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentRemoveClick(comment1))

            // Assert: 댓글 아이템 삭제
            state.awaitItem().run {
                popUpState.commentState.commentItemGroup.let {
                    assertThat(it?.size).isEqualTo(1)
                    assertThat(it?.firstOrNull()?.data?.commentId).isEqualTo(comment2.commentId)
                }
                assertThat(popUpState.commentState.commentSettingState).isNull()
            }

            // Assert: 클러스터 갱신
            (event.awaitItem() as RefreshContent).run {
                assertThat(option.operation).isEqualTo(ContentOperation.REFRESH_CLUSTER)
                assertThat(option.id).isEqualTo(DefaultMapId.SELECT_COURSE_ID.name)
            }
        }
    }

    // ==================== commentReportClick 테스트 ====================
    @Test
    fun `댓글 신고 버튼 클릭시 댓글 아이템 삭제 및 클러스터 갱신`() = runTest {
        // Arrange
        val comment1 = Comment("CM001","CPOO1", userId = "US001", userName = "USER1") // 신고
        val comment2 = Comment("CM002","CPOO1", userId = "US002", userName = "USER2")
        val reportReason = ReportReason.VIOLENCE
        val initState = initState.createCommentStateWithCommentItem(listOf(comment1.toItemState(),comment2.toItemState()))
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        coEvery { reportContentUseCase(comment1.toReportContent(reportReason)) } returns Result.success(Unit)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act:  댓글 신고 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentReportClick(comment1, reportReason))

            // Assert: 댓글 아이템 삭제
            state.awaitItem().run {
                popUpState.commentState.commentItemGroup.let {
                    assertThat(it?.size).isEqualTo(1)
                    assertThat(it?.firstOrNull()?.data?.commentId).isEqualTo(comment2.commentId)
                }
                assertThat(popUpState.commentState.commentSettingState).isNull()
            }

            // Assert: 클러스터 갱신
            (event.awaitItem() as RefreshContent).run {
                assertThat(option.operation).isEqualTo(ContentOperation.REFRESH_CLUSTER)
                assertThat(option.id).isEqualTo(DefaultMapId.SELECT_COURSE_ID.name)
            }
        }
    }

    // ==================== commentEmogiPress 테스트 ====================
    @Test
    fun `댓글 한줄평 타입 버튼 클릭시 대표 아이콘 표시`() = runTest {
        // Arrange
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)
        val emoji ="^^"
        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act:  댓글 추가 이모지 아이템 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentEmogiPress(emoji))

            // Assert: 대표 아이콘 표시
            state.awaitItem().run {
                assertThat(popUpState.commentState.commentAddState.titleEmoji).isEqualTo(emoji)
            }
        }
    }

    // ==================== commentTypePress 테스트 ====================
    @Test
    fun `댓글 한줄평 타입 버튼 클릭시 한줄평 미리보기 숨기기 및 상세리뷰 저장`() = runTest {
        // Arrange
        val typePress = TypeEditText(CommentType.ONE, "편집중인 상세리뷰 텍스트")
        val initState = initState.run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentAddState = CommentState.CommentAddState(
                            commentType = CommentType.DETAIL,
                            isOneLinePreview = true
                        )
                    )
                )
            )
        }
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act:  댓글 한줄평 타입 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentTypePress(typePress))

            // Assert: 한줄평 미리보기 숨기기 및 상세리뷰 저장
            state.awaitItem().run {
                assertThat(popUpState.commentState.commentAddState.commentType).isEqualTo(CommentType.ONE)
                assertThat(popUpState.commentState.commentAddState.isOneLinePreview).isEqualTo(false)
                assertThat(popUpState.commentState.commentAddState.detailReview).isEqualTo(typePress.editText)
            }
        }
    }

    @Test
    fun `댓글 상세리뷰 타입 버튼 클릭시 한줄평 미리보기 표시 및 한줄평 저장`() = runTest {
        // Arrange
        val typePress = TypeEditText(CommentType.DETAIL, "편집중인 한불평 텍스트")
        val initState = initState.run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentAddState = CommentState.CommentAddState(
                            commentType = CommentType.ONE,
                            isOneLinePreview = false
                        )
                    )
                )
            )
        }
        val viewModel =
            createViewModel(StandardTestDispatcher(testScheduler), initState)

        assertFlows(viewModel.driveScreenState, viewModel.driveEvent) {
            // Act:  댓글 상세리뷰 타입 버튼 클릭
            viewModel.handleIntent(DriveScreenIntent.CommentTypePress(typePress))

            // Assert: 한줄평 미리보기 표시 및 한줄평 저장
            state.awaitItem().run {
                assertThat(popUpState.commentState.commentAddState.commentType).isEqualTo(CommentType.DETAIL)
                assertThat(popUpState.commentState.commentAddState.isOneLinePreview).isEqualTo(true)
                assertThat(popUpState.commentState.commentAddState.oneLineReview).isEqualTo(typePress.editText)
            }
        }
    }

}