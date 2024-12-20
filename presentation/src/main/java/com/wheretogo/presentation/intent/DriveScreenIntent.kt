package com.wheretogo.presentation.intent

import androidx.compose.ui.text.input.TextFieldValue
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.OverlayTag
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.state.DriveScreenState.ListState.ListItemState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentAddState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentItemState

sealed class DriveScreenIntent {
    //결과
    object MapIsReady : DriveScreenIntent()
    data class UpdateCamera(val latLng: LatLng, val viewPort: Viewport) : DriveScreenIntent()
    data class UpdateLocation(val latLng: LatLng) : DriveScreenIntent()
    object DismissPopup : DriveScreenIntent()
    data class OverlayRenderComplete(val isRendered: Boolean) : DriveScreenIntent()


    //동작
    data class CourseMarkerClick(val tag: OverlayTag) : DriveScreenIntent()
    data class CheckPointMarkerClick(val tag: OverlayTag) : DriveScreenIntent()
    data class DriveListItemClick(val itemState: ListItemState) : DriveScreenIntent()
    data class DriveListItemBookmarkClick(val itemState: ListItemState) : DriveScreenIntent()
    data class CommentListItemClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentLikeClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentAddClick(val itemState: CommentAddState) : DriveScreenIntent()
    data class CommentEditValueChange(val textFiled: TextFieldValue) : DriveScreenIntent()
    data class CommentEmogiPress(val emogi: String) : DriveScreenIntent()
    data class CommentTypePress(val type: CommentType) : DriveScreenIntent()

    object FoldFloatingButtonClick : DriveScreenIntent()
    object CommentFloatingButtonClick : DriveScreenIntent()
    object ExportMapFloatingButtonClick : DriveScreenIntent()
}