package com.wheretogo.presentation.state

import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.CommentState.CommentAddState
import java.util.EnumSet


data class DriveScreenState(
    val guideState: GuideState = GuideState(),
    val searchBarState: SearchBarState = SearchBarState(),
    val naverMapState: NaverMapState = NaverMapState(),
    val listState: ListState = ListState(),
    val popUpState: PopUpState = PopUpState(),
    val bottomSheetState: BottomSheetState = BottomSheetState(),
    val floatingButtonState: FloatingButtonState = FloatingButtonState(),
    val stateMode: DriveVisibleMode = DriveVisibleMode.Explorer,
    val overlayGroup: List<MapOverlay> = emptyList(),
    val fingerPrint: Int = 0,
    val selectedCourse: Course = Course(),
    val selectedCheckPoint: CheckPoint = CheckPoint(),
    val isLoading: Boolean = false,
    val isCongrats: Boolean = false,
    val error: String? = null
) {

    companion object {
        val searchBarVisible: EnumSet<DriveVisibleMode> =
            EnumSet.of(DriveVisibleMode.Explorer, DriveVisibleMode.SearchBarExpand)
        val bottomSheetVisible: EnumSet<DriveVisibleMode> =
            EnumSet.of(DriveVisibleMode.BottomSheetExpand, DriveVisibleMode.BlurBottomSheetExpand)
        val floatingVisible: EnumSet<DriveVisibleMode> = EnumSet.of(
            DriveVisibleMode.CourseDetail,
            DriveVisibleMode.BlurCourseDetail,
            DriveVisibleMode.BlurCheckpointDetail
        )
        val itemListVisible: EnumSet<DriveVisibleMode> = EnumSet.of(DriveVisibleMode.Explorer)
        val popUpVisible: EnumSet<DriveVisibleMode> =
            EnumSet.of(DriveVisibleMode.BlurCheckpointDetail)
        val imeBoxVisible: EnumSet<DriveVisibleMode> =
            EnumSet.of(DriveVisibleMode.BottomSheetExpand)
        val blurVisible: EnumSet<DriveVisibleMode> = EnumSet.of(
            DriveVisibleMode.BlurCourseDetail,
            DriveVisibleMode.BlurCheckpointDetail,
            DriveVisibleMode.BlurBottomSheetExpand
        )

        fun infoContent(mode: DriveVisibleMode): DriveBottomSheetContent =
            when {
                infoCourseContent.contains(mode) -> DriveBottomSheetContent.COURSE_INFO
                infoCheckpointContent.contains(mode) -> DriveBottomSheetContent.CHECKPOINT_INFO
                else -> DriveBottomSheetContent.EMPTY
            }

        val infoCourseContent: EnumSet<DriveVisibleMode> =
            EnumSet.of(DriveVisibleMode.CourseDetail, DriveVisibleMode.BlurCourseDetail)
        val infoCheckpointContent: EnumSet<DriveVisibleMode> =
            EnumSet.of(DriveVisibleMode.BlurCheckpointDetail)
    }

    // 초기화
    fun initSearchBar(): DriveScreenState {
        return copy(
            searchBarState = searchBarState.copy(
                isActive = false,
                isLoading = false,
                isEmptyVisible = false,
                searchBarItemGroup = emptyList(),
                adItemGroup = emptyList()
            )
        )
    }

    fun initCheckPointAddState(): DriveScreenState {
        val newCheckPointAddState = bottomSheetState.checkPointAddState.run {
            copy(
                latLng = selectedCourse.points.first(),
                sliderPercent = 0.0f,
            )
        }
        return copy(
            bottomSheetState = bottomSheetState.copy(
                checkPointAddState = newCheckPointAddState,
            )
        )
    }

    fun initInfoState(): DriveScreenState {
        val infoState = when (bottomSheetState.content) {
            DriveBottomSheetContent.COURSE_INFO -> {

                bottomSheetState.infoState.copy(
                    isRemoveButton = selectedCourse.isUserCreated,
                    isReportButton = true,
                    createdBy = selectedCourse.userName
                )
            }

            DriveBottomSheetContent.CHECKPOINT_INFO -> {
                bottomSheetState.infoState.copy(
                    isRemoveButton = selectedCheckPoint.isUserCreated,
                    isReportButton = true,
                    createdBy = selectedCheckPoint.userName
                )
            }

            else -> bottomSheetState.infoState
        }

        return copy(
            bottomSheetState = bottomSheetState.copy(
                infoState = infoState
            )
        )
    }

    fun initCommentAddState(): DriveScreenState{
       return run {
            copy(
                popUpState= popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentAddState = CommentAddState()
                    )
                )
            )
        }
    }


    // 교체
    fun replaceCommentListLoading(
        isLoading: Boolean,
    ): DriveScreenState {
        return copy(
            popUpState = popUpState.copy(
                commentState = popUpState.commentState.copy(
                    isLoading = isLoading
                )
            )
        )
    }


    fun replaceCommentLoading(
        commentId: String,
        isLoading: Boolean,
    ): DriveScreenState {
        val newCommentGroup =
            popUpState.commentState.commentItemGroup.map {
                if (it.data.commentId == commentId) it.copy(isLoading = isLoading) else it
            }

        return copy(
            popUpState = popUpState.copy(
                commentState = popUpState.commentState.copy(
                    commentItemGroup = newCommentGroup
                )
            )
        )
    }

    fun replaceCommentSettingLoading(
        isLoading: Boolean
    ): DriveScreenState {
        return run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentSettingState =
                            popUpState.commentState.commentSettingState.copy(
                                isLoading = isLoading,
                            )

                    )
                )
            )
        }
    }

    fun replaceCommentSettingVisible(
        isVisible: Boolean
    ): DriveScreenState {
        return run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentSettingState = popUpState.commentState.commentSettingState.copy(
                            isVisible = isVisible,
                        )
                    )
                )
            )
        }
    }

    fun replaceCommentAddStateLoading(
        isLoading: Boolean
    ): DriveScreenState {
        return run {
            val old = popUpState.commentState.commentAddState
            if (isLoading) {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = old.copy(
                                isLargeEmogi = true,
                                isLoading = true
                            )
                        )
                    )
                )
            } else {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = old.copy(
                                isLargeEmogi = true,
                                isLoading = false
                            )
                        )
                    )
                )
            }
        }

    }

    fun replaceCheckpointAddLoading(
        isLoading: Boolean
    ): DriveScreenState {
        return run {
            copy(
                bottomSheetState = bottomSheetState.copy(
                    checkPointAddState = bottomSheetState.checkPointAddState.copy(
                        isLoading = isLoading
                    )
                )
            )
        }
    }

    fun replaceScreenLoading(
        isLoading: Boolean
    ): DriveScreenState {
        return run {
            copy(isLoading = isLoading)
        }
    }

    fun replaceInfoLoading(
        isLoading: Boolean
    ): DriveScreenState {
        return run {
            copy(
                bottomSheetState = bottomSheetState.copy(
                    infoState = bottomSheetState.infoState.copy(
                        isLoading = isLoading
                    )
                )
            )
        }
    }

    fun replaceSearchBarLoading(
        isLoading: Boolean
    ): DriveScreenState {
        return run {
            copy(
                searchBarState = searchBarState.copy(
                    isLoading = isLoading
                )
            )
        }
    }

}