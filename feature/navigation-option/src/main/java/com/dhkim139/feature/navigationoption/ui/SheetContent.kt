package com.dhkim139.feature.navigationoption.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim139.core.ui.theme.WhereTogoTheme
import com.dhkim139.feature.navigationoption.R
import com.dhkim139.feature.navigationoption.ui.content.MapAppPicker
import com.dhkim139.feature.navigationoption.ui.content.Overview
import com.dhkim139.feature.navigationoption.ui.content.ParkingPicker

internal const val DIMMED_ALPHA = 0.4f

@Composable
internal fun NavOptionContent(
    content: SheetContent,
    onIntent: (NavOptionIntent) -> Unit,
    overviewMaxHeight: Dp = 250.dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        AnimatedContent(
            targetState = content,
            contentKey = { it.javaClass },
            transitionSpec = {
                SheetMotion.transitionFor(initialState, targetState)
            },
            label = "navOptionMode",
        ) { current ->
            Column {
                when (current) {
                    is SheetContent.Overview -> Overview(
                        overview = current,
                        onToggleMyLocation = {
                            onIntent(NavOptionIntent.ToggleMyLocation)
                        },
                        onOpenItem = { onIntent(NavOptionIntent.EndpointClicked(it)) },
                        onChangeMapApp = {
                            onIntent(NavOptionIntent.OpenMapAppPicker)
                        },
                        onConfirm = { onIntent(NavOptionIntent.Confirm) },
                        modifier = Modifier.heightIn(max = overviewMaxHeight)
                    )

                    is SheetContent.ParkingPicker -> ParkingPicker(
                        parkingPicker = current,
                        onSelectLot = { onIntent(NavOptionIntent.SelectLot(it)) },
                        onClearParking = { onIntent(NavOptionIntent.ClearParking) },
                        onClose = { onIntent(NavOptionIntent.ClosePicker) },
                    )

                    is SheetContent.MapAppPicker -> MapAppPicker(
                        mapApps = current,
                        onSelect = { onIntent(NavOptionIntent.SelectMapApp(it)) },
                        onClose = { onIntent(NavOptionIntent.CloseMapAppPicker) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GrabHandle() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .padding(bottom = 12.dp)
                .size(width = 32.dp, height = 4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
internal fun RailDot(size: Dp) {
    Box(
        Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(size / 2)),
    )
}

@Composable
internal fun ParkingBadge(selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (selected) null else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = Modifier.size(24.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.parking_badge),
                fontSize = 12.sp,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}


@Preview(name = "내 위치 포함", showBackground = true, widthDp = 360)
@Composable
private fun PreviewOverview() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.overviewContent, onIntent = {}) }
}

@Preview(name = "내 위치 제외", showBackground = true, widthDp = 360)
@Composable
private fun PreviewNoMyLocation() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.noMyLocationContent, onIntent = {}) }
}


@Preview(name = "주차장 선택", showBackground = true, widthDp = 360)
@Composable
private fun PreviewParked() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.parkedContent, onIntent = {}) }
}

@Preview(name = "주차장 있음", showBackground = true, widthDp = 360)
@Composable
private fun PreviewPicker() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.pickerContent, onIntent = {}) }
}

@Preview(name = "주차장 없음", showBackground = true, widthDp = 360)
@Composable
private fun PreviewEmpty() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.emptyPickerContent, onIntent = {}) }
}


@Preview(name = "주차장 로딩", showBackground = true, widthDp = 360)
@Composable
private fun PreviewLoading() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.loadingPickerContent, onIntent = {}) }
}

@Preview(name = "지도 앱 목록", showBackground = true, widthDp = 360)
@Composable
private fun PreviewMapAppPicker() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.mapAppPickerContent, onIntent = {}) }
}

@Preview(name = "카카오 · 내 위치 있음", showBackground = true, widthDp = 360)
@Composable
private fun PreviewKakaoParked() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.kakaoParkedContent, onIntent = {}) }
}

@Preview(name = "카카오 · 내 위치 없음", showBackground = true, widthDp = 360)
@Composable
private fun PreviewKakaoNoLocation() {
    WhereTogoTheme { NavOptionContent(content = PreviewData.kakaoNoLocationContent, onIntent = {}) }
}

