package com.wheretogo.presentation.feature.naver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.AppPath
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toNaver
import javax.inject.Inject


class NaverMapOverlayStore @Inject constructor(private val isGenerate: Boolean) { // 테스트용 오버레이 실제 생성 여부
    private val markers = mutableMapOf<String, AppMarker>() // 컨텐츠 id
    private val paths = mutableMapOf<String, AppPath>() // 컨텐츠 id

    fun updateMarkerVisible(contentId: String, isVisible: Boolean) {
        markers.get(contentId)?.let {
            val newMarker = it.replaceVisible(isVisible)
            markers.replace(contentId, newMarker)
        }
    }

    fun updateMarkerCaption(contentId: String, caption: String) {
        markers.get(contentId)?.let {
            val newMarker = it.replaceCation(caption)
            markers.replace(contentId, newMarker)
        }
    }

    fun updateMarkerPosition(contentId: String, latLng: LatLng) {
        markers.get(contentId)?.let {
            val newMarker = it.replacePosition(latLng)
            markers.replace(contentId, newMarker)
        }
    }

    fun updatePathVisible(contentId: String, isVisible: Boolean) {
        paths.get(contentId)?.let {
            val newPath = it.replaceVisible(isVisible)
            paths.replace(contentId, newPath)
        }
    }

    fun getOrCreateMarker(markerInfo: MarkerInfo): AppMarker {
        return markers.getOrPut(markerInfo.contentId) {
            createMarker(markerInfo)
        }
    }

    fun createAppPath(pathInfo: PathInfo): AppPath? {
        return runCatching {
            if (pathInfo.points.size < 2)
                return null

            val oldPath = paths.get(pathInfo.contentId)
            return if (oldPath != null) {
                oldPath.replacePoints(pathInfo.points)
            } else {
                val newPath = createPath(pathInfo)
                paths.put(pathInfo.contentId, newPath)
                newPath
            }
        }.getOrNull()
    }

    private fun createMarker(markerInfo: MarkerInfo): AppMarker {
        val marker = if (isGenerate) markerInfo.toNaverMarker() else null
        return AppMarker(markerInfo, marker)
    }

    private fun createPath(pathInfo: PathInfo): AppPath {
        val path = (if (isGenerate) pathInfo.toNaverPath() else null)?.apply {
            coords = pathInfo.points.toNaver()
        }
        val appPath = AppPath(pathInfo, path)
        return appPath
    }

    fun remove(id: String) {
        markers[id]?.reflectClear()
        markers.remove(id)
        paths[id]?.reflectClear()
        paths.remove(id)
    }

    fun size() = markers.size + paths.size

    fun clear(){
        markers.forEach {
            it.value.coreMarker?.apply { map=null }
        }
        markers.clear()
        paths.forEach {
            it.value.corePathOverlay?.apply { map=null }
        }
        paths.clear()
    }

    // 유틸

    private fun MarkerInfo.toNaverMarker(): Marker {
        val markerInfo = this@toNaverMarker
        return Marker().apply {
            markerInfo.position?.let { position = it.toNaver() }
            markerInfo.caption?.let { captionText = it }


            when{
                //마커가 사진
                markerInfo.iconPath != null ->{
                    val bitmap = BitmapFactory.decodeFile(markerInfo.iconPath)
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
                    zIndex = 10

                }
                //마커가 아이콘
                markerInfo.iconRes != null ->{
                    icon = OverlayImage.fromResource(markerInfo.iconRes)
                    zIndex = 9
                }
            }

            if (markerInfo.contentId == CHECKPOINT_ADD_MARKER) {
                zIndex = 999
            }

            captionOffset = 20
            captionTextSize = 16f
            isHideCollidedMarkers = true
            setCaptionAligns(Align.Top, Align.Right)
            when (markerInfo.type) {
                MarkerType.SPOT -> {
                    minZoom = OverlayType.SPOT.minZoomLevel()
                }
                MarkerType.CHECKPOINT -> {
                    minZoom = OverlayType.CHECKPOINT.minZoomLevel()
                }

            }

        }
    }

    private fun PathInfo.toNaverPath(): PathOverlay {
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