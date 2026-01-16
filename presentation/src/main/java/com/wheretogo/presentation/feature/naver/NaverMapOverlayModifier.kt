package com.wheretogo.presentation.feature.naver

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
import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultLeafMarkerUpdater
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.MarkerIcons
import com.wheretogo.domain.MarkerType
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.MarkerZIndex
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.minZoomLevel
import com.wheretogo.presentation.model.AppLeaf
import com.wheretogo.presentation.model.ClusterHolder
import com.wheretogo.presentation.model.ClusterInfo
import com.wheretogo.presentation.model.LeafInfo
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toNaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class NaverMapOverlayModifier @Inject constructor(
    private val context: Context,
    private val isGenerate: Boolean// 테스트용 오버레이 실제 생성 여부
) {
    private val normal: Pair<Float, Float> = 50f to 50f
    private val focus: Pair<Float, Float> = 110f to 110f

    fun createMarker(markerInfo: MarkerInfo): Result<Marker> {
        return runCatching {
            val marker = if (isGenerate) markerInfo.toNaverMarker(normal) else null
            marker!!
        }
    }

    fun createPath(pathInfo: PathInfo): Result<PathOverlay> {
        return runCatching {
            val path = (if (isGenerate) pathInfo.toNaverPath() else null)?.apply {
                coords = pathInfo.points.toNaver()
            }
            path!!
        }
    }

    fun createCluster(
        clusterInfo: ClusterInfo,
        onLeafRendered: (Int) -> Unit = {},
        onLeafClick: (String) -> Unit = {},
    ): ClusterHolder {
        val builder = Clusterer.ComplexBuilder<LeafItem>()
        val clusterHolder = ClusterHolder()
        var created = 0
        CoroutineScope(Dispatchers.IO).launch {
            repeat(10) {
                delay(10)
                if (created == clusterInfo.leafGroup.size) {
                    launch(Dispatchers.Main) {
                        onLeafRendered(created)
                    }
                    return@launch
                }
            }
            launch(Dispatchers.Main) {
                onLeafRendered(created)
            }
        }
        builder.clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {
            override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
                super.updateClusterMarker(info, marker)
                marker.tag = "cluster"
                marker.zIndex = MarkerZIndex.CLUSTER.ordinal
                marker.icon = if (info.size < 5) {
                    MarkerIcons.CLUSTER_LOW_DENSITY
                } else {
                    MarkerIcons.CLUSTER_MEDIUM_DENSITY
                }
            }
        }).leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {
            override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                super.updateLeafMarker(info, marker)
                val item = info.key as LeafItem
                marker.apply {
                    marker.icon = item.overlayImage
                    marker.tag = item.leafId
                    // 캡션
                    marker.captionText = item.caption
                    captionOffset = 20
                    captionTextSize = 14f
                    isForceShowCaption = true

                    setCaptionAligns(Align.Bottom)

                    setOnClickListener {
                        item.onLeafClick(item.leafId)
                        true
                    }
                }
                clusterHolder.showLeaf(AppLeaf(item, marker))

                created++
            }
        }).thresholdStrategy { zoom ->
            if (zoom <= 11) {
                0.0
            } else {
                20.0
            }
        }.markerManager(
            ClusterMarkerManager(
                onReleaseMarker = {
                    clusterHolder.hideLeaf(it)
                }
            ))
        val cluster = builder.build()
        val leafItemGroup = clusterInfo.leafGroup.associate {
            createLeaf(it, onLeafClick) to null
        }
        clusterHolder.cluster = cluster
        cluster.addAll(leafItemGroup)
        return clusterHolder
    }

    fun createLeaf(leafInfo: LeafInfo, onLeafClick: (String) -> Unit = {}): LeafItem {
        val overlayImage =
            createOverlayImage(leafInfo.thumbnail)
        return LeafItem(
            leafId = leafInfo.id,
            latLng = leafInfo.latLng.toNaver(),
            caption = leafInfo.caption,
            thumbnail = leafInfo.thumbnail,
            overlayImage = overlayImage,
            onLeafClick = onLeafClick
        )
    }

    fun scaleUp(imgPath: String): Result<OverlayImage> {
        return runCatching {
            createOverlayImage(imgPath, focus.first, focus.second)
        }
    }


    fun scaleDown(imgPath: String): Result<OverlayImage> {
        return runCatching {
            createOverlayImage(imgPath, normal.first, normal.second)
        }
    }

    private fun MarkerInfo.toNaverMarker(dpPair: Pair<Float, Float>): Marker {
        val markerInfo = this@toNaverMarker
        return Marker().apply {
            markerInfo.position?.let { position = it.toNaver() }
            markerInfo.caption?.let { captionText = it }
            tag = false // 스케일 확대 여부
            when {
                //마커가 사진
                markerInfo.iconPath != null -> {
                    val overlayImage =
                        createOverlayImage(markerInfo.iconPath, dpPair.first, dpPair.second)
                    icon = overlayImage

                    zIndex = MarkerZIndex.PHOTO.ordinal

                }
                //마커가 아이콘
                markerInfo.iconRes != null -> {
                    icon = OverlayImage.fromResource(markerInfo.iconRes)
                    zIndex = MarkerZIndex.ICON.ordinal
                }

                else -> {
                    val iconRes = when (contentId) {
                        CHECKPOINT_ADD_MARKER -> R.drawable.ic_mk_cm
                        else -> R.drawable.ic_mk_df
                    }
                    icon = OverlayImage.fromResource(iconRes)
                }
            }

            if (markerInfo.contentId == CHECKPOINT_ADD_MARKER) {
                zIndex = MarkerZIndex.ADD.ordinal
            }

            captionOffset = 20
            captionTextSize = 16f
            isHideCollidedMarkers = true
            setCaptionAligns(Align.Top, Align.Right)
            minZoom = when (markerInfo.type) {
                MarkerType.COURSE -> {
                    OverlayType.COURSE_MARKER.minZoomLevel()
                }

                MarkerType.CHECKPOINT -> {
                    OverlayType.CLUSTER.minZoomLevel()
                }

                MarkerType.DEFAULT -> {
                    OverlayType.ONE_TIME_MARKER.minZoomLevel()
                }
            }

        }
    }

    private fun PathInfo.toNaverPath(): PathOverlay {
        return PathOverlay().apply {
            tag = this@toNaverPath.contentId
            minZoom = OverlayType.FULL_PATH.minZoomLevel()
        }
    }

    private fun createOverlayImage(
        imgPath: String,
        widthDp: Float = normal.first,
        heightDp: Float = normal.second
    ): OverlayImage {
        val bitmap = BitmapFactory.decodeFile(imgPath)
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