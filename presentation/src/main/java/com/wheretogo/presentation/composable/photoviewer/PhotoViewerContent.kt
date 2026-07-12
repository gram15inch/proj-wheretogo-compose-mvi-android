package com.wheretogo.presentation.composable.photoviewer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.naver.maps.map.MapView
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.NaverMapSheet
import com.wheretogo.presentation.composable.content.RowAdPlaceholder
import com.wheretogo.presentation.composable.photoviewer.badge.StatusBadge
import com.wheretogo.presentation.model.CameraOption
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.NaverMapStyle
import com.wheretogo.presentation.state.NaverMapState
import com.wheretogo.presentation.state.StampProgress
import com.wheretogo.presentation.theme.Palette
import com.wheretogo.presentation.theme.WhereTogoTheme
import com.wheretogo.presentation.theme.interFontFamily
import com.wheretogo.presentation.viewmodel.MapEvent
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface StampState {
    data object Empty : StampState
    data object Loading : StampState
    data class Stamped(
        val stampedAt: Long,
        val thumbnail: String?,
        val description: String?
    ) : StampState
}

data class PhotoViewerActions(
    val onStamp: (description: String?) -> Unit = {},
    val onRemoveStamp: () -> Unit = {},
    val onCameraUpdate: (CameraState) -> Unit = {}
)

@Composable
fun PhotoViewerContent(
    mapView: MapView? = null,
    photo: GalleryPhoto? = null,
    overlay: List<MapOverlay> = emptyList(),
    event: SharedFlow<MapEvent>? = null,
    fingerPrint: Int? = null,
    stampProgress: StampProgress = StampProgress.Idle,
    stampState: StampState = StampState.Empty,
    actions: PhotoViewerActions = PhotoViewerActions(),
) {
    val context = LocalContext.current
    var showStampInput by remember { mutableStateOf(false) }
    var showRemoveSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(stampState, stampProgress) {
        when (stampState) {
            is StampState.Stamped,
            is StampState.Empty -> {
                if (stampProgress == StampProgress.Idle) {
                    if (showStampInput || showRemoveSheet) {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    }
                    showStampInput = false
                    showRemoveSheet = false
                }
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
    ) {
        val courseId = photo?.courseId
        val name = photo?.courseName
        val date = photo?.exif?.dateTaken
        val location = photo?.exif?.location
        val address = photo?.simpleAddress()
        val hasDate = date != null && date != 0L
        val hasCourse = !(courseId.isNullOrBlank() || name.isNullOrBlank())
        val hasLocation = location != null && mapView != null
        val hasAddress = address != null

        Column(
            modifier = Modifier
                .padding(top = 18.dp)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 뱃지
            when {
                !hasLocation -> {
                    StatusBadge(
                        text = stringResource(R.string.no_location_status),
                        icon = Icons.Outlined.LocationOff,
                    )
                }

                !hasCourse -> {
                    StatusBadge(
                        text = stringResource(R.string.no_match_header_label),
                        icon = Icons.Outlined.Explore
                    )
                }
            }

            // 본문
            when {
                !hasLocation -> {
                    NoLocationSection(
                        onOpenCameraSettings = { openCameraAppInfo(context) }
                    )
                }

                else -> {
                    // 헤더
                    when {
                        !name.isNullOrBlank() -> {
                            MatchHeader(
                                name = name,
                                address = address ?: "",
                                stampState = stampState,
                                onMore = { showRemoveSheet = true },
                            )
                        }

                        !hasCourse -> {
                            NoMatchHeader()
                        }
                    }

                    // 지도
                    NaverMapSheet(
                        mapView = mapView,
                        state = NaverMapState(initCamera = CameraOption.fromGalleryPhoto(photo)),
                        style = NaverMapStyle.Place,
                        overlayGroup = overlay,
                        event = event,
                        fingerPrint = fingerPrint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                            ),
                        onCameraUpdate = actions.onCameraUpdate
                    )

                    // 바디
                    when {
                        hasCourse -> {
                            when (stampState) {
                                is StampState.Empty -> EmptySection(onStamp = {
                                    showStampInput = true
                                })

                                is StampState.Stamped -> StampedSection(state = stampState)
                                is StampState.Loading -> RowAdPlaceholder()
                            }
                            if (showStampInput) {
                                StampInputSheet(
                                    placeName = name,
                                    photo = photo,
                                    isLoading = stampProgress == StampProgress.Stamping,
                                    onConfirm = { desc ->
                                        actions.onStamp(desc)
                                    },
                                    onDismiss = { showStampInput = false },
                                )
                            }
                            if (showRemoveSheet) {
                                RemoveStampSheet(
                                    isLoading = stampProgress == StampProgress.Removing,
                                    onConfirm = {
                                        actions.onRemoveStamp()
                                    },
                                    onDismiss = { showRemoveSheet = false },
                                )
                            }
                        }

                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (hasDate)
                                    DateLabel(date)
                                if (hasAddress)
                                    AddressLabel(address)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchHeader(
    name: String,
    address: String,
    stampState: StampState,
    onMore: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StampBadge(stampState)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (stampState is StampState.Stamped) {
            IconButton(onClick = onMore) {
                Icon(Icons.Default.MoreVert, null)
            }
        }
    }
}

private fun millisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy.MM.dd")
    return sdf.format(Date(millis))
}

@Composable
fun NoMatchHeader(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = stringResource(R.string.no_match_header_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.no_match_header_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun StampBadge(stampState: StampState) {
    val stamped = stampState is StampState.Stamped
    Box(
        modifier = Modifier
            .then(
                if (stamped) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer, CircleShape
                ) else Modifier
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(
            imageVector = Icons.Outlined.Verified,
            contentDescription = null,
            tint = if (stamped) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(42.dp)
        )
    }
}

@Composable
private fun EmptySection(
    onStamp: () -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.swipe_to_pick_photo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onStamp,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Outlined.Verified, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.pick_this_photo),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DateLabel(ts: Long) {
    Row(
        modifier = Modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(Date(ts)),
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun AddressLabel(address: String) {
    Row(
        modifier = Modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = address,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun StampedSection(
    state: StampState.Stamped
) {
    val isPreview = LocalInspectionMode.current
    if (state.thumbnail.isNullOrBlank()) return

    val surfaceColor = Color(0xFFF7F5F0)
    val accentColor = Palette.MutedBlue

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = accentColor.copy(alpha = 0.15f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)          // 배경은 크림
            .padding(12.dp)                    // border 제거
    ) {
        if (isPreview) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f))
            )
        } else {
            AsyncImage(
                model = state.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    stringResource(
                        R.string.num_visit,
                        millisToDate(state.stampedAt)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                )
            }

            if (!state.description.isNullOrBlank()) {
                Text(
                    state.description,
                    style = TextStyle(
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Palette.Black50
                    ),
                )
            }
        }
    }
}

@Composable
@Preview
private fun MatchHeaderPreview() {
    WhereTogoTheme {
        Column {
            MatchHeader(
                name = "더 현대 야경길",
                address = "대한민국 서울특별시",
                stampState = StampState.Empty,
                {}
            )
            MatchHeader(
                name = "더 현대 야경길",
                address = "대한민국 서울특별시",
                stampState =
                    StampState.Stamped(
                        0,
                        null,
                        ""
                    ),
                {}
            )
        }

    }
}

@Composable
@Preview
private fun NoMatchHeaderPreview() {
    WhereTogoTheme {
        NoMatchHeader(modifier = Modifier.padding(16.dp))
    }
}

@Composable
@Preview
private fun LabelPreview() {
    WhereTogoTheme {
        Column {
            DateLabel(0L)
            AddressLabel("서울시 중구 231-3")
        }
    }
}

@Composable
@Preview
private fun EmptySectionPreview() {
    WhereTogoTheme {
        EmptySection({})
    }
}

@Composable
@Preview
private fun StampedSelectionPreview() {
    WhereTogoTheme {
        Column {
            StampedSection(
                StampState.Stamped(
                    stampedAt = 0L,
                    thumbnail = "preview",
                    description = "좋았던 곳. 다시 방문하고 싶어요, 최고의 사진이에요."
                )
            )
        }

    }
}

fun openCameraAppInfo(context: Context) {
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val resolveInfo = context.packageManager.resolveActivity(
        cameraIntent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    val packageName = resolveInfo?.activityInfo?.packageName

    if (packageName != null && packageName != "android") {
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        context.startActivity(settingsIntent)
    } else {
        Toast.makeText(context, "기본 카메라 앱을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
    }
}



