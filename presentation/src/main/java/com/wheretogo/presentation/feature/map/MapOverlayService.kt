package com.wheretogo.presentation.feature.map

import com.wheretogo.domain.model.course.Course
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo

open class MapOverlayService {
    protected val _overlays = mutableMapOf<String, MapOverlay>() // OverlayType.toKey

    protected fun Course.toSpotKey(): String = courseId + OverlayType.SPOT_MARKER.name
    protected fun Course.toPathKey(): String = courseId + OverlayType.PATH.name
    protected fun MarkerInfo.toKey(): String = contentId + OverlayType.ONE_TIME_MARKER.name
    protected fun OverlayType.toKey(id: String) = id + name
}