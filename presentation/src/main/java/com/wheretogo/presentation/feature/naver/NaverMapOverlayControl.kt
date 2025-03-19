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
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.getCourseIconType
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.OverlayTag
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toNaver
import com.wheretogo.presentation.toStringTag


fun getMapOverlay(item: Course): MapOverlay {
    val overlayTag = OverlayTag(item.courseId, item.courseId, OverlayType.COURSE, latlng = item.cameraLatLng)
    check(item.waypoints.isNotEmpty())
    return MapOverlay(
        overlayId = overlayTag.overlayId,
        overlayType = overlayTag.overlayType,
        markerGroup = listOf(getMarkerOverlay(item)),
        path = getPathOverlay(item.courseId, item.points)
    )
}

fun getMarkerOverlay(course:Course):Marker{
    val markerPosition = course.waypoints.firstOrNull()!!.toNaver()
    val overlayTag = OverlayTag(course.courseId, course.courseId, OverlayType.COURSE, getCourseIconType(course.type), markerPosition.toDomainLatLng())
    return Marker().apply {
        tag = overlayTag.toStringTag()
        position = markerPosition
        captionText = course.courseName
        captionOffset = 20
        captionTextSize = 16f
        isHideCollidedMarkers = true
        minZoom= OverlayType.COURSE.minZoomLevel()
        setCaptionAligns(Align.Top, Align.Right)
    }
}

fun getPathOverlay(courseId: String, point:List<LatLng>):PathOverlay?{
    val overlayTag = OverlayTag(courseId, courseId, OverlayType.PATH)
    val naverPoints = point.toNaver()
    return  if(naverPoints.size >= 2){
        PathOverlay().apply {
            tag = overlayTag.toStringTag()
            coords = naverPoints
            width = 18
            outlineWidth = 3
            minZoom= OverlayType.PATH.minZoomLevel()
        }
    }else null
}

fun getMapOverlay(courseId: String, item: CheckPoint): MapOverlay {
    val overlayTag = OverlayTag(item.checkPointId, courseId, OverlayType.CHECKPOINT)
    return MapOverlay(
        overlayTag.overlayId,
        overlayTag.overlayType,
        listOf(Marker().apply {
            tag = overlayTag.toStringTag()
            captionText = item.titleComment
            captionOffset = 20
            captionTextSize = 16f
            position = item.latLng.toNaver()
            isHideCollidedMarkers = true
            minZoom = OverlayType.CHECKPOINT.minZoomLevel()
            setCaptionAligns(Align.Top, Align.Right)
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
        }),
        null
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

