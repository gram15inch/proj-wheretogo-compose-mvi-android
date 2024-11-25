package com.wheretogo.presentation.intent

import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MarkerTag

import com.wheretogo.domain.model.map.Viewport

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
    data class DriveListItemBookmarkClick(val journey: Journey) : DriveScreenIntent()

    object FoldFloatingButtonClick : DriveScreenIntent()
    object CommentFloatingButtonClick : DriveScreenIntent()
    object ExportMapFloatingButtonClick : DriveScreenIntent()

}