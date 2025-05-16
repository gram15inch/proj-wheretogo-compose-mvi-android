package com.wheretogo.presentation.feature.naver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.Log
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toNaver
import javax.inject.Inject
import androidx.core.graphics.createBitmap


class NaverMapOverlayStore @Inject constructor() {
    private val markers = mutableMapOf<String, Marker>()
    private val paths = mutableMapOf<String, PathOverlay>()

    fun getOrCreateMarker(markerInfo: MarkerInfo): Marker {
        return markers.getOrPut(markerInfo.contentId) {
            markerInfo.toNaverMarker()
        }
    }

    fun getOrCreatePath(pathInfo: PathInfo): PathOverlay? {
        return runCatching {
            if (pathInfo.points.size < 2)
                return null

            paths.getOrPut(pathInfo.contentId) {
                pathInfo.toNaverPath()
            }.apply { coords = pathInfo.points.toNaver() }
        }.onFailure {
            Log.d("tst_", "fail ${pathInfo.contentId}-${it.message}")
        }.getOrNull()
    }

    fun remove(id: String) {
        markers[id]?.map = null
        markers.remove(id)
        paths[id]?.map = null
        paths.remove(id)
    }

    fun size() = markers.size + paths.size

    private fun MarkerInfo.toNaverMarker():Marker{
        val markerInfo = this@toNaverMarker
        return Marker().apply {
            markerInfo.position?.let { position = it.toNaver() }
            markerInfo.caption?.let { captionText= it}
            //마커가 아이콘
            markerInfo.iconRes?.let { res->
                icon = OverlayImage.fromResource(res)
            }

            //마커가 사진
            markerInfo.iconPath?.let { path ->
                val bitmap = BitmapFactory.decodeFile(path)
                val overlayImage = OverlayImage.fromBitmap(
                    getRoundedRectWithShadowBitmap(
                        bitmap,
                        30f,
                        8f,
                        Color.BLACK,
                        Color.WHITE
                    )
                )
                icon = overlayImage
            }
            if(markerInfo.contentId== CHECKPOINT_ADD_MARKER) {
                zIndex = 999
            }

            captionOffset = 20
            captionTextSize = 16f
            isHideCollidedMarkers = true
            setCaptionAligns(Align.Top, Align.Right)
            when(markerInfo.type){
                MarkerType.SPOT->{
                    minZoom= OverlayType.SPOT.minZoomLevel()
                }
                MarkerType.CHECKPOINT->{
                    minZoom = OverlayType.CHECKPOINT.minZoomLevel()
                }

            }

        }
    }

    private fun PathInfo.toNaverPath():PathOverlay{
        return PathOverlay().apply {
            tag = this@toNaverPath.contentId
            minZoom = minZoomLevel
        }
    }

    @Suppress("SameParameterValue")
    private fun getRoundedRectWithShadowBitmap(
        bitmap: Bitmap,
        cornerRadius: Float,
        shadowRadius: Float,
        shadowColor: Int,
        backgroundColor: Int
    ): Bitmap {
        val shadowOffset = shadowRadius * 2
        val width = bitmap.width + shadowOffset.toInt()
        val height = bitmap.height + shadowOffset.toInt()

        val output = createBitmap(width, height)
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
}