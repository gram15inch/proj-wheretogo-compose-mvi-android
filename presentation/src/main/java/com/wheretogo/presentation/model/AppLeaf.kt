package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.Marker
import com.wheretogo.presentation.feature.naver.LeafItem

data class AppLeaf(
    val leaf: LeafItem,
    val coreMarker: Marker? = null
){

    fun replaceCation(caption: String): AppLeaf {
        return copy(
            coreMarker = coreMarker?.apply {
                captionText = caption
            },
            leaf = leaf.copy(
                caption = caption
            )
        )

    }

    fun reflectClear() {
        coreMarker?.apply {
            map = null
        }
    }
}