package com.wheretogo.presentation.intent

import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.util.ImageInfo
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.model.TypeEditText
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.ListState.ListItemState

sealed class DriveScreenIntent {
    //가이드
    data class GuidePopupClick(val step: DriveTutorialStep) : DriveScreenIntent()

    //서치바
    data class AddressItemClick(val searchBarItem: SearchBarItem) : DriveScreenIntent()
    data class SearchBarClick(val isSkipAd: Boolean) : DriveScreenIntent()
    data object SearchBarClose : DriveScreenIntent()
    data class SearchSubmit(val submit:String) : DriveScreenIntent()

    //지도
    object MapAsync : DriveScreenIntent()
    data class CameraUpdated(val cameraState: CameraState) : DriveScreenIntent()
    data class MarkerClick(val appMarker: AppMarker) : DriveScreenIntent()


    //목록
    data class DriveListItemClick(val itemState: ListItemState) : DriveScreenIntent()

    //팝업
    data object DismissPopupComment :DriveScreenIntent()
    data class CommentListItemClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentListItemLongClick(val comment: Comment) : DriveScreenIntent()
    data class CommentLikeClick(val itemState: CommentItemState) : DriveScreenIntent()
    data class CommentAddClick(val editText: String) : DriveScreenIntent()
    data class CommentRemoveClick(val comment: Comment) : DriveScreenIntent()
    data class CommentReportClick(val comment: Comment) : DriveScreenIntent()
    data class CommentEmogiPress(val emogi: String) : DriveScreenIntent()
    data class CommentTypePress(val typeEditText: TypeEditText) : DriveScreenIntent()


    // 플로팅 버튼
    data object CommentFloatingButtonClick : DriveScreenIntent()
    data object CheckpointAddFloatingButtonClick : DriveScreenIntent()
    data class InfoFloatingButtonClick(val content: DriveBottomSheetContent) : DriveScreenIntent()
    data object ExportMapFloatingButtonClick : DriveScreenIntent()
    data class ExportMapAppButtonClick(val result:Result<Unit>) : DriveScreenIntent()
    data object FoldFloatingButtonClick : DriveScreenIntent()


    // 바텀시트
    data class BottomSheetChange(val state:SheetVisibleMode) : DriveScreenIntent()
    data class CheckpointLocationSliderChange(val percent: Float) : DriveScreenIntent()
    data class CheckpointDescriptionChange(val text: String) : DriveScreenIntent()
    data object CheckpointDescriptionEnterClick : DriveScreenIntent()
    data class CheckpointImageChange(val imageInfo: ImageInfo) : DriveScreenIntent()
    data object CheckpointSubmitClick : DriveScreenIntent()
    data class InfoReportClick(val reason: String) : DriveScreenIntent()
    data object InfoRemoveClick : DriveScreenIntent()

    // 공통
    data class LifecycleChange(val event: AppLifecycle) : DriveScreenIntent()
    data class EventReceive(val event: AppEvent, val result: Boolean) : DriveScreenIntent()
    data object BlurClick : DriveScreenIntent()
}