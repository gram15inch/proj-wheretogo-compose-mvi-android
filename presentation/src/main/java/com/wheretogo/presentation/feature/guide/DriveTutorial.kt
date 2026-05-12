package com.wheretogo.presentation.feature.guide

import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.dummy.guideCourse
import com.wheretogo.presentation.DriveFloatHighlight
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.GuideState


fun DriveScreenState.toStepState(step: DriveTutorialStep): DriveScreenState {
    return copy(
        guideState = guideState.copy(
            tutorialStep = step
        )
    ).run {
        when (step) {
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

            DriveTutorialStep.COMMENT_SHEET_DRAG -> {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isDragGuide = true
                        )
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        highlight = DriveFloatHighlight.NONE
                    )
                )
            }

            DriveTutorialStep.EXPORT_FLOAT_CLICK -> {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isDragGuide = false
                        )
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        highlight = DriveFloatHighlight.EXPORT
                    ),
                )
            }

            DriveTutorialStep.FOLD_FLOAT_CLICK -> {
                copy(
                    guideState = guideState.copy(
                        alignment = GuideState.Companion.Align.BOTTOM_START
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        highlight = DriveFloatHighlight.FOLD
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
            if (item.course.courseId == guideCourse.courseId)
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
