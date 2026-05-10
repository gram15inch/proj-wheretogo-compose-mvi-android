package com.dhkim139.wheretogo.viewmodel.drive

import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.model.AdItem
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.SearchBarState

fun DriveScreenState.createShowPopupCommentState(isImeVisible: Boolean = false) =
    run {
        copy(
            popUpState = popUpState.copy(
                commentState = CommentState(
                    isContentVisible = true,
                    commentItemGroup = emptyList(),
                    isImeVisible = isImeVisible
                )
            ),
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.Hide
            )
        )
    }

fun  DriveScreenState.createCommentStateWithCommentItem(item: List<CommentState.CommentItemState>) =
    run {
        copy(
            popUpState = popUpState.copy(
                commentState = popUpState.commentState.copy(
                    commentItemGroup = item
                )
            )
        )
    }

fun DriveScreenState.createShowCheckPointAddBottomSheetState(
    sliderPercent: Float = 0f,
    description: String = "",
    imgUriString: String = "",
    isSubmitActive: Boolean = false
) = run {
        copy(
            stateMode = DriveVisibleMode.BottomSheetExpand,
            bottomSheetState = bottomSheetState.copy(
                content = DriveBottomSheetContent.CHECKPOINT_ADD,
                checkPointAddState = bottomSheetState.checkPointAddState.copy(
                    sliderPercent = sliderPercent,
                    imgUriString = imgUriString,
                    description = description,
                    isSubmitActive = isSubmitActive
                )
            )
        )
    }

fun DriveScreenState.createShowCheckPointInfoInfoBottomSheet() =
    run {
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

fun DriveScreenState.createShowCourseInfoInfoBottomSheet() =
    run {
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


fun DriveScreenState.createShowSearchBarExpandState(adItems: List<AdItem> = emptyList()) =
    run {
        copy(
            stateMode = DriveVisibleMode.SearchBarExpand,
            searchBarState = SearchBarState(
                isActive = true,
                adItemGroup = adItems
            )
        )
    }

fun DriveScreenState.createExportMapAppFloatExpandState(adItems: List<AdItem> = emptyList()) =
    run {
        copy(
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.ExportExpand,
                adItemGroup = adItems
            )
        )
    }