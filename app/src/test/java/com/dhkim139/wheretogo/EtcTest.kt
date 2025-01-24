package com.dhkim139.wheretogo

import com.wheretogo.domain.OverlayType
import com.wheretogo.presentation.model.OverlayTag
import com.wheretogo.presentation.parse
import org.junit.Assert.assertEquals
import org.junit.Test


class EtcTest {

    @Test
    fun tagTest() {
        OverlayTag.parse("1/1/${OverlayType.NONE}")!!.let { tag ->
            assertEquals("1", tag.parentId)
            assertEquals("1", tag.overlayId)
            assertEquals(OverlayType.NONE, tag.type)
        }
    }

}