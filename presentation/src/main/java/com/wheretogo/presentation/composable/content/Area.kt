package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.presentation.DriveVisibleMode


@Composable
fun ZIndexOfDriveContentArea(
    tutorialStep: DriveTutorialStep,
    visibleMode: DriveVisibleMode,
    content: @Composable (DriveContentZIndex, Boolean, Boolean) -> Unit
) {
    var drivelistZIndex by remember { mutableFloatStateOf(0f) }
    var searchbarZIndex by remember { mutableFloatStateOf(0f) }
    var floatingZIndex by remember { mutableFloatStateOf(0f) }
    var popupZIndex by remember { mutableFloatStateOf(0f) }
    var bottomSheetZIndex by remember { mutableFloatStateOf(0f) }
    var isBlock by remember { mutableStateOf(false) }
    var isCover by remember { mutableStateOf(false) }

    if (tutorialStep != DriveTutorialStep.SKIP)
        LaunchedEffect(tutorialStep) {
            when (tutorialStep) {
                DriveTutorialStep.DRIVE_LIST_ITEM_CLICK -> {
                    isBlock = true
                    isCover = true
                    drivelistZIndex = 2f
                    searchbarZIndex = 0f
                    floatingZIndex = 0f
                    popupZIndex = 0f
                    bottomSheetZIndex = 0f
                }

                DriveTutorialStep.SEARCHBAR_CLICK -> {
                    isBlock = true
                    isCover = true
                    drivelistZIndex = 0f
                    searchbarZIndex = 2f
                    floatingZIndex = 0f
                    popupZIndex = 0f
                    bottomSheetZIndex = 0f
                }

                DriveTutorialStep.EXPORT_FLOAT_CLICK,
                DriveTutorialStep.COMMENT_FLOAT_CLICK,
                DriveTutorialStep.FOLD_FLOAT_CLICK -> {
                    isBlock = true
                    isCover = true
                    drivelistZIndex = 0f
                    searchbarZIndex = 0f
                    floatingZIndex = 2f
                    popupZIndex = 0f
                    bottomSheetZIndex = 0f
                }

                DriveTutorialStep.COMMENT_SHEET_DRAG -> {
                    drivelistZIndex = 0f
                    searchbarZIndex = 0f
                    floatingZIndex = 0f
                    popupZIndex = 2f
                    bottomSheetZIndex = 0f
                }

                else -> {
                    isBlock = false
                    isCover = false
                    drivelistZIndex = 1f
                    searchbarZIndex = 1f
                    floatingZIndex = 1f
                    popupZIndex = 1f
                    bottomSheetZIndex = 1f
                }
            }
        }
    else
        LaunchedEffect(visibleMode) {
            when (visibleMode) {
                DriveVisibleMode.BlurCheckpointBottomSheetExpand -> {
                    isBlock = false
                    isCover = false
                    drivelistZIndex = 0f
                    searchbarZIndex = 0f
                    floatingZIndex = 0f
                    popupZIndex = 0f
                    bottomSheetZIndex = 2f
                }

                else -> {
                    isBlock = false
                    isCover = false
                    drivelistZIndex = 1f
                    searchbarZIndex = 1f
                    floatingZIndex = 1f
                    popupZIndex = 1f
                    bottomSheetZIndex = 1f
                }
            }
        }
    content(
        DriveContentZIndex(
            drivelistZIndex,
            searchbarZIndex,
            floatingZIndex,
            popupZIndex,
            bottomSheetZIndex
        ), isBlock, isCover
    )
}

data class DriveContentZIndex(
    val driveList: Float,
    val searchBar: Float,
    val floatBtn: Float,
    val mapPopup: Float,
    val bottomSheet: Float,
)

@Composable
fun ActiveOfFloatBtnArea(
    tutorialStep: DriveTutorialStep,
    content: @Composable (FloatBtnActive) -> Unit
) {
    var checkpointActive by remember { mutableStateOf(true) }
    var commentActive by remember { mutableStateOf(true) }
    var infoZActive by remember { mutableStateOf(true) }
    var exportActive by remember { mutableStateOf(true) }
    var foldZActive by remember { mutableStateOf(true) }

    LaunchedEffect(tutorialStep) {
        when (tutorialStep) {
            DriveTutorialStep.MOVE_TO_LEAF,
            DriveTutorialStep.LEAF_CLICK -> {
                checkpointActive = false
                commentActive = false
                infoZActive = false
                exportActive = false
                foldZActive = false
            }

            DriveTutorialStep.EXPORT_FLOAT_CLICK -> {
                checkpointActive = false
                commentActive = false
                infoZActive = false
                exportActive = true
                foldZActive = false
            }

            DriveTutorialStep.COMMENT_FLOAT_CLICK -> {
                checkpointActive = false
                commentActive = true
                infoZActive = false
                exportActive = false
                foldZActive = false
            }

            DriveTutorialStep.FOLD_FLOAT_CLICK -> {
                checkpointActive = false
                commentActive = false
                infoZActive = false
                exportActive = false
                foldZActive = true
            }

            else -> {
                checkpointActive = true
                commentActive = true
                infoZActive = true
                exportActive = true
                foldZActive = true
            }
        }
    }
    content(
        FloatBtnActive(
            checkpointActive,
            commentActive,
            infoZActive,
            exportActive,
            foldZActive
        )
    )
}


data class FloatBtnActive(
    val checkpoint: Boolean,
    val comment: Boolean,
    val info: Boolean,
    val export: Boolean,
    val fold: Boolean
)

@Composable
fun OneHandArea(content: @Composable BoxScope.() -> Unit) {
    val min = minOf(screenSize(true), 800.dp)
    Box(
        modifier = Modifier
            .sizeIn(maxWidth = min)
    ) {
        content()
    }
}


@Composable
fun ExtendArea(
    modifier: Modifier = Modifier,
    isExtend: Boolean,
    paddingHorizontalWhenExtend: Dp = 0.dp,
    holdContent: @Composable () -> Unit,
    moveContent: @Composable () -> Unit
) {
    if (isExtend) {
        Row(
            modifier.padding(horizontal = paddingHorizontalWhenExtend),
            verticalAlignment = Alignment.Top
        ) {
            holdContent()
            moveContent()
        }
    } else {
        Box(
            modifier = modifier.graphicsLayer(clip = true),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)) {
                holdContent()
            }
            moveContent()
        }
    }

}