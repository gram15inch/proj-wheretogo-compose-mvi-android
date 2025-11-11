package com.wheretogo.presentation.feature.map

import com.naver.maps.map.overlay.OverlayImage
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.MarkerZIndex
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.PathType
import com.wheretogo.presentation.feature.geo.LocationService
import com.wheretogo.presentation.feature.naver.NaverMapOverlayStore
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toMarkerContainer
import com.wheretogo.presentation.toMarkerInfo
import javax.inject.Inject

class DriveMapOverlayService @Inject constructor(
    private val overlayStore: NaverMapOverlayStore,
    private val overlayModifier: NaverMapOverlayModifier,
    private val locationService: LocationService
) :
    MapOverlayService() {

    fun getOverlays(): Collection<MapOverlay> = _overlays.values

    fun addCourse(courseGroup: List<Course>) {
        courseGroup.forEach { course ->
            // 마커 추가
            if (_overlays[course.toSpotKey()] == null) {
                val markerContainer = course.toMarkerContainer(
                    marker = overlayStore.getMarker(
                        contentId = course.courseId,
                        initMarker = {
                            overlayModifier.createMarker(course.toMarkerInfo())
                        }
                    )
                )
                _overlays[course.toSpotKey()] = markerContainer
            }


            // 경로 추가
            if (_overlays[course.toPathKey()] == null) {
                val pathInfo = PathInfo(
                    contentId = course.courseId,
                    points = course.points,
                    minZoomLevel = OverlayType.PATH.minZoomLevel()
                )
                overlayStore.getPath(
                    contentId = course.courseId,
                    initPath = {
                        overlayModifier.createPath(pathInfo)
                    }
                ).onSuccess {
                    val pathContainer =
                        MapOverlay.PathContainer(course.courseId, PathType.FULL, it)
                    _overlays[course.toPathKey()] = pathContainer
                }
            }
        }

    }

    fun addCheckPoint(checkPointGroup: List<CheckPoint>, isForceRefresh: Boolean = false) {
        _overlays.apply {
            checkPointGroup.forEach {
                if (isForceRefresh || _overlays[it.toKey()] == null) {
                    val marker = it.toMarkerContainer(
                        overlayStore.getMarker(
                            it.checkPointId
                        ){
                            overlayModifier.createMarker(it.toMarkerInfo())
                        }
                    )
                    _overlays[it.toKey()] = marker
                }
            }
        }
    }

    fun updateMarker(markerInfo: MarkerInfo) {
        if (markerInfo.type == MarkerType.CHECKPOINT) {
            val key = OverlayType.CHECKPOINT.toKey(markerInfo.contentId)
            _overlays[key].apply {
                if (this is MapOverlay.MarkerContainer) {
                    val appMarker = this.marker
                    if (markerInfo.caption != null && markerInfo.caption != appMarker.markerInfo.caption)
                        overlayStore.updateMarkerCaption(markerInfo.contentId, markerInfo.caption)

                    if (markerInfo.position != null && markerInfo.position != appMarker.markerInfo.position)
                        overlayStore.updateMarkerPosition(markerInfo.contentId, markerInfo.position)
                }
            }
        }
    }

    fun focusCourse(course: Course) {
        //주위 스팟 숨기기
        _overlays.forEach {
            when (val overlay = it.value) {
                is MapOverlay.MarkerContainer -> {
                    overlayStore.updateMarkerVisible(
                        overlay.contentId,
                        overlay.contentId == course.courseId
                    )
                }

                is MapOverlay.PathContainer -> {
                    overlayStore.updatePathVisible(
                        overlay.contentId,
                        overlay.contentId == course.courseId
                    )
                }
            }
        }
    }

    fun showAll() {
        _overlays.values.forEach {
            when (val overlay = it) {
                is MapOverlay.MarkerContainer -> {
                    when (overlay.type) {
                        MarkerType.SPOT -> {
                            overlayStore.updateMarkerVisible(
                                overlay.contentId,
                                true
                            )
                        }

                        MarkerType.CHECKPOINT -> {}
                    }

                }

                is MapOverlay.PathContainer -> {
                    overlayStore.updatePathVisible(
                        overlay.contentId,
                        true
                    )
                }
            }
        }
    }

    fun removeCourse(courseIdGroup: List<String>) {
        courseIdGroup.forEach { courseId ->
            _overlays.remove(OverlayType.SPOT.toKey(courseId))
            _overlays.remove(OverlayType.PATH.toKey(courseId))
            overlayStore.remove(courseId)
        }
    }

    fun removeCheckPoint(checkPointIdGroup: List<String>) {
        checkPointIdGroup.forEach {
            _overlays.remove(OverlayType.CHECKPOINT.toKey(it))
            overlayStore.remove(it)
        }
    }

    fun clearCheckPoint() {
        val checkPointIdGroup = _overlays.mapNotNull {
            val mapOverlay = it.value
            if (mapOverlay is MapOverlay.MarkerContainer) {
                if (mapOverlay.type == MarkerType.CHECKPOINT)
                    return@mapNotNull it.value.contentId
            }
            null
        }
        removeCheckPoint(checkPointIdGroup)
    }

    fun clear(){
        overlayStore.clear()
        _overlays.clear()
    }

    fun scaleToPointMarker(
        point: LatLng,
        overlays: List<MapOverlay>,
        markerType: MarkerType = MarkerType.CHECKPOINT
    ) {
        runCatching {
            if (overlays.isEmpty())
                return

            var minMarker: Pair<Float, MapOverlay.MarkerContainer>? = null
            var latestMinMarker: Pair<Float, MapOverlay.MarkerContainer>? = null
            val checkPointMarkerGroup = overlays.mapNotNull {
                if (it is MapOverlay.MarkerContainer
                    && it.marker.markerInfo.type == markerType
                    && it.contentId != CHECKPOINT_ADD_MARKER
                ) {
                    it
                } else {
                    null
                }
            }
            checkPointMarkerGroup.forEach {
                val container = it.toDistancePair(point)
                if (container?.second?.marker?.coreMarker?.tag == true)
                    latestMinMarker = container
                if (container == null)
                    return
                when {
                    minMarker == null -> {
                        minMarker = container
                    }

                    minMarker.first > container.first -> {
                        if (container.second.marker.markerInfo.type == MarkerType.CHECKPOINT)
                            minMarker = container
                    }
                }
            }
            if (minMarker == null)
                return

            if (latestMinMarker == null)
                overlayModifier.scaleUp(minMarker.second.marker.markerInfo)?.let {
                    minMarker.second.setUpIcon(it)
                }
            else {
                if (latestMinMarker.second.marker.markerInfo.contentId != minMarker.second.marker.markerInfo.contentId) {
                    overlayModifier.scaleDown(latestMinMarker.second.marker.markerInfo).let {
                        latestMinMarker.second.setDownIcon(it)
                    }
                    overlayModifier.scaleUp(minMarker.second.marker.markerInfo)?.let {
                        minMarker.second.setUpIcon(it)
                    }
                }
            }
        }
    }

    private fun MapOverlay.MarkerContainer.toDistancePair(latLng: LatLng): Pair<Float, MapOverlay.MarkerContainer>? {
        val from = latLng
        val to = this.marker.markerInfo.position
        if (to == null)
            return null
        val distance = locationService.distanceFloat(from, to)
        return distance to this
    }

    private fun MapOverlay.MarkerContainer.setUpIcon(oi: OverlayImage) {
        marker.coreMarker?.icon = oi
        marker.coreMarker?.tag = true
        marker.coreMarker?.zIndex = MarkerZIndex.PHOTO_ZOOM.ordinal
    }

    private fun MapOverlay.MarkerContainer.setDownIcon(oi: OverlayImage) {
        marker.coreMarker?.icon = oi
        marker.coreMarker?.tag = false
        marker.coreMarker?.zIndex = MarkerZIndex.PHOTO.ordinal
    }
}