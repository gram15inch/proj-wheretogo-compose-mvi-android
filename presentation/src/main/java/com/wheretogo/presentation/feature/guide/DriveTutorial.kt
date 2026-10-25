package com.wheretogo.presentation.feature.guide

import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.dummy.guideCourse
import com.wheretogo.presentation.DriveFloatHighlight
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.GuideState
import com.wheretogo.presentation.state.PopUpState

// 가이드 시작
fun DriveScreenState.toNextState(next: DriveTutorialStep): DriveScreenState {
    return copy(
        guideState = guideState.copy(
            tutorialStep = next
        )
    ).run {
        when (next) {
            DriveTutorialStep.DRIVE_LIST_ITEM_CLICK -> {
                setHighlightItemWithGuide(true)
            }

            DriveTutorialStep.MOVE_TO_LEAF -> {
                setHighlightItemWithGuide(false)
            }

            DriveTutorialStep.COMMENT_FLOAT_CLICK -> {
                copy(
                    floatingButtonState = floatingButtonState.copy(
                        highlight = DriveFloatHighlight.COMMENT
                    )
                )
            }

            DriveTutorialStep.COMMENT_SHEET_DRAG ->{
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isDragGuide = true
                        )
                    )
                )
            }

            DriveTutorialStep.COMMENT_SHEET_CLOSED -> { copy() }

            DriveTutorialStep.EXPORT_FLOAT_CLICK -> {
                copy(
                    popUpState = PopUpState(),
                    floatingButtonState = floatingButtonState.copy(
                        highlight = DriveFloatHighlight.EXPORT,
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    bottomSheetState = BottomSheetState(),
                    stateMode = DriveVisibleMode.BlurCourseDetail
                )
            }

            DriveTutorialStep.EXPORT_SHEET_CLOSED -> {
                copy(
                    floatingButtonState = floatingButtonState.copy(
                        highlight = DriveFloatHighlight.FOLD,
                    )
                )
            }

            DriveTutorialStep.SEARCHBAR_CLICK -> {
                copy(
                    guideState = guideState.copy(
                        alignment = GuideState.Companion.Align.TOP_START
                    ),
                    searchBarState = searchBarState.copy(
                        isHighlight = true,
                        isTextGuide = true
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        highlight = DriveFloatHighlight.NONE
                    ),
                )
            }

            DriveTutorialStep.SEARCHBAR_EDIT -> {
                copy(
                    searchBarState = searchBarState.copy(
                        isHighlight = false,
                        isEditBlock = true
                    )
                )
            }

            DriveTutorialStep.ADDRESS_CLICK -> {
                val highlightItemGroup = searchBarState.searchBarItemGroup.run {
                    mapIndexed { i, item ->
                        if (i == 0)
                            item.copy(isHighlight = true)
                        else
                            item
                    }
                }
                copy(
                    searchBarState = searchBarState.copy(
                        isTextGuide = false,
                        isEditBlock = false,
                        searchBarItemGroup = highlightItemGroup
                    )
                )
            }

            DriveTutorialStep.DRIVE_GUIDE_DONE -> {
                copy(
                    guideState = guideState.copy(
                        isHighlight = true
                    ),
                    searchBarState = searchBarState.copy(
                        isTextGuide = false
                    ),
                    isCongrats = true
                )
            }

            DriveTutorialStep.SKIP -> {
                copy(
                    guideState = guideState.copy(
                        isHighlight = false,
                    )
                )
            }

            else -> {
                this
            }
        }
    }
}

private fun DriveScreenState.setHighlightItemWithGuide(isHighlight: Boolean): DriveScreenState {
    val highlightItemGroup = if (isHighlight) {
        listState.listItemGroup.map { item ->
            if (item.courseRenderItem.title == guideCourse.title)
                item.copy(isHighlight = true)
            else
                item.copy(isHighlight = false)
        }
    } else {
        listState.listItemGroup.map { item ->
            item.copy(isHighlight = false)
        }
    }
    return copy(
        listState = listState.copy(
            listItemGroup = highlightItemGroup
        )
    )
}
