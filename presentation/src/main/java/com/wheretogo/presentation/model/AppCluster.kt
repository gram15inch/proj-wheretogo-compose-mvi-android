package com.wheretogo.presentation.model

import com.wheretogo.presentation.OverlayType

data class AppCluster(
    override val key: String,
    override val type: OverlayType,
    val clusterInfo: ClusterInfo,
    val clusterHolder: ClusterHolder,
) : MapOverlay {

    override fun getFingerPrint(): Int {
        var h = key.hashCode()
        h = 31 * h + type.hashCode()
        h = 31 * h + clusterInfo.contentId.hashCode()
        h = 31 * h + clusterInfo.leafGroup.size.hashCode()
        h = 31 * h + clusterHolder.getFilerPrint()
        return h
    }

    override fun replaceVisible(isVisible: Boolean): MapOverlay {
        return this
    }

    override fun reflectClear() {
        clusterHolder.clear()
    }

    fun removeLeaf(leafId: String) {
        clusterHolder.removeLeaf(leafId)
    }

    fun updateLeafCaption(leafId: String, caption: String) {
        clusterHolder.getLeaf(leafId)?.let {
            clusterHolder.updateLeafCaption(leafId, caption)
        }
    }

    fun getAppLeafGroup() = clusterHolder.visibleLeafGroup.toList()
}
