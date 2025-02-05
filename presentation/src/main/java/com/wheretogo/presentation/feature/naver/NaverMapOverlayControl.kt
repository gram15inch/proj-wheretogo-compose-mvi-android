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
import com.wheretogo.domain.OverlayType
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.MarkerIconType
import com.wheretogo.presentation.getCourseIconType
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.OverlayTag
import com.wheretogo.presentation.toNaver
import com.wheretogo.presentation.toStringTag


fun getMapOverlay(item: Course): MapOverlay {
    val naverPoints = item.points.toNaver()
    val overlayTag = OverlayTag(item.courseId, item.courseId, OverlayType.COURSE)
    return MapOverlay(
        overlayTag.overlayId,
        overlayTag.type,
        getCourseIconType(item.type),
        listOf(Marker().apply {
            this.captionText = item.courseName
            this.setCaptionAligns(Align.Top, Align.Right)
            this.captionOffset = 20
            this.captionTextSize = 16f
            naverPoints.getOrNull(0)
                ?.let { position = it }
            isHideCollidedMarkers = true
            tag = overlayTag.toStringTag()
        }),
        PathOverlay().apply {
            if (naverPoints.size >= 2) {
                coords = naverPoints
                tag = overlayTag.copy(type = OverlayType.PATH).toStringTag()
                width = 18
                outlineWidth = 3
            }
        }
    )
}

fun getMapOverlay(courseId: String, item: CheckPoint): MapOverlay {
    val overlayTag = OverlayTag(item.checkPointId, courseId, OverlayType.CHECKPOINT)
    return MapOverlay(
        overlayTag.overlayId,
        overlayTag.type,
        MarkerIconType.DEFAULT,
        listOf(Marker().apply {
            this.captionText = item.titleComment
            this.setCaptionAligns(Align.Top, Align.Right)
            this.captionOffset = 20
            this.captionTextSize = 16f
            position = item.latLng.toNaver()
            isHideCollidedMarkers = true
            if (item.imageLocalPath.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeFile(item.imageLocalPath)
                icon = OverlayImage.fromBitmap(
                    getRoundedRectWithShadowBitmap(
                        bitmap,
                        30f,
                        8f,
                        Color.BLACK,
                        Color.WHITE
                    )
                )
            }
            tag = overlayTag.toStringTag()
        }),
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

