package com.wheretogo.presentation.model

import com.naver.maps.map.clustering.Clusterer
import com.wheretogo.presentation.feature.naver.LeafItem

data class AppCluster(
    val id: String,
    val cluster: Clusterer<LeafItem>? = null,
) {
    private val leafMarkerGroup : MutableMap<String, AppLeaf> = mutableMapOf()
    fun reflectClear() {
        cluster?.apply {
            clear()
            leafMarkerGroup.clear()
            map = null
        }
    }

    fun removeLeaf(leafId:String){
        leafMarkerGroup[leafId]?.let {
            it.reflectClear()
            leafMarkerGroup.remove(leafId)
            cluster?.remove(it.leaf)
        }
    }

    fun hideLeaf(leafId:String){
        leafMarkerGroup.remove(leafId)
    }

    fun addLeaf(appLeaf: AppLeaf){
        leafMarkerGroup.put(appLeaf.leaf.leafId, appLeaf)
    }

    fun updateLeafCaption(leafId:String, caption:String){
        leafMarkerGroup[leafId]?.replaceCation(caption)
    }

    fun getAppLeafGroup() = leafMarkerGroup.values
}