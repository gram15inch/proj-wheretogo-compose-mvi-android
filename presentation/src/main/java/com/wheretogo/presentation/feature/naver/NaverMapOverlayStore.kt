package com.wheretogo.presentation.feature.naver

import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultLeafMarkerUpdater
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.MarkerZIndex
import com.wheretogo.presentation.model.AppCluster
import com.wheretogo.presentation.model.AppLeaf
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.AppPath
import com.wheretogo.presentation.model.PathInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class NaverMapOverlayStore @Inject constructor() {
    private val markers = mutableMapOf<String, AppMarker>() // 컨텐츠 id
    private val clusterers = mutableMapOf<String, AppCluster>() // 그룹 id
    private val paths = mutableMapOf<String, AppPath>() // 컨텐츠 id

    fun updateMarkerVisible(contentId: String, isVisible: Boolean) {
        markers.get(contentId)?.let {
            val newMarker = it.replaceVisible(isVisible)
            markers.replace(contentId, newMarker)
        }
    }

    fun updateMarkerCaption(contentId: String, caption: String) {
        markers.get(contentId)?.let {
            val newMarker = it.replaceCation(caption)
            markers.replace(contentId, newMarker)
        }
    }

    fun updateMarkerPosition(contentId: String, latLng: LatLng) {
        markers.get(contentId)?.let {
            val newMarker = it.replacePosition(latLng)
            markers.replace(contentId, newMarker)
        }
    }

    fun updatePathVisible(contentId: String, isVisible: Boolean) {
        paths.get(contentId)?.let {
            val newPath = it.replaceVisible(isVisible)
            paths.replace(contentId, newPath)
        }
    }

    fun getMarker(contentId: String, initMarker: () -> AppMarker? = {null}): Result<AppMarker> {
        return runCatching {
            markers.getOrPut(contentId) {
                initMarker()?:return Result.failure(DomainError.InternalError())
            }
        }
    }

    fun getPath(contentId: String, initPath: () -> AppPath): Result<AppPath> {
        return runCatching {
            paths.getOrPut(contentId) {
                initPath()
            }
        }
    }

    fun getCluster(clusterId: String): Result<AppCluster>{
        return runCatching {
            clusterers[clusterId]?:throw DomainError.NotFound("$clusterId not found")
        }
    }

    fun addCluster(cluster: AppCluster){
        clusterers[cluster.id] = cluster
    }

    fun addLeaf(clusterId:String, leaf: LeafItem){
        getCluster(clusterId).onSuccess {
            it.cluster?.addAll(mapOf(leaf to null))
        }
    }

    fun removeCluster(clusterId:String){
        clusterers[clusterId]?.reflectClear()
        clusterers.remove(clusterId)
    }

    fun removeLeaf(clusterId:String, leafId:String){
        getCluster(clusterId).onSuccess {
            it.removeLeaf(leafId)
        }
    }

    fun updatePath(pathInfo: PathInfo): Result<AppPath> {
        return runCatching {
            if (pathInfo.points.size < 2)
                return Result.failure(DomainError.InternalError("points.size < 2"))

            val oldPath = paths.get(pathInfo.contentId)

            if (oldPath == null)
                return Result.failure(DomainError.NotFound("path empty"))
            oldPath.replacePoints(pathInfo.points)
        }
    }

    fun removeMarker(idGroup:List<String>){
        idGroup.forEach {
            markers[it]?.reflectClear()
            markers.remove(it)
        }
    }

    fun removePath(idGroup:List<String>){
        idGroup.forEach {
            paths[it]?.reflectClear()
            paths.remove(it)
        }
    }

    fun removeMarkerAndPath(id: String) {
        markers[id]?.reflectClear()
        markers.remove(id)
        paths[id]?.reflectClear()
        paths.remove(id)
    }

    fun size() = markers.size + paths.size

    fun clear() {
        markers.forEach {
            it.value.reflectClear()
        }
        paths.forEach {
            it.value.reflectClear()
        }
        clusterers.forEach {
            it.value.reflectClear()
        }
        markers.clear()
        paths.clear()
        clusterers.clear()
    }

    fun createCluster(
        clusterId: String,
        leafItemGroup: Map<LeafItem, Nothing?>,
        onLeafRendered: (Int) -> Unit = {},
    ): AppCluster {
        if (isClusterContains(clusterId))
            removeCluster(clusterId)
        val builder = Clusterer.ComplexBuilder<LeafItem>()
        var wrapperCluster = AppCluster(clusterId, null)
        var created = 0
        CoroutineScope(Dispatchers.IO).launch {
            repeat(10) {
                delay(10)
                if (created == leafItemGroup.size) {
                    launch(Dispatchers.Main) {
                        onLeafRendered(created)
                    }
                    return@launch
                }
            }
            launch(Dispatchers.Main) {
                onLeafRendered(created)
            }
        }
        builder.clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {
            override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
                super.updateClusterMarker(info, marker)
                marker.tag = "cluster"
                marker.zIndex = MarkerZIndex.CLUSTER.ordinal
                marker.icon = if (info.size < 5) {
                    MarkerIcons.CLUSTER_LOW_DENSITY
                } else {
                    MarkerIcons.CLUSTER_MEDIUM_DENSITY
                }
            }
        }).leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {
            override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                super.updateLeafMarker(info, marker)

                val key = info.key as LeafItem
                marker.apply {
                    marker.icon = key.overlayImage
                    marker.tag = key.leafId
                    // 캡션
                    marker.captionText = key.caption
                    captionOffset = 20
                    captionTextSize = 14f
                    isForceShowCaption = true

                    setCaptionAligns(Align.Bottom)

                    setOnClickListener {
                        key.onLeafClick(key.leafId)
                        true
                    }
                }
                wrapperCluster.addLeaf(AppLeaf(key, marker))

                created++
            }
        }).thresholdStrategy { zoom ->
            if (zoom <= 11) {
                0.0
            } else {
                20.0
            }
        }.markerManager(
            ClusterMarkerManager(
                onReleaseMarker = {
                    wrapperCluster.hideLeaf(it)
                }
            ))
        val cluster = builder.build()
        wrapperCluster = wrapperCluster.copy(cluster = cluster)
        cluster.addAll(leafItemGroup)
        addCluster(wrapperCluster)
        return wrapperCluster
    }

    fun isClusterContains(clusterId: String): Boolean {
        return clusterers.contains(clusterId)
    }
}