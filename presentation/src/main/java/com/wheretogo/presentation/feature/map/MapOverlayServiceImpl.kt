package com.wheretogo.presentation.feature.map

import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.OverlayImage
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.feature.LocationService
import com.wheretogo.domain.feature.successMap
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.StartDirection
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.presentation.MarkerZIndex
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.model.StringKey
import com.wheretogo.presentation.feature.naver.NaverMapOverlayProvider
import com.wheretogo.presentation.model.AppLeaf
import com.wheretogo.presentation.model.ClusterInfo
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.toBackwardLine
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toForwardLine
import com.wheretogo.presentation.toLeafInfo
import com.wheretogo.presentation.toMarkerInfo
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
    private fun forwardPolylineKey(id: String) = StringKey("${OverlayType.FORWARD_POLYLINE}/${id}")
    private fun backwardPolylineKey(id: String) =
        StringKey("${OverlayType.BACKWORD_POLYLINE}/${id}")

    private fun clusterKey(id: String) = StringKey("${OverlayType.CLUSTER}/${id}")

    private fun <T> updateScope(callback: () -> T): T {
        val r = callback()
        CoroutineScope(Dispatchers.Main).launch {
            _fingerPrintFlow.emit(overlayProvider.getFingerPrint())
        }
        return r
    }

    override fun addCourseMarkerAndPath(courseGroup: List<Course>) {
        courseGroup.forEach { course ->
            course.toMarkerInfo()?.let { overlayProvider.addMarker(courseMarkerKey(course.id), it) }
            overlayProvider.addPolyline(forwardPolylineKey(course.id), course.toForwardLine())
            overlayProvider.addPolyline(backwardPolylineKey(course.id), course.toBackwardLine())
        }
    }

    override fun updateCourseMarkerPosition(courseId: String, position: LatLng) {
        overlayProvider.updateMarkerPosition(courseMarkerKey(courseId), position)
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
                overlayProvider.updateMarkerCaption(key, markerInfo.caption ?: "")

            if (markerInfo.position != null && markerInfo.position != it.markerInfo.position)
                markerInfo.position?.let { latlng ->
                    overlayProvider.updateMarkerPosition(key, latlng)
                }
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

    override fun removeCourseMarkerAndPath(courseIdGroup: List<String>) {
        courseIdGroup.forEach { courseId ->
            overlayProvider.removeOverlay(listOf(courseMarkerKey(courseId)))
            overlayProvider.removeOverlay(listOf(forwardPolylineKey(courseId)))
            overlayProvider.removeOverlay(listOf(backwardPolylineKey(courseId)))
        }
    }

    override fun removeOneTimeMarker(markerIdGroup: List<String>): Unit = updateScope {
        markerIdGroup.map {
            oneTimeMarkerKey(it)
        }.let {
            overlayProvider.removeOverlay(it)
        }
    }

    override fun removeCheckPointCluster(courseId: String) {
        overlayProvider.removeCluster(clusterKey(courseId))
        latestScaleId = ""
    }

    override fun removeCheckPointLeaf(courseId: String, checkPointId: String): Unit = updateScope {
        overlayProvider.removeLeaf(clusterKey(courseId), checkPointId)
        latestScaleId = ""
    }

    override fun focusAndHideOthers(courseId: String, direction: StartDirection): Unit =
        updateScope {
            //주위 스팟 숨기기
            overlays.forEach {
                when (it.type) {
                    OverlayType.COURSE_MARKER -> {
                        overlayProvider.updateVisible(
                            StringKey(it.key),
                            courseMarkerKey(courseId).value == it.key
                        )
                    }

                    OverlayType.FORWARD_POLYLINE -> {
                        overlayProvider.updateVisible(
                            StringKey(it.key),
                            direction == StartDirection.FORWARD && forwardPolylineKey(courseId).value == it.key
                        )
                    }

                    OverlayType.BACKWORD_POLYLINE -> {
                        overlayProvider.updateVisible(
                            StringKey(it.key),
                            direction == StartDirection.REVERSE && backwardPolylineKey(courseId).value == it.key
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

                OverlayType.FORWARD_POLYLINE,
                OverlayType.BACKWORD_POLYLINE -> {
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
        overlayProvider.getCluster(clusterKey(clusterId)).successMap { appCluster ->
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

    //======================================

    override fun refreshSpot(latLng: LatLng) {
        val id = "SIMPLE_SPOT"
        val spotKey = oneTimeMarkerKey(id)
        if (overlayProvider.overlays.none { it.key == spotKey.value }) {
            overlayProvider.addMarker(
                key = spotKey,
                info = MarkerInfo(
                    contentId = id,
                    position = latLng,
                    iconRes = R.drawable.ic_mk_df
                )
            )
        } else {
            overlayProvider.updateMarkerPosition(spotKey, latLng)
        }
    }

    override fun refreshPath(
        course: Course,
    ) = updateScope {
        val fKey = forwardPolylineKey(course.id)
        val bKey = backwardPolylineKey(course.id)
        var fpath: MapOverlay? = null
        var bpath: MapOverlay? = null
        val remove = buildList {
            overlayProvider.overlays.forEach {
                when (it.type) {
                    OverlayType.FORWARD_POLYLINE,
                    OverlayType.BACKWORD_POLYLINE -> {
                        when (it.key) {
                            fKey.value -> fpath = it
                            bKey.value -> bpath = it
                            else -> add(StringKey(it.key))
                        }
                    }

                    else -> {}
                }
            }
        }

        if (fpath == null)
            overlayProvider.addPolyline(fKey, course.toForwardLine())

        if (bpath == null)
            overlayProvider.addPolyline(bKey, course.toBackwardLine())

        overlayProvider.removeOverlay(remove)
    }
}