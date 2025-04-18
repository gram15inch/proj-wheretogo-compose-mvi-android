package com.wheretogo.presentation.feature.map

import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.model.MapOverlay

open class MapOverlayService {
    protected val _overlays = mutableMapOf<String, MapOverlay>()
    val overlays: Collection<MapOverlay> = _overlays.values

    protected fun Course.toSpotKey(): String = courseId + OverlayType.SPOT.name
    protected fun Course.toPathKey(): String = courseId + OverlayType.PATH.name
    protected fun CheckPoint.toKey(): String = checkPointId + OverlayType.CHECKPOINT.name
    protected fun OverlayType.toKey(id: String) = id + name

}