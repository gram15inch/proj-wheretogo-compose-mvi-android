package com.wheretogo.presentation.feature.map

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import kotlinx.coroutines.flow.StateFlow

interface MapOverlayService {

    val overlays: List<MapOverlay>
    val fingerPrintFlow: StateFlow<Int>
    fun addCourseMarkerAndPath(courseGroup: List<Course>)

    fun addOneTimeMarker(markerInfoGroup: List<MarkerInfo>, isForceRefresh: Boolean = false)

    fun addCheckPointCluster(
        courseId: String,
        checkPointGroup: List<CheckPoint>,
        onLeafRendered: (Int) -> Unit = {},
        onLeafClick: (String) -> Unit = {}
    ): Result<Unit>

    fun addCheckPointLeaf(
        courseId: String,
        checkPoint: CheckPoint,
        onLeafClick: (String) -> Unit = {}
    ): Result<Unit>

    fun updateOneTimeMarker(markerInfo: MarkerInfo)

    fun updateCheckPointLeafCaption(clusterId: String, leafId: String, caption: String)

    fun removeCourseMarkerAndPath(courseIdGroup: List<String>)

    fun removeOneTimeMarker(markerIdGroup: List<String>)

    fun removeCheckPointCluster(courseId: String)

    fun removeCheckPointLeaf(courseId: String, checkPointId: String)

    fun focusAndHideOthers(course: Course)

    fun showAllOverlays()

    fun scaleToPointLeafInCluster(
        clusterId: String,
        point: LatLng
    ): Result<String>

    fun clear()

    //===============================

    fun addWaypoint(latlng: LatLng): Boolean

    fun removeWaypoint(id: String)

    fun moveWaypoint(id: String, latlng: LatLng)


    fun hideWaypoint(id: String)

    fun createScaffoldPath(): Result<Unit>

    fun createFullPath(points: List<LatLng> = emptyList()): Result<Unit>

}