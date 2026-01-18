package com.wheretogo.presentation.feature.map

import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.OverlayImage
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.FieldInvalidReason
import com.wheretogo.domain.PathType
import com.wheretogo.domain.RouteFieldType
import com.wheretogo.domain.feature.sucessMap
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.MarkerZIndex
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.R
import com.wheretogo.domain.feature.LocationService
import com.wheretogo.presentation.feature.model.StringKey
import com.wheretogo.presentation.feature.naver.NaverMapOverlayProvider
import com.wheretogo.presentation.model.AppLeaf
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.ClusterInfo
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toLeafInfo
import com.wheretogo.presentation.toMarkerInfo
import com.wheretogo.presentation.toPathInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MapOverlayServiceImpl @Inject constructor(
    private val overlayProvider: NaverMapOverlayProvider,
    private val locationService: LocationService
) : MapOverlayService {

    override val overlays: List<MapOverlay> get() = overlayProvider.overlays
    private val _fingerPrintFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    override val fingerPrintFlow: StateFlow<Int> get() = _fingerPrintFlow.asStateFlow()

    private var latestScaleId = ""

    private fun courseMarkerKey(id: String) = StringKey("${OverlayType.COURSE_MARKER}/${id}")
    private fun oneTimeMarkerKey(id: String) = StringKey("${OverlayType.ONE_TIME_MARKER}/${id}")
    private fun coursePathKey(id: String) = StringKey("${OverlayType.FULL_PATH}/${id}")
    private fun clusterKey(id: String) = StringKey("${OverlayType.CLUSTER}/${id}")

    private fun <T> updateScope(callback: () -> T): T {
        val r = callback()
        CoroutineScope(Dispatchers.Main).launch {
            _fingerPrintFlow.emit(overlayProvider.getFingerPrint())
        }
        return r
    }

    override fun addCourseMarkerAndPath(courseGroup: List<Course>) = updateScope {
        courseGroup.forEach { course ->
            overlayProvider.addMarker(courseMarkerKey(course.courseId), course.toMarkerInfo())
            overlayProvider.addPath(coursePathKey(course.courseId), course.toPathInfo())
        }
    }


    override fun addOneTimeMarker(
        markerInfoGroup: List<MarkerInfo>,
        isForceRefresh: Boolean
    ) = updateScope {
        markerInfoGroup.forEach {
            val key = oneTimeMarkerKey(it.contentId)
            val marker = overlayProvider.getMarker(key).getOrNull()
            if (isForceRefresh || marker == null) {
                overlayProvider.addMarker(key, it)
            }
        }
    }

    override fun addCheckPointCluster(
        courseId: String,
        checkPointGroup: List<CheckPoint>,
        onLeafRendered: (Int) -> Unit,
        onLeafClick: (String) -> Unit
    ): Result<Unit> = updateScope {
        runCatching {
            val clusterInfo = ClusterInfo(
                courseId, checkPointGroup.map { it.toLeafInfo() })
            overlayProvider.addCluster(
                clusterKey(courseId), clusterInfo, onLeafRendered, onLeafClick
            )
            Unit
        }
    }

    override fun addCheckPointLeaf(
        courseId: String,
        checkPoint: CheckPoint,
        onLeafClick: (String) -> Unit
    ): Result<Unit> = updateScope {
        runCatching {
            val appLeaf = checkPoint.toLeafInfo()
            overlayProvider.addLeaf(clusterKey(courseId), appLeaf, onLeafClick)
        }
    }

    override fun updateOneTimeMarker(markerInfo: MarkerInfo): Unit = updateScope {
        val key = oneTimeMarkerKey(markerInfo.contentId)
        overlayProvider.getMarker(key).onSuccess {
            if (markerInfo.caption != null && markerInfo.caption != it.markerInfo.caption)
                overlayProvider.updateMarkerCaption(key, markerInfo.caption)

            if (markerInfo.position != null && markerInfo.position != it.markerInfo.position)
                overlayProvider.updateMarkerPosition(key, markerInfo.position)
        }
    }

    override fun updateCheckPointLeafCaption(
        clusterId: String,
        leafId: String,
        caption: String
    ): Unit = updateScope {
        overlayProvider.getCluster(clusterKey(clusterId)).onSuccess {
            it.updateLeafCaption(leafId, caption)
        }
    }

    override fun removeCourseMarkerAndPath(courseIdGroup: List<String>): Unit = updateScope {
        courseIdGroup.forEach { courseId ->
            overlayProvider.removeOverlay(listOf(courseMarkerKey(courseId)))
            overlayProvider.removeOverlay(listOf(coursePathKey(courseId)))
        }
    }

    override fun removeOneTimeMarker(markerIdGroup: List<String>): Unit = updateScope {
        markerIdGroup.map {
            oneTimeMarkerKey(it)
        }.let {
            overlayProvider.removeOverlay(it)
        }
    }

    override fun removeCheckPointCluster(courseId: String): Unit = updateScope {
        overlayProvider.removeCluster(clusterKey(courseId))
        latestScaleId = ""
    }

    override fun removeCheckPointLeaf(courseId: String, checkPointId: String): Unit = updateScope {
        overlayProvider.removeLeaf(clusterKey(courseId), checkPointId)
        latestScaleId = ""
    }

    override fun focusAndHideOthers(course: Course): Unit = updateScope {
        //주위 스팟 숨기기
        overlays.forEach {
            when (it.type) {
                OverlayType.COURSE_MARKER -> {
                    overlayProvider.updateVisible(
                        StringKey(it.key),
                        courseMarkerKey(course.courseId).value == it.key
                    )
                }

                OverlayType.FULL_PATH -> {
                    overlayProvider.updateVisible(
                        StringKey(it.key),
                        coursePathKey(course.courseId).value == it.key
                    )
                }

                else -> {}
            }
        }
    }

    override fun showAllOverlays(): Unit = updateScope {
        overlays.forEach {
            when (it.type) {
                OverlayType.COURSE_MARKER -> {
                    overlayProvider.updateVisible(
                        StringKey(it.key),
                        true
                    )
                }

                OverlayType.FULL_PATH -> {
                    overlayProvider.updateVisible(
                        StringKey(it.key),
                        true
                    )
                }

                else -> {}
            }
        }
    }

    override fun scaleToPointLeafInCluster(
        clusterId: String,
        point: LatLng
    ): Result<String> = updateScope {
        overlayProvider.getCluster(clusterKey(clusterId)).sucessMap { appCluster ->
            runCatching {
                val leafGroup = appCluster.getAppLeafGroup()
                val minLeaf = leafGroup.minByOrNull {
                    it.leaf.position.toDomainLatLng().toDistance(point)
                }

                if (minLeaf == null)
                    return@updateScope Result.failure(DomainError.NotFound("min leaf not found"))

                if (latestScaleId == minLeaf.leaf.leafId)
                    return@updateScope Result.success(latestScaleId)

                if (latestScaleId.isNotBlank()) {
                    leafGroup.firstOrNull { it.leaf.leafId == latestScaleId }?.setRevertIcon()
                }

                overlayProvider.scaleUp(
                    clusterKey(clusterId),
                    minLeaf.leaf.leafId,
                    minLeaf.leaf.thumbnail
                )
                latestScaleId = minLeaf.leaf.leafId
                latestScaleId
            }
        }
    }

    override fun clear(): Unit = updateScope {
        overlayProvider.clear()
        latestScaleId = ""
    }

    private fun LatLng.toDistance(latLng: LatLng): Float {
        val from = latLng
        val to = this
        val distance = locationService.distanceFloat(from, to)
        return distance
    }

    private fun AppLeaf.setUpIcon(oi: OverlayImage) {
        latestScaleId = leaf.leafId
        coreMarker?.apply {
            icon = oi
            zIndex = MarkerZIndex.PHOTO_ZOOM.ordinal
            captionTextSize = 16f
            setCaptionAligns(Align.Top)
        }
    }

    private fun AppLeaf.setRevertIcon() {
        coreMarker?.apply {
            icon = leaf.overlayImage
            zIndex = MarkerZIndex.PHOTO.ordinal
            captionTextSize = 14f
            setCaptionAligns(Align.Bottom)
        }
    }


    //=============================== CourseAdd

    private val WAYPOINT_PATH_ID = "WAYPOINT_PATH"

    override fun addWaypoint(latlng: LatLng): Boolean = updateScope {
        val markerGroup = overlays.filter { it is AppMarker }
        if (markerGroup.size < 5) {
            val id = latlng.hashCode().toString()
            overlayProvider.addMarker(
                oneTimeMarkerKey(id), MarkerInfo(
                    id,
                    latlng,
                    iconRes = R.drawable.ic_mk_df
                )
            )
            return@updateScope true
        }
        return@updateScope false
    }

    override fun removeWaypoint(id: String) = updateScope {
        overlayProvider.removeOverlay(listOf(oneTimeMarkerKey(id)))
        val markerGroup = overlays.filter { it is AppMarker }
        if (markerGroup.size < 2) {
            overlayProvider.removeOverlay(listOf(coursePathKey(WAYPOINT_PATH_ID)))
        }
    }

    override fun moveWaypoint(id: String, latlng: LatLng): Unit = updateScope {
        val key = oneTimeMarkerKey(id)
        overlayProvider.getMarker(key).onSuccess {
            overlayProvider.updateMarkerPosition(key, latlng)
        }
    }

    override fun hideWaypoint(id: String) = updateScope {
        val key = oneTimeMarkerKey(id)
        overlayProvider.getMarker(key).onSuccess {
            it.replaceVisible(false)
        }
        overlayProvider.removeOverlay(listOf(coursePathKey(WAYPOINT_PATH_ID)))
    }

    override fun createScaffoldPath(): Result<Unit> = updateScope {
        return@updateScope runCatching {
            val waypoints =
                overlays.mapNotNull { if (it is AppMarker) it.markerInfo.position else null }

            if (waypoints.isEmpty()) {
                return@updateScope Result.failure(AppError.Ignore())
            }
            val id = WAYPOINT_PATH_ID
            PathInfo(
                contentId = id,
                points = waypoints,
                type = PathType.SCAFFOLD,
            )
        }.sucessMap { pathInfo ->
            overlayProvider.updatePath(coursePathKey(WAYPOINT_PATH_ID), pathInfo).run {
                if (this.exceptionOrNull() is DomainError.NotFound) {
                    overlayProvider.addPath(coursePathKey(WAYPOINT_PATH_ID), pathInfo)
                    Result.success(Unit)
                } else
                    this
            }
        }
    }

    override fun createFullPath(points: List<LatLng>): Result<Unit> = updateScope {
        return@updateScope runCatching {
            if (points.size < 2) {
                return@updateScope Result.failure(
                    DomainError.RouteFieldInvalid(
                        RouteFieldType.POINT,
                        FieldInvalidReason.MIN
                    )
                )
            }

            val id = WAYPOINT_PATH_ID
            val pathInfo = PathInfo(
                contentId = id,
                points = points,
                type = PathType.FULL
            )
            pathInfo
        }.sucessMap { pathInfo ->
            overlayProvider.updatePath(coursePathKey(WAYPOINT_PATH_ID), pathInfo).run {
                if (this.exceptionOrNull() is DomainError.NotFound) {
                    overlayProvider.addPath(coursePathKey(WAYPOINT_PATH_ID), pathInfo)
                    Result.success(Unit)
                } else
                    this
            }
        }.map { }
    }

}