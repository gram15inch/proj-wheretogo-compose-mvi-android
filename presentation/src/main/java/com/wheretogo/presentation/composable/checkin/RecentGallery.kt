package com.wheretogo.presentation.composable.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.pressHighlightEffect
import com.wheretogo.presentation.model.MiniPhoto
import com.wheretogo.presentation.theme.WhereTogoTheme

@Composable
fun RecentGallery(
    photos: List<MiniPhoto>,
    maxCol: Int = 4,
    onViewAllClick: () -> Unit = {},
    onPhotosClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.latest_certification),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onViewAllClick)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.view_all),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.primary,
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressHighlightEffect(interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onPhotosClick,
                )
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val tileModifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
            photos.take(maxCol).forEach { photo ->
                CertPhotoTile(photo = photo, modifier = tileModifier)
            }
            repeat((maxCol - photos.size).coerceAtLeast(0)) { index ->
                EmptySlotTile(depth = index, modifier = tileModifier)
            }
        }
    }
}

@Composable
private fun CertPhotoTile(
    photo: MiniPhoto,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (photo.photoUri != null) {
            AsyncImage(
                model = photo.photoUri,
                contentDescription = photo.courseName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                    ),
                )
                .padding(horizontal = 5.dp, vertical = 4.dp),
        ) {
            Text(
                text = photo.courseName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmptySlotTile(
    depth: Int,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    val alpha = when (depth) {
        0 -> 0.55f
        1 -> 0.35f
        else -> 0.22f
    }
    val strokeColor = cs.onSurfaceVariant.copy(alpha = alpha)
    val cornerRadius = 14.dp
    val strokeWidth = 1.5.dp

    Box(
        modifier = modifier
            .size(74.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = strokeWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
                    ),
                )
                val inset = strokeWidth.toPx() / 2
                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(cornerRadius.toPx() - inset),
                    style = stroke,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Photo,
            contentDescription = null,
            tint = strokeColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview()
@Composable
private fun MyCertificationsGalleryPreviews() {
    WhereTogoTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            RecentGallery(
                photos = emptyList(),
                onViewAllClick = {},
                onPhotosClick = {}
            )
            RecentGallery(
                photos = listOf(MiniPhoto(1, null, "한강 야경길")),
                onViewAllClick = {},
                onPhotosClick = {}
            )
        }
    }
}