package com.wheretogo.presentation.intent

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.SheetState
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.DriveScreenState.ListState.ListItemState
import com.wheretogo.presentation.state.InfoState

sealed class DriveScreenIntent {

    //서치바
    data class AddressItemClick(val searchBarItem: SearchBarItem) : DriveScreenIntent()
    data class SearchToggleClick(val isBar:Boolean) : DriveScreenIntent()
    data class SearchSubmit(val submit:String) : DriveScreenIntent()

    //지도
    data object MapIsReady : DriveScreenIntent()
    data class CameraUpdated(val cameraState: CameraState) : DriveScreenIntent()
    data class CourseMarkerClick(val overlay: MapOverlay.MarkerContainer) : DriveScreenIntent()
    data class CheckPointMarkerClick(val overlay: MapOverlay.MarkerContainer) : DriveScreenIntent()
    data class ContentPaddingChanged(val amount:Int) : DriveScreenIntent()


    //목록
    data class DriveListItemClick(val itemState: ListItemState) : DriveScreenIntent()

    //팝업
    data object DismissPopup : DriveScreenIntent()
    data object DismissPopupComment :DriveScreenIntent()
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
    data object InfoFloatingButtonClick : DriveScreenIntent()
    data object ExportMapFloatingButtonClick : DriveScreenIntent()
    data object FoldFloatingButtonClick : DriveScreenIntent()


    // 바텀시트
    data class BottomSheetChange(val state:SheetState) : DriveScreenIntent()
    data class CheckpointLocationSliderChange(val percent: Float) : DriveScreenIntent()
    data class CheckpointDescriptionChange(val text: String) : DriveScreenIntent()
    data object CheckpointDescriptionEnterClick : DriveScreenIntent()
    data class CheckpointImageChange(val imgUri: Uri?) : DriveScreenIntent()
    data object CheckpointSubmitClick : DriveScreenIntent()
    data class InfoReportClick(val infoState: InfoState) : DriveScreenIntent()
    data class InfoRemoveClick(val infoState: InfoState) : DriveScreenIntent()
}