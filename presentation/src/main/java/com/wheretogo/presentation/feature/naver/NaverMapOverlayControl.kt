package com.wheretogo.presentation.feature.naver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.getCheckPointMarkerTag
import com.wheretogo.domain.model.getCourseMarkerTag
import com.wheretogo.domain.model.getCoursePathOverlayTag
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.toNaver

fun NaverMap.addJourneyOverlay(item: Journey, onMarkerClick: (Overlay) -> Unit): MapOverlay {
    val naverPoints = item.points.toNaver()
    return MapOverlay(
        item.code,
        Marker().apply {
            position = naverPoints[0] // todo 인덱스 에러 처리
            map = this@addJourneyOverlay
            tag = getCourseMarkerTag(code = item.code)
            this.setOnClickListener { overlay ->
                onMarkerClick(overlay)
                true
            }
        },
        PathOverlay().apply {
            coords = naverPoints
            map = this@addJourneyOverlay
            tag = getCoursePathOverlayTag(code = item.code)
        }
    )
}


fun getMapOverlay(item: Journey): MapOverlay {
    val naverPoints = item.points.toNaver()
    return MapOverlay(
        item.code,
        Marker().apply {
            this.captionText = "운전연수 코스 ${item.code}"
            this.setCaptionAligns(Align.Top, Align.Right)
            this.captionOffset = 20
            this.captionTextSize = 16f
            position = naverPoints[0]
            isHideCollidedMarkers = true
            tag = getCourseMarkerTag(code = item.code)
        },
        PathOverlay().apply {
            coords = naverPoints
            tag = getCoursePathOverlayTag(code = item.code)
            width = 18
            outlineWidth = 3
        }
    )
}

fun getMapOverlay(code: Int, item: CheckPoint): MapOverlay {
    return MapOverlay(
        item.id,
        Marker().apply {
            this.captionText = "\uD83D\uDE03 주위가 조용해요. ${item.id}"
            this.setCaptionAligns(Align.Top, Align.Right)
            this.captionOffset = 20
            this.captionTextSize = 16f
            position = item.latLng.toNaver()
            val bitmap = BitmapFactory.decodeFile(item.url)
            isHideCollidedMarkers = true
            icon = OverlayImage.fromBitmap(getRoundedRectWithShadowBitmap(bitmap, 30f, 8f, Color.BLACK, Color.WHITE))
            tag = getCheckPointMarkerTag(code, checkPoint = item)
        },
        PathOverlay()
    )
}


fun getRoundedRectWithShadowBitmap(
    bitmap: Bitmap,
    cornerRadius: Float,
    shadowRadius: Float,
    shadowColor: Int,
    backgroundColor: Int
): Bitmap {
    val shadowOffset = shadowRadius * 2
    val width = bitmap.width + shadowOffset.toInt()
    val height = bitmap.height + shadowOffset.toInt()

    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val shadowPaint = Paint().apply {
        color = backgroundColor
        setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        isAntiAlias = true
    }

    val rect = RectF(
        shadowRadius,
        shadowRadius,
        bitmap.width.toFloat() + shadowRadius,
        bitmap.height.toFloat() + shadowRadius
    )

    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, shadowPaint)

    val bitmapPaint = Paint().apply {
        isAntiAlias = true
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bitmapPaint)

    return output
}

fun HideOverlayMap.hideOverlayWithoutItem(itemCode: Int, allItems: MutableMap<Int, MapOverlay>) {
    if (this.isEmpty()) {
        for (itemInAll in allItems) {
            if (itemInAll.value.code != itemCode) {
                allItems[itemInAll.value.code]?.let { hiddenItem ->
                    this[hiddenItem.code] = hiddenItem
                }
            }
        }
    } else {
        this.clear()
    }
}


class HideOverlayMap(
    private val innerMap: MutableMap<Int, MapOverlay> = mutableMapOf()
) : MutableMap<Int, MapOverlay> by innerMap {

    override fun put(key: Int, value: MapOverlay): MapOverlay? {
        hide(value)
        return innerMap.put(key, value)
    }

    override fun remove(key: Int): MapOverlay? {
        innerMap[key]?.let {
            show(it)
        }
        return innerMap.remove(key)
    }

    override fun clear() {
        innerMap.forEach {
            show(it.value)
        }
        innerMap.clear()
    }

    private fun show(element: MapOverlay) {
        element.marker.isVisible = true
        element.pathOverlay.isVisible = true
    }

    private fun hide(element: MapOverlay) {
        element.marker.isVisible = false
        element.pathOverlay.isVisible = false
    }
}

