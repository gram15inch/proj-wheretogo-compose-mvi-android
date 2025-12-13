package com.wheretogo.presentation.feature.map

import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.OverlayImage
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.MarkerZIndex
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.PathType
import com.wheretogo.presentation.feature.geo.LocationService
import com.wheretogo.presentation.feature.naver.LeafItem
import com.wheretogo.presentation.feature.naver.NaverMapOverlayModifier
import com.wheretogo.presentation.feature.naver.NaverMapOverlayStore
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.AppLeaf
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toMarkerContainer
import com.wheretogo.presentation.toMarkerInfo
import com.wheretogo.presentation.toNaver
import javax.inject.Inject

class DriveMapOverlayService @Inject constructor(
    private val overlayStore: NaverMapOverlayStore,
    private val overlayModifier: NaverMapOverlayModifier,
    private val locationService: LocationService
) :
    MapOverlayService() {
    var latestScaleId = ""
    fun getOverlays(): Collection<MapOverlay> = _overlays.values

    fun addCourseMarkerAndPath(courseGroup: List<Course>) {
        courseGroup.forEach { course ->
            // 마커 추가
            if (_overlays[course.toSpotKey()] == null) {
                overlayStore.getMarker(
                    contentId = course.courseId,
                    initMarker = {
                        overlayModifier.createMarker(course.toMarkerInfo())
                    }
                ).onSuccess {
                    val overlay = course.toMarkerContainer(
                        marker = it
                    )
                    _overlays[course.toSpotKey()] = overlay
                }
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

    fun addOneTimeMarker(markerInfoGroup: List<MarkerInfo>, isForceRefresh: Boolean = false) {
        _overlays.apply {
            markerInfoGroup.forEach {
                if (isForceRefresh || _overlays[it.toKey()] == null) {
                    overlayStore.getMarker(
                        it.contentId
                    ) { overlayModifier.createMarker(it) }
                        .onSuccess { appMarker ->
                            val marker = MapOverlay.MarkerContainer(
                                contentId = it.contentId,
                                type = MarkerType.DEFAULT,
                                marker = appMarker
                            )
                            _overlays[it.toKey()] = marker
                        }
                }
            }
        }
    }

    fun addCheckPointCluster(
        courseId: String,
        checkPointGroup: List<CheckPoint>,
        onLeafRendered: (Int) -> Unit = {},
        onLeafClick: (String) -> Unit = {}
    ): Result<Unit> {
        return runCatching {
            val leafItemGroup = checkPointGroup.associate {
                val overlayImage =
                    overlayModifier.createOverlayImage(it.thumbnail)
                LeafItem(
                    leafId = it.checkPointId,
                    latLng = it.latLng.toNaver(),
                    caption = it.caption,
                    thumbnail = it.thumbnail,
                    overlayImage = overlayImage,
                    onLeafClick = onLeafClick
                ) to null
            }
            val cluster = overlayStore.createCluster(
                clusterId = courseId,
                leafItemGroup = leafItemGroup,
                onLeafRendered = onLeafRendered
            )
            _overlays[OverlayType.CLUSTER_MARKER.toKey(courseId)] =
                MapOverlay.ClusterContainer(courseId, cluster)
            Unit
        }
    }

    fun addCheckPointLeaf(
        courseId: String,
        checkPoint: CheckPoint,
        onLeafClick: (String) -> Unit = {}
    ): Result<Unit> {
        return runCatching {
            overlayStore.addLeaf(courseId, checkPoint.toLeafItem(onLeafClick))
        }
    }

    fun updateOneTimeMarker(markerInfo: MarkerInfo) {
        if (markerInfo.type != MarkerType.DEFAULT)
            return
        val key = markerInfo.toKey()
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

    fun updateCheckPointLeafCaption(clusterId:String, leafId:String, caption:String){
        overlayStore.getCluster(clusterId).onSuccess {
            it.updateLeafCaption(leafId, caption)
        }
    }

    fun removeCourseMarkerAndPath(courseIdGroup: List<String>) {
        courseIdGroup.forEach { courseId ->
            _overlays.remove(OverlayType.SPOT_MARKER.toKey(courseId))
            _overlays.remove(OverlayType.PATH.toKey(courseId))
            overlayStore.removeMarkerAndPath(courseId)
        }
    }

    fun removeOneTimeMarker(markerIdGroup: List<String>) {
        overlayStore.removeMarker(markerIdGroup)
        markerIdGroup.forEach {
            _overlays.remove(OverlayType.ONE_TIME_MARKER.toKey(it))
        }
    }

    fun removeCheckPointCluster(courseId: String){
        overlayStore.removeCluster(courseId)
        _overlays.remove(OverlayType.CLUSTER_MARKER.toKey(courseId))
        latestScaleId = ""
    }

    fun removeCheckPointLeaf(courseId:String, checkPointId: String){
        overlayStore.removeLeaf(courseId,checkPointId)
        latestScaleId = ""
    }

    fun focusAndHideOthers(course: Course) {
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
                else -> {}
            }
        }
    }

    fun showAllOverlays() {
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

                        else -> {}
                    }

                }

                is MapOverlay.PathContainer -> {
                    overlayStore.updatePathVisible(
                        overlay.contentId,
                        true
                    )
                }
                else -> {}
            }
        }
    }

    fun scaleToPointLeafInCluster(
        clusterId: String,
        point: LatLng
    ) {
        runCatching {
            overlayStore.getCluster(clusterId).onSuccess {
                 appCluster ->
                val leafGroup = appCluster.getAppLeafGroup()
                val minLeaf = leafGroup.minByOrNull {
                    it.leaf.position.toDomainLatLng().toDistance(point)
                }

                if (minLeaf == null)
                    return

                if (latestScaleId == minLeaf.leaf.leafId)
                    return

                if (latestScaleId.isNotBlank()) {
                    leafGroup.firstOrNull{it.leaf.leafId == latestScaleId}?.setRevertIcon()
                }
                overlayModifier.scaleUp(minLeaf.leaf.thumbnail)?.let {
                    minLeaf.setUpIcon(it)
                }
            }

        }
    }

    fun clear(){
        overlayStore.clear()
        _overlays.clear()
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

    private fun CheckPoint.toLeafItem(
        onLeafClick: (String) -> Unit = {}
    ): LeafItem{
        val overlayImage =
            overlayModifier.createOverlayImage(thumbnail)
        return LeafItem(
            leafId = checkPointId,
            latLng = latLng.toNaver(),
            caption = caption,
            thumbnail = thumbnail,
            overlayImage = overlayImage,
            onLeafClick = onLeafClick
        )
    }
}