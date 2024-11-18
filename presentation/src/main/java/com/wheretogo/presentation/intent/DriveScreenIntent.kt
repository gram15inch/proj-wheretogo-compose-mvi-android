package com.wheretogo.presentation.intent

import com.wheretogo.domain.model.Comment
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.MarkerTag
import com.wheretogo.domain.model.Viewport

sealed class DriveScreenIntent {
    //결과
    object MapIsReady : DriveScreenIntent()
    data class UpdateCamera(val latLng: LatLng, val viewPort: Viewport) : DriveScreenIntent()
    data class UpdateLocation(val latLng: LatLng) : DriveScreenIntent()
    object DismissPopup : DriveScreenIntent()

    //동작
    data class CourseMarkerClick(val tag: MarkerTag) : DriveScreenIntent()
    data class CheckPointMarkerClick(val tag: MarkerTag) : DriveScreenIntent()
    data class DriveListItemClick(val journey: Journey) : DriveScreenIntent()
    data class CommentListItemClick(val comment: Comment) : DriveScreenIntent()
    data class CommentLikeClick(val comment: Comment) : DriveScreenIntent()

    object FoldFloatingButtonClick : DriveScreenIntent()
    object CommentFloatingButtonClick : DriveScreenIntent()
    object ExportMapFloatingButtonClick : DriveScreenIntent()

}