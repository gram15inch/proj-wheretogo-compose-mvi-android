package com.wheretogo.presentation.feature.map

import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.RouteCategory
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.PathType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SEARCH_MARKER
import com.wheretogo.presentation.feature.naver.NaverMapOverlayStore
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toIcRes
import com.wheretogo.presentation.toNaver
import javax.inject.Inject

class DriveMapOverlayService @Inject constructor(private val overlayStore: NaverMapOverlayStore) :
    MapOverlayService() {

    fun addCourse(courseGroup: List<Course>) {
        courseGroup.forEach { course ->
            // 마커 추가
            if (_overlays[course.toSpotKey()] == null) {
                val markerContainer = MapOverlay.MarkerContainer(
                    course.courseId, MarkerType.SPOT,
                    overlayStore.getOrCreateMarker(
                        markerInfo = MarkerInfo(
                            contentId = course.courseId,
                            position = course.waypoints.first(),
                            iconRes = RouteCategory.fromCode(course.type)?.item.toIcRes()
                        )
                    )
                )
                _overlays[course.toSpotKey()] = markerContainer
            }


            // 경로 추가
            if (_overlays[course.toPathKey()] == null) {
                overlayStore.getOrCreatePath(
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
                    val icon = when (it.checkPointId) {
                        CHECKPOINT_ADD_MARKER -> R.drawable.ic_mk_cm
                        SEARCH_MARKER -> R.drawable.ic_mk_df
                        else -> null
                    }
                    val marker = MapOverlay.MarkerContainer(
                        it.checkPointId, MarkerType.CHECKPOINT,
                        overlayStore.getOrCreateMarker(
                            markerInfo = MarkerInfo(
                                contentId = it.checkPointId,
                                position = it.latLng,
                                caption = it.caption,
                                iconPath = it.imageLocalPath.ifEmpty { null },
                                iconRes = icon
                            )
                        )
                    )
                    _overlays[it.toKey()] = marker
                }
            }
        }
    }

    fun updateMarker(markerInfo: MarkerInfo) {
        if (markerInfo.type == MarkerType.CHECKPOINT) {
            _overlays[OverlayType.CHECKPOINT.toKey(markerInfo.contentId)].apply {
                if (this is MapOverlay.MarkerContainer) {
                    markerInfo.caption?.let { marker.captionText = it }
                    markerInfo.position?.let { marker.position = it.toNaver() }
                }
            }
        }
    }

    fun focusCourse(course: Course) {
        //주위 스팟 숨기기
        _overlays.forEach {
            when (val overlay = it.value) {
                is MapOverlay.MarkerContainer -> {
                    overlay.marker.isVisible = overlay.id == course.courseId
                }

                is MapOverlay.PathContainer -> {
                    overlay.path.isVisible = overlay.id == course.courseId
                }
            }
        }
    }

    fun showAll() {
        overlays.forEach {
            when (val overlay = it) {
                is MapOverlay.MarkerContainer -> {
                    when (overlay.type) {
                        MarkerType.SPOT -> {
                            overlay.marker.isVisible = true
                        }

                        MarkerType.CHECKPOINT -> {}
                    }

                }

                is MapOverlay.PathContainer -> {
                    overlay.path.isVisible = true
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

    fun clearCheckPoint(){
        val checkPointIdGroup  = _overlays.mapNotNull {
            val mapOverlay = it.value
            if(mapOverlay is MapOverlay.MarkerContainer){
                if(mapOverlay.type == MarkerType.CHECKPOINT)
                   return@mapNotNull it.value.id
            }
            null
        }
        removeCheckPoint(checkPointIdGroup)
    }
}