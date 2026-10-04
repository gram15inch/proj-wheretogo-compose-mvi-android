package com.wheretogo.presentation.feature.naver

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dhkim139.core.ui.theme.Palette
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.wheretogo.domain.model.course.BoundingBox
import com.wheretogo.domain.model.course.CameraFocus
import com.wheretogo.domain.model.route.Direction
import com.wheretogo.presentation.toNaver


private const val FIT_BOUNDS_PADDING_DP = 78f
private const val FIT_BOUNDS_BASE_SIDE_DP = 360f
private const val FIT_BOUNDS_MIN_PADDING_DP = 32f
// 작은 화면: 한쪽 패딩이 짧은 변의 25%
private const val FIT_BOUNDS_SMALL_MAX_RATIO = 0.25f
// 큰 화면: 실제 컨텐츠 폭이 기준 폰의 1.5배
private const val FIT_BOUNDS_LARGE_CONTENT_RATIO = 1.5f
private const val FIT_BOUNDS_PHONE_CONTENT_DP =
    FIT_BOUNDS_BASE_SIDE_DP - FIT_BOUNDS_PADDING_DP * 2          // 204dp
private const val FIT_BOUNDS_MAX_CONTENT_DP =
    FIT_BOUNDS_PHONE_CONTENT_DP * FIT_BOUNDS_LARGE_CONTENT_RATIO // 306dp

fun NaverMap.applyFocus(focus: CameraFocus?, density: Density, mapSize: IntSize) {
    if (focus == null) return
    val bounds = focus.bounds
    val target = focus.target
    val zoom = focus.zoom
    val update = when {
        bounds != null -> CameraUpdate.fitBounds(
            bounds.toLatLngBounds(),
            fitBoundsPaddingPx(mapSize, focus.bottomPaddingPx, density),
        )
        target != null && zoom != null ->
            CameraUpdate.toCameraPosition(CameraPosition(target.toNaver(), zoom))

        target != null -> CameraUpdate.scrollTo(target.toNaver())
        else -> null
    } ?: return
    setContentPadding(0, 0, 0, focus.bottomPaddingPx)
    moveCamera(update.animate(CameraAnimation.Fly, 700))
}

private fun fitBoundsPaddingPx(mapSize: IntSize, bottomPaddingPx: Int, density: Density): Int =
    with(density) {
        val visibleHeightPx = (mapSize.height - bottomPaddingPx).coerceAtLeast(0)
        val shortSidePx = minOf(mapSize.width, visibleHeightPx)

        if (shortSidePx <= 0) return FIT_BOUNDS_PADDING_DP.dp.roundToPx()

        val shortSideDp = shortSidePx.toDp().value

        val paddingDp = if (shortSideDp < FIT_BOUNDS_BASE_SIDE_DP) {
            val scale = shortSideDp / FIT_BOUNDS_BASE_SIDE_DP
            (FIT_BOUNDS_PADDING_DP * scale * scale)
                .coerceAtLeast(FIT_BOUNDS_MIN_PADDING_DP)
                .coerceAtMost(shortSideDp * FIT_BOUNDS_SMALL_MAX_RATIO)
        } else {
            maxOf(
                FIT_BOUNDS_PADDING_DP,
                (shortSideDp - FIT_BOUNDS_MAX_CONTENT_DP) / 2f,
            )
        }

        paddingDp.dp.roundToPx()
    }

private fun BoundingBox.toLatLngBounds() =
    LatLngBounds(southWest.toNaver(), northEast.toNaver())

val Direction.accent: Color
    get() = if (this == Direction.FORWARD) Palette.ForwardBlue else Palette.BackwardOrange




