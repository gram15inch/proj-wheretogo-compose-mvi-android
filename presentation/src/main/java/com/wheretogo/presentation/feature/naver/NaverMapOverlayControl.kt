package com.wheretogo.presentation.feature.naver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.CHECKPOINT_TYPE
import com.wheretogo.domain.COURSE_TYPE
import com.wheretogo.domain.getCheckPointMarkerTag
import com.wheretogo.domain.getCourseMarkerTag
import com.wheretogo.domain.getCoursePathOverlayTag
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.toNaver


fun getMapOverlay(item: Course): MapOverlay {
    val naverPoints = item.route.toNaver()
    return MapOverlay(
        item.courseId,
        COURSE_TYPE,
        Marker().apply {
            this.captionText = item.courseName
            this.setCaptionAligns(Align.Top, Align.Right)
            this.captionOffset = 20
            this.captionTextSize = 16f
            naverPoints.getOrNull(0)
                ?.let { position = it }
            isHideCollidedMarkers = true
            tag = getCourseMarkerTag(code = item.courseId)
        },
        PathOverlay().apply {
            if (naverPoints.size >= 2) {
                coords = naverPoints
                tag = getCoursePathOverlayTag(code = item.courseId)
                width = 18
                outlineWidth = 3
            }
        }
    )
}

fun getMapOverlay(item: CheckPoint): MapOverlay {
    return MapOverlay(
        item.checkPointId,
        CHECKPOINT_TYPE,
        Marker().apply {
            this.captionText = "\uD83D\uDE03 주위가 조용해요. ${item.checkPointId}"
            this.setCaptionAligns(Align.Top, Align.Right)
            this.captionOffset = 20
            this.captionTextSize = 16f
            position = item.latLng.toNaver()
            val bitmap = BitmapFactory.decodeFile(item.imgUrl)
            isHideCollidedMarkers = true
            icon = OverlayImage.fromBitmap(
                getRoundedRectWithShadowBitmap(
                    bitmap,
                    30f,
                    8f,
                    Color.BLACK,
                    Color.WHITE
                )
            )
            tag = getCheckPointMarkerTag(item.checkPointId)
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

