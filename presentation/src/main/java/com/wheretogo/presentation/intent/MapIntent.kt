package com.wheretogo.presentation.intent

import com.dhkim139.core.ui.model.AppLifecycle
import com.wheretogo.domain.model.course.CourseRenderItem
import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.domain.model.map.MoveCameraOption
import com.wheretogo.domain.model.map.RefreshContentOption
import com.wheretogo.domain.model.map.RefreshOverlayOption

sealed class MapIntent {
    object MapAsync : MapIntent()
    data object FetchCourse : MapIntent()
    data class CameraUpdated(val cameraState: CameraState) : MapIntent()
    data class MarkerClick(val markerInfo: MarkerInfo) : MapIntent()
    data class MoveCamera(val option: MoveCameraOption) : MapIntent()
    data class RefreshContent(val option: RefreshContentOption) : MapIntent()
    data class RefreshOverlay(val option: RefreshOverlayOption) : MapIntent()
    data class Focus(val item: CourseRenderItem) : MapIntent()
    data object RELEASE : MapIntent()
    data object ClearMap : MapIntent()
    data class LifeCycleChange(val lifecycle: AppLifecycle) : MapIntent()
}
