package com.dhkim139.feature.navigationoption.ui

import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.data.ParkingTag

internal object UiRules {

    const val MAX_LINK_SEC = 5 * 60
    const val MAX_VISIBLE_CANDIDATES = 5
    const val MAX_TAGS = 3
    const val DELIVERABLE_WITHOUT_WAYPOINTS = 2

    val DISPLAY_ORDER: List<MapApp> = listOf(MapApp.NAVER_MAP, MapApp.TMAP, MapApp.KAKAO_MAP)
    val INVISIBLE_TAGS = listOf(ParkingTag.UNKNOWN, ParkingTag.ACCESSIBLE)

    fun visibleTag(
        tags: List<ParkingTag>
    ): List<ParkingTag> {
        return tags
            .filter { !INVISIBLE_TAGS.contains(it) }
            .take(MAX_TAGS)
    }

    fun visibleParking(
        candidates: List<ParkingCandidate>,
    ): List<ParkingCandidate> = candidates
        .filter { it.linkSec <= MAX_LINK_SEC }
        .sortedBy { it.linkSec }
        .take(MAX_VISIBLE_CANDIDATES)
}