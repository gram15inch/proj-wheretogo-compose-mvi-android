package com.dhkim139.feature.camerapicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dhkim139.feature.camerapicker.model.VerifiedImage


@Composable
internal fun CameraPickerContent(
    state: CameraPickerState,
    locationLoading: Boolean,
    onIntent: (CameraPickerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var prevCount by remember { mutableIntStateOf(state.images.size) }

    fun onCaptureRequested() = onIntent(CameraPickerIntent.CaptureClicked)

    LaunchedEffect(state.images.size) {
        if (state.images.size > prevCount) {
            withFrameNanos { }
            scrollState.scrollTo(scrollState.maxValue)
        }
        prevCount = state.images.size
    }

    Column(modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(22.dp))
            PickerHeader()
            Spacer(Modifier.height(20.dp))

            CaptureArea(
                selectedImage = state.selectedImage,
                locationLoading = locationLoading,
                onCaptureRequested = ::onCaptureRequested,
            )
            Spacer(Modifier.height(14.dp))

            if (state.showUnverifiedBanner) {
                UnverifiedBanner()
                Spacer(Modifier.height(12.dp))
            }

            if (state.images.isNotEmpty()) {
                PhotoStrip(
                    images = state.images,
                    selectedId = state.selectedImageId,
                    isEditMode = state.isEditMode,
                    locationLoading = locationLoading,
                    onCaptureRequested = ::onCaptureRequested,
                    onSelectImage = { onIntent(CameraPickerIntent.SelectImage(it)) },
                    onDeleteImage = { onIntent(CameraPickerIntent.DeleteImage(it)) },
                    onToggleEditMode = { onIntent(CameraPickerIntent.ToggleEditMode) }
                )
                Spacer(Modifier.height(16.dp))
            }

            LocationStatusRow(
                status = if (locationLoading) LocationStatus.Locating else state.locationStatus,
                permission = state.permission,
                onRequestPermission = { onIntent(CameraPickerIntent.RequestPermissionClicked) },
            )
            Spacer(Modifier.height(8.dp))
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = if (scrollState.canScrollForward) 8.dp else 0.dp,
        ) {
            BottomActions(
                allowEnable = state.canComplete,
                capturedSize = state.images.size,
                onAllow = { onIntent(CameraPickerIntent.CompleteClicked) },
                onDeny = { onIntent(CameraPickerIntent.ExitCheck) },
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun PickerHeader() {
    Column {
        Text(
            stringResource(R.string.camerapicker_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.camerapicker_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CaptureArea(
    selectedImage: VerifiedImage?,
    locationLoading: Boolean,
    onCaptureRequested: () -> Unit,
) {
    val overlayVisible = locationLoading && selectedImage != null
    val clickable = !(selectedImage != null && locationLoading)
    val radius = 18.dp
    val ratio = 3f / 4f

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(radius))
            .then(
                if (selectedImage != null) Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                else Modifier
            )
            .then(if (clickable) Modifier.clickable { onCaptureRequested() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        when {
            selectedImage == null -> {
                Box(
                    Modifier
                        .matchParentSize()
                        .dashedBorder(MaterialTheme.colorScheme.outlineVariant, radius)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(46.dp),
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        stringResource(R.string.camerapicker_tap_to_capture),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                AsyncImage(
                    model = selectedImage.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        if (overlayVisible) LocationLoadingOverlay()
    }
}

@Composable
private fun LocationLoadingOverlay() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                strokeWidth = 3.dp,
                color = Color.White,
            )
            Text(
                stringResource(R.string.camerapicker_finding_location),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
            Text(
                stringResource(R.string.camerapicker_finding_location_sub),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun UnverifiedBanner() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Warning, null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                stringResource(R.string.camerapicker_unverified_banner),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun PhotoStrip(
    images: List<VerifiedImage>,
    selectedId: Long?,
    isEditMode: Boolean,
    locationLoading: Boolean,
    onToggleEditMode: () -> Unit,
    onCaptureRequested: () -> Unit,
    onSelectImage: (Long) -> Unit,
    onDeleteImage: (Long) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.camerapicker_picked_count, images.size))
        TextButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onToggleEditMode()
        }) {
            Text(
                stringResource(
                    if (isEditMode) R.string.camerapicker_done
                    else R.string.camerapicker_edit
                )
            )
        }
    }
    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .fadingRightEdge(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp, end = 10.dp),
        ) {
            items(images, key = { it.id }) { image ->
                ThumbnailItem(
                    image = image,
                    selected = image.id == selectedId,
                    editMode = isEditMode,
                    onClick = { onSelectImage(image.id) },
                    onDelete = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteImage(image.id)
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        AddPhotoTile(enabled = !locationLoading, onClick = onCaptureRequested)
    }

}
private val ImageSize = 56.dp
private val BadgeSize = 24.dp
private val Overhang = 6.dp   // 튀어나올 양

@Composable
private fun ThumbnailItem(
    image: VerifiedImage,
    selected: Boolean,
    editMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Box(
        modifier
            .size(width = ImageSize + Overhang, height = ImageSize + Overhang)  // 썸네일 실제 크기 = 사진 + 튀어나온 만큼
    ) {
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .size(ImageSize)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (selected)
                        Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        )
                    else Modifier
                )
                .clickable(onClick = onClick),
        ) {
            AsyncImage(
                model = image.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (editMode) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }
        }

        when {
            editMode -> {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(BadgeSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Close, null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            !image.isVerified -> {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(1.dp)
                        .size(BadgeSize - 2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PriorityHigh, null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPhotoTile(enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .dashedBorder(
                color = if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant,
                cornerRadius = 12.dp,
                on = 4.dp, off = 3.dp,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.AddAPhoto, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.4f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun LocationStatusRow(
    status: LocationStatus,
    permission: LocationPermission,
    onRequestPermission: () -> Unit,
) {
    val (dotColor, label) = when {
        permission != LocationPermission.GRANTED ->
            MaterialTheme.colorScheme.tertiary to stringResource(R.string.camerapicker_permission_off)
        status is LocationStatus.Available ->
            Color(0xFF1D9E75) to stringResource(R.string.camerapicker_location_confirmed)
        status is LocationStatus.Locating ->
            MaterialTheme.colorScheme.onSurfaceVariant to stringResource(R.string.camerapicker_location_locating)
        else ->
            MaterialTheme.colorScheme.error to stringResource(R.string.camerapicker_location_unavailable)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (status is LocationStatus.Locating && permission == LocationPermission.GRANTED) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            } else {
                Box(Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor))
            }
            Spacer(Modifier.width(9.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        when {
            permission != LocationPermission.GRANTED ->
                TextButton(onClick = onRequestPermission) {
                    Text(stringResource(R.string.camerapicker_allow))
                }
            status is LocationStatus.Available ->
                Text(
                    stringResource(R.string.camerapicker_location_accuracy, status.accuracyM.toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            else -> {}
        }
    }
}

@Composable
private fun BottomActions(
    allowEnable: Boolean,
    capturedSize: Int,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onDeny,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(
                stringResource(R.string.camerapicker_exit_without_saving),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onAllow,
            enabled = allowEnable,
            modifier = Modifier
                .weight(1.6f)
                .height(54.dp),
            shape = RoundedCornerShape(15.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.camerapicker_complete_add, capturedSize),
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
    on: Dp = 12.dp,
    off: Dp = 8.dp,
) = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(on.toPx(), off.toPx())),
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

private fun Modifier.fadingRightEdge() = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.horizontalGradient(0.82f to Color.Black, 1f to Color.Transparent),
            blendMode = BlendMode.DstIn,
        )
    }