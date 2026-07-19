package com.wheretogo.presentation.composable.photoviewer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.naver.maps.map.MapView
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.model.gallery.PhotoExif
import com.wheretogo.presentation.composable.content.PageIndicator
import com.wheretogo.presentation.composable.effect.LifecycleDisposer
import com.wheretogo.presentation.intent.PhotoViewerIntent
import com.wheretogo.presentation.viewmodel.PhotoViewerViewModel

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    photos: List<GalleryPhoto>,
    initialIndex: Int,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    mapView: MapView?,
    viewModel: PhotoViewerViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val density = LocalDensity.current
    val window = LocalWindowInfo.current

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size },
    )
    val currentPhoto = photos[pagerState.currentPage]

    val photoHeightIn = calculateHeightIn(window, currentPhoto.exif)
    val heightInScrollState = remember(photoHeightIn.max) {
        HeightInScrollState(photoHeightIn)
    }
    val photoHeight by animateFloatAsState(
        targetValue = heightInScrollState.targetHeight,
        animationSpec =
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "photoHeight",
    )
    val fingerPrint by viewModel.fingerPrint.collectAsState()

    BackHandler { onClose() }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.handleIntent(PhotoViewerIntent.Refresh(currentPhoto))
    }

    LifecycleDisposer {
        viewModel.handleIntent(PhotoViewerIntent.LifecycleChange(it))
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .nestedScroll(heightInScrollState.connection),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(with(density) { photoHeight.toDp() })
                    .clipToBounds(),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val pagePhoto = photos[page]
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageModifier = if (page == initialIndex) {
                            with(sharedScope) {
                                Modifier
                                    .fillMaxSize()
                                    .sharedElement(
                                        sharedContentState =
                                            rememberSharedContentState(key = pagePhoto.id),
                                        animatedVisibilityScope = animatedScope,
                                    )
                            }
                        } else {
                            Modifier.fillMaxSize()
                        }
                        if(pagePhoto.imageSource==null)
                            AsyncImage(
                                model = pagePhoto.thumbnail,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = imageModifier,
                            )
                        AsyncImage(
                            model = pagePhoto.imageSource,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = imageModifier,
                        )
                    }
                }

                if (photos.size > 1) {
                    PageIndicator(
                        pageCount = photos.size,
                        currentPage = pagerState.currentPage,
                        maxDotCount = 11,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                    )
                }
            }


            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(modifier = Modifier.boundedHeight()) {
                    PhotoViewerContent(
                        mapView = mapView,
                        photo = currentPhoto,
                        overlay = viewModel.overlay,
                        fingerPrint = fingerPrint,
                        event = viewModel.event,
                        stampState = state.stampState,
                        stampProgress = state.stampProgress,
                        actions = PhotoViewerActions(
                            onStamp = {
                                viewModel.handleIntent(PhotoViewerIntent.Stamp(currentPhoto,it))
                            },
                            onRemoveStamp = {
                                viewModel.handleIntent(PhotoViewerIntent.RemoveStamp(currentPhoto))
                            },
                            onCameraUpdate = {
                                viewModel.handleIntent(PhotoViewerIntent.CameraUpdate(it))
                            }
                        ),
                    )
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .align(Alignment.TopStart),
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

private fun Modifier.boundedHeight(): Modifier = layout { measurable, constraints ->
    val safe = if (constraints.maxHeight == Constraints.Infinity) {
        val maxH = Constraints.fitPrioritizingWidth(
            minWidth = constraints.minWidth,
            maxWidth = constraints.maxWidth,
            minHeight = 0,
            maxHeight = 100_000,
        ).maxHeight
        constraints.copy(maxHeight = maxH)
    } else constraints
    val placeable = measurable.measure(safe)
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

@Stable
private class HeightInScrollState(
    val heightIn: HeightIn,
    private val followRatio: Float = 0.75f, // 스크롤 양 조절
) {
    var targetHeight by mutableFloatStateOf(heightIn.base)
        private set

    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // 위로 스크롤시에만
            if (available.y >= 0f) return Offset.Zero
            // 타겟 확장
            return Offset(0f, consumeAndResize(available.y))
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            // 아래로 스크롤시에만
            if (available.y <= 0f) return Offset.Zero
            // 타겟 축소
            return Offset(0f, consumeAndResize(available.y))
        }
    }

    private fun consumeAndResize(delta: Float): Float {
        val resizeHeight = (targetHeight + delta * followRatio) // 보정된 스크롤
            .coerceIn(heightIn.min, heightIn.max) // 타겟의 최소, 최댓값 지정
        val consumedHeight = resizeHeight - targetHeight
        targetHeight = resizeHeight
        return consumedHeight / followRatio  // 원본 단위로 환산
    }
}

private data class HeightIn(val base: Float, val min: Float, val max: Float)

@Composable
private fun calculateHeightIn(window: WindowInfo ,exif: PhotoExif): HeightIn {
    val containerWidth = window.containerSize.width.toFloat()
    val containerHeight = window.containerSize.height.toFloat()
    val base = minOf(containerWidth, containerHeight * 0.7f) // 진입시 크기
    val min = base * 0.5f // 최소 높이가 진입시 크기의 50%
    // 최대 높이가 사진의 비율과 컨테이너 크기의 82% 중 작은값
    val max = remember(exif, containerWidth, containerHeight, base) {
        val exifWidth = exif.width ?: 0
        val exifHeight = exif.height ?: 0
        val photoRatio = if (exifWidth > 0 && exifHeight > 0) {
             exifHeight.toFloat() / exifWidth
        } else {
            1.3f
        }
        val heightByContainer = (containerHeight * 0.82f)
        val heightByPhoto = containerWidth * photoRatio
        minOf(heightByContainer,heightByPhoto).coerceAtLeast(base)
    }
    return HeightIn(base, min, max)
}