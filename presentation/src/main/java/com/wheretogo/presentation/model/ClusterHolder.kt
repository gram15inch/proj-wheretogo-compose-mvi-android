package com.wheretogo.presentation.model

import com.naver.maps.map.clustering.Clusterer
import com.wheretogo.presentation.feature.naver.LeafItem

class ClusterHolder() {
    var cluster: Clusterer<LeafItem>? = null
    private val _visibleLeafGroup = TraceList<AppLeaf> { it.leaf.leafId }
    val visibleLeafGroup: List<AppLeaf> get() = _visibleLeafGroup.toList()

    fun showLeaf(appLeaf: AppLeaf) {
        _visibleLeafGroup.addOrReplace(appLeaf)
    }

    fun hideLeaf(leafId: String) {
        _visibleLeafGroup.remove(leafId)
    }

    fun getLeaf(leafId: String): AppLeaf? {
        return _visibleLeafGroup.getOrNull(leafId)
    }

    fun addLeaf(leafItem: LeafItem) {
        cluster?.add(leafItem, null)
    }

    fun removeLeaf(leafId: String) {
        _visibleLeafGroup.getOrNull(leafId)?.let {
            it.reflectClear()
            _visibleLeafGroup.remove(leafId)
            cluster?.remove(it.leaf)
        }
    }

    fun updateLeafCaption(leafId: String, caption: String) {
        _visibleLeafGroup.getOrNull(leafId)?.let {
            _visibleLeafGroup.addOrReplace(it.replaceCation(caption))
        }
    }

    fun clear() {
        cluster?.clear()
        _visibleLeafGroup.clear()
        cluster?.map = null
    }

    fun getFilerPrint(): Int {
        return _visibleLeafGroup.getFingerPrint(false)
    }
}