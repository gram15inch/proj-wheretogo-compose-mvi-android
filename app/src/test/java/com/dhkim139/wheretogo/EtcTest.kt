package com.dhkim139.wheretogo

import com.wheretogo.domain.OverlayType
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.MarkerIconType
import com.wheretogo.presentation.model.OverlayTag
import com.wheretogo.presentation.parse
import com.wheretogo.presentation.toStringTag
import org.junit.Assert.assertEquals
import org.junit.Test


class EtcTest {

    @Test
    fun tagTest() {
        val stringTag = OverlayTag("1","2",OverlayType.COURSE,MarkerIconType.PHOTO, LatLng(1.0,2.0)).toStringTag()
        OverlayTag.parse(stringTag)!!.let { tag ->
            assertEquals("1", tag.overlayId)
            assertEquals("2", tag.parentId)
            assertEquals(OverlayType.COURSE, tag.overlayType)
            assertEquals(MarkerIconType.PHOTO, tag.iconType)
            assertEquals(LatLng(1.0,2.0) ,tag.latlng)
        }
    }

}