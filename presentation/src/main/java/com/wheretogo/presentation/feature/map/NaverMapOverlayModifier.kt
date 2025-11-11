package com.wheretogo.presentation.feature.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.MarkerZIndex
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.AppPath
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toNaver
import javax.inject.Inject

class NaverMapOverlayModifier @Inject constructor(
    private val context: Context,
    private val isGenerate: Boolean// 테스트용 오버레이 실제 생성 여부
) {
    private val normal: Pair<Float, Float> = 50f to 50f
    private val focus: Pair<Float, Float> = 110f to 110f

    fun createMarker(markerInfo: MarkerInfo): AppMarker {
        val marker = if (isGenerate) markerInfo.toNaverMarker(normal) else null
        return AppMarker(markerInfo, marker)
    }

    fun createPath(pathInfo: PathInfo): AppPath {
        val path = (if (isGenerate) pathInfo.toNaverPath() else null)?.apply {
            coords = pathInfo.points.toNaver()
        }
        val appPath = AppPath(pathInfo, path)
        return  appPath

    }

    private fun MarkerInfo.toNaverMarker(dpPair:Pair<Float, Float>): Marker {
        val markerInfo = this@toNaverMarker
        return Marker().apply {
            markerInfo.position?.let { position = it.toNaver() }
            markerInfo.caption?.let { captionText = it }
            tag = false // 스케일 확대 여부
            when {
                //마커가 사진
                markerInfo.iconPath != null -> {
                    val overlayImage =
                        createOverlayImage(markerInfo, dpPair.first, dpPair.second)
                    icon = overlayImage

                    zIndex = MarkerZIndex.PHOTO.ordinal

                }
                //마커가 아이콘
                markerInfo.iconRes != null -> {
                    icon = OverlayImage.fromResource(markerInfo.iconRes)
                    zIndex = MarkerZIndex.ICON.ordinal
                }
            }

            if (markerInfo.contentId == CHECKPOINT_ADD_MARKER) {
                zIndex = MarkerZIndex.ADD.ordinal
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

    fun scaleUp(info: MarkerInfo): OverlayImage? {
        return createOverlayImage(info, focus.first, focus.second)
    }

    private fun createOverlayImage(info: MarkerInfo, widthDp: Float, heightDp: Float): OverlayImage {
        val bitmap = BitmapFactory.decodeFile(info.iconPath)
        val overlayImage = OverlayImage.fromBitmap(
            bitmap
                .resizeToDp(context, widthDp, heightDp)
                .roundedRectWithShadow(
                    30f,
                    8f,
                    Color.BLACK,
                    Color.WHITE
                )
        )
        return overlayImage
    }

    fun scaleDown(info:MarkerInfo): OverlayImage {
        return createOverlayImage(info, normal.first,normal.second)
    }

    private fun PathInfo.toNaverPath(): PathOverlay {
        return PathOverlay().apply {
            tag = this@toNaverPath.contentId
            minZoom = minZoomLevel
        }
    }

    @Suppress("SameParameterValue")
    private fun Bitmap.roundedRectWithShadow(
        cornerRadius: Float,
        shadowRadius: Float,
        shadowColor: Int,
        backgroundColor: Int
    ): Bitmap {
        val bitmap = this
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

    private fun Bitmap.resizeToDp(
        context: Context,
        newWidthDp: Float,
        newHeightDp: Float
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val newWidthPx = (newWidthDp * density).toInt()
        val newHeightPx = (newHeightDp * density).toInt()

        return scale(newWidthPx, newHeightPx)
    }
}

