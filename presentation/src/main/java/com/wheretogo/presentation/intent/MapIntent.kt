package com.wheretogo.presentation.intent

import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.domain.model.map.MoveCameraOption
import com.wheretogo.domain.model.map.RefreshContentOption
import com.wheretogo.domain.model.map.RefreshOverlayOption
import com.wheretogo.presentation.AppLifecycle

sealed class MapIntent {
    object MapAsync : MapIntent()
    data class CameraUpdated(val cameraState: CameraState) : MapIntent()
    data class MarkerClick(val markerInfo: MarkerInfo) : MapIntent()
    data class MoveCamera(val option: MoveCameraOption) : MapIntent()
    data class RefreshContent(val option: RefreshContentOption) : MapIntent()
    data class RefreshOverlay(val option: RefreshOverlayOption) : MapIntent()
    data class Focus(val course: Course) : MapIntent()
    data object RELEASE : MapIntent()
    data object ClearMap : MapIntent()
    data class LifeCycleChange(val lifecycle: AppLifecycle) : MapIntent()
}
