package com.wheretogo.presentation.intent

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.wheretogo.domain.model.map.OverlayTag
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.DriveScreenState.ListState.ListItemState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentAddState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentItemState

sealed class DriveScreenIntent {

    //지도
    data object MapIsReady : DriveScreenIntent()
    data class UpdateCamera(val cameraState: CameraState) : DriveScreenIntent()
    data class OverlayRenderComplete(val isRendered: Boolean) : DriveScreenIntent()
    data class CourseMarkerClick(val tag: OverlayTag) : DriveScreenIntent()
    data class CheckPointMarkerClick(val tag: OverlayTag) : DriveScreenIntent()

    //목록
    data class DriveListItemClick(val itemState: ListItemState) : DriveScreenIntent()
    data class DriveListItemBookmarkClick(val itemState: ListItemState) : DriveScreenIntent()

    //팝업
    data object DismissPopup : DriveScreenIntent()
    data class CommentListItemClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentListItemLongClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentLikeClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentAddClick(val itemState: CommentAddState) : DriveScreenIntent()
    data class CommentRemoveClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentReportClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentEditValueChange(val textFiled: TextFieldValue) : DriveScreenIntent()
    data class CommentEmogiPress(val emogi: String) : DriveScreenIntent()
    data class CommentTypePress(val type: CommentType) : DriveScreenIntent()


    // 플로팅 버튼
    data object CommentFloatingButtonClick : DriveScreenIntent()
    data object CheckpointAddFloatingButtonClick : DriveScreenIntent()
    data object ExportMapFloatingButtonClick : DriveScreenIntent()
    data object FoldFloatingButtonClick : DriveScreenIntent()


    // 바텀시트
    data object BottomSheetClose : DriveScreenIntent()
    data class CheckpointLocationSliderChange(val percent: Float) : DriveScreenIntent()
    data class CheckpointDescriptionChange(val text: String) : DriveScreenIntent()
    data object CheckpointDescriptionEnterClick : DriveScreenIntent()
    data class CheckpointImageChange(val imgUri: Uri?) : DriveScreenIntent()

}