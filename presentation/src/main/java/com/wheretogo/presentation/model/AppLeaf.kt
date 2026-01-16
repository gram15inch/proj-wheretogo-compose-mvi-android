package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.wheretogo.presentation.MarkerZIndex
import com.wheretogo.presentation.feature.naver.LeafItem

data class AppLeaf(
    val leaf: LeafItem,
    val coreMarker: Marker? = null
) : Traceable {
    override fun getFingerPrint(): Int {
        var h = leaf.leafId.hashCode()
        h = 31 * h + leaf.latLng.hashCode()
        h = 31 * h + leaf.caption.hashCode()
        h = 31 * h + leaf.thumbnail.hashCode()
        return h
    }

    fun replaceImage(imgPath: String, oi: OverlayImage): AppLeaf {
        return copy(
            coreMarker = coreMarker?.apply {
                icon = oi
                zIndex = MarkerZIndex.PHOTO_ZOOM.ordinal
                captionTextSize = 16f
                setCaptionAligns(Align.Top)
            },
            leaf = leaf.copy(
                thumbnail = imgPath
            )
        )
    }

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

    override fun equals(other: Any?): Boolean {
        return if (other is AppLeaf)
            leaf.leafId == other.leaf.leafId &&
                    leaf.latLng == other.leaf.latLng &&
                    leaf.caption == other.leaf.caption &&
                    leaf.thumbnail == other.leaf.thumbnail
        else false
    }
}