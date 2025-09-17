package com.wheretogo.presentation.feature.map

import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.PathType
import com.wheretogo.presentation.feature.naver.NaverMapOverlayStore
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toMarkerContainer
import com.wheretogo.presentation.toMarkerInfo
import javax.inject.Inject

class DriveMapOverlayService @Inject constructor(private val overlayStore: NaverMapOverlayStore) :
    MapOverlayService() {

    fun getOverlays(): Collection<MapOverlay> = _overlays.values

    fun addCourse(courseGroup: List<Course>) {
        courseGroup.forEach { course ->
            // 마커 추가
            if (_overlays[course.toSpotKey()] == null) {
                val markerContainer = course.toMarkerContainer(
                    overlayStore.getOrCreateMarker(
                        course.toMarkerInfo()
                    )
                )
                _overlays[course.toSpotKey()] = markerContainer
            }


            // 경로 추가
            if (_overlays[course.toPathKey()] == null) {
                overlayStore.createAppPath(
                    pathInfo = PathInfo(
                        contentId = course.courseId,
                        points = course.points,
                        minZoomLevel = OverlayType.PATH.minZoomLevel()
                    )
                )?.let {
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
                        overlayStore.getOrCreateMarker(
                            it.toMarkerInfo()
                        )
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
}