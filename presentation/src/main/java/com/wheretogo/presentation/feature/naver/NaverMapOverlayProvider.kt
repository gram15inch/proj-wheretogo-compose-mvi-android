package com.wheretogo.presentation.feature.naver

import com.wheretogo.domain.DomainError
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.feature.model.StringKey
import com.wheretogo.presentation.model.AppCluster
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.AppPath
import com.wheretogo.presentation.model.ClusterInfo
import com.wheretogo.presentation.model.LeafInfo
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.model.TraceList
import com.wheretogo.presentation.toOverlayType
import javax.inject.Inject


class NaverMapOverlayProvider @Inject constructor(
    private val modifier: NaverMapOverlayModifier
) {
    private val _overlays = TraceList<MapOverlay> { it.key }
    val overlays: List<MapOverlay> get() = _overlays.toList()

    fun getFingerPrint(): Int {
        return _overlays.getFingerPrint(false)
    }

    fun scaleUp(key: StringKey, leafId: String, imgPath: String): Boolean {
        val cluster = _overlays.getOrNull(key.value)
        if (cluster == null || cluster !is AppCluster)
            return false

        modifier.scaleUp(imgPath).onSuccess {
            cluster.clusterHolder.getLeaf(leafId)?.replaceImage(imgPath, it)
        }
        return true
    }

    fun scaleDown(key: StringKey, leafId: String, imgPath: String): Boolean {
        val cluster = _overlays.getOrNull(key.value)
        if (cluster == null || cluster !is AppCluster)
            return false

        modifier.scaleDown(imgPath).onSuccess {
            cluster.clusterHolder.getLeaf(leafId)?.replaceImage(imgPath, it)
        }
        return true
    }


    //==================================

    fun updateVisible(key: StringKey, isVisible: Boolean): Boolean {
        val overlay = _overlays.getOrNull(key.value)
        return if (overlay != null) {
            overlay.replaceVisible(isVisible)
            true
        } else {
            false
        }
    }

    fun updateMarkerCaption(key: StringKey, caption: String): Boolean {
        val marker = _overlays.getOrNull(key.value)
        return if (marker != null && marker is AppMarker) {
            _overlays.addOrReplace(marker.replaceCation(caption))
            true
        } else {
            false
        }
    }

    fun updateMarkerPosition(key: StringKey, latLng: LatLng): Boolean {
        val marker = _overlays.getOrNull(key.value)
        return if (marker != null && marker is AppMarker) {
            _overlays.addOrReplace(marker.replacePosition(latLng))
            true
        } else {
            false
        }
    }

    fun getMarker(key: StringKey): Result<AppMarker> {
        return runCatching {
            val marker = _overlays.getOrNull(key.value)
            if (marker != null && marker is AppMarker)
                marker
            else
                return Result.failure(DomainError.InternalError())
        }
    }

    fun getPath(key: StringKey): Result<AppPath> {
        return runCatching {
            val path = _overlays.getOrNull(key.value)
            if (path != null && path is AppPath)
                path
            else
                return Result.failure(DomainError.InternalError())
        }
    }

    fun getCluster(key: StringKey): Result<AppCluster> {
        return runCatching {
            val cluster = _overlays.getOrNull(key.value)
            if (cluster != null && cluster is AppCluster)
                cluster
            else
                return Result.failure(DomainError.InternalError())
        }
    }

    fun addMarker(key: StringKey, info: MarkerInfo): Boolean {
        val marker = modifier.createMarker(info).getOrNull()
        val type = info.type.toOverlayType()
        val item = AppMarker(key.value, type, info, marker)
        return _overlays.addOrReplace(item)
    }

    fun addPath(key: StringKey, info: PathInfo): Boolean {
        val path = modifier.createPath(info).getOrNull()
        val type = info.type.toOverlayType()
        val item = AppPath(key.value, type, info, path)
        return _overlays.addOrReplace(item)
    }

    fun addCluster(
        key: StringKey,
        info: ClusterInfo,
        onLeafRendered: (Int) -> Unit,
        onLeafClick: (String) -> Unit,
    ): Boolean {
        val cluster = modifier.createCluster(info, onLeafRendered, onLeafClick)
        val type = OverlayType.CLUSTER
        val item = AppCluster(key.value, type, info, cluster)
        return _overlays.addOrReplace(item)
    }

    fun addLeaf(key: StringKey, info: LeafInfo, onLeafClick: (String) -> Unit) {
        getCluster(key).onSuccess {
            val item = modifier.createLeaf(info, onLeafClick)
            it.clusterHolder.addLeaf(item)
        }
    }

    fun removeCluster(key: StringKey) {
        _overlays.getOrNull(key.value)?.let {
            it.reflectClear()
            _overlays.remove(key.value)
        }
    }

    fun removeLeaf(key: StringKey, leafId: String) {
        getCluster(key).onSuccess {
            it.removeLeaf(leafId)
        }
    }

    fun updatePath(key: StringKey, pathInfo: PathInfo): Result<Unit> {
        return runCatching {
            if (pathInfo.points.size < 2)
                return Result.failure(DomainError.InternalError("points.size < 2"))
            val oldPath = getPath(key).getOrNull()

            if (oldPath == null)
                return Result.failure(DomainError.NotFound("path empty"))
            val new = oldPath.replacePoints(pathInfo.points)
                .replaceType(pathInfo.type)
            _overlays.addOrReplace(new)
        }
    }

    fun removeOverlay(keyGroup: List<StringKey>) {
        keyGroup.forEach { key ->
            _overlays.getOrNull(key.value)?.let {
                it.reflectClear()
                _overlays.remove(key.value)
            }
        }
    }

    fun clear() {
        _overlays.toList().forEach {
            it.reflectClear()
        }
        _overlays.clear()
    }
}