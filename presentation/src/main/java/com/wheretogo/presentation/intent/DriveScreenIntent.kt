package com.wheretogo.presentation.intent

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.SheetState
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.state.ListState

sealed class DriveScreenIntent {

    //서치바
    data class AddressItemClick(val searchBarItem: SearchBarItem) : DriveScreenIntent()
    data object SearchBarClick : DriveScreenIntent()
    data object SearchBarClose : DriveScreenIntent()
    data class SearchSubmit(val submit:String) : DriveScreenIntent()

    //지도
    data object MapIsReady : DriveScreenIntent()
    data class CameraUpdated(val cameraState: CameraState) : DriveScreenIntent()
    data class CourseMarkerClick(val overlay: MapOverlay.MarkerContainer) : DriveScreenIntent()
    data class CheckPointMarkerClick(val overlay: MapOverlay.MarkerContainer) : DriveScreenIntent()
    data class ContentPaddingChanged(val amount:Int) : DriveScreenIntent()


    //목록
    data class DriveListItemClick(val itemState: ListState.ListItemState) : DriveScreenIntent()

    //팝업
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
    data class ExportMapAppButtonClick(val result:Result<Unit>) : DriveScreenIntent()
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

    // 공통
    data class LifecycleChange(val event: AppLifecycle) : DriveScreenIntent()
    data object BlurClick : DriveScreenIntent()
}