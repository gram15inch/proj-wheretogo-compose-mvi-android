package com.dhkim139.feature.navigationoption.ui.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.R
import com.dhkim139.feature.navigationoption.ui.ParkingBadge
import com.dhkim139.feature.navigationoption.ui.ParkingCandidate
import com.dhkim139.feature.navigationoption.ui.RailDot
import com.dhkim139.feature.navigationoption.ui.RowListPlaceholder
import com.dhkim139.feature.navigationoption.ui.SheetContent
import com.dhkim139.feature.navigationoption.ui.SheetMotion
import com.dhkim139.feature.navigationoption.ui.candidateMeta

private val LIST_HEIGHT = 200.dp

private data class Target(val sheet: SheetContent.ParkingPicker, val height: Dp)

@Composable
internal fun ParkingPicker(
    parkingPicker: SheetContent.ParkingPicker,
    onSelectLot: (String) -> Unit,
    onClearParking: () -> Unit,
    onClose: () -> Unit,
) {
    val roleLabel = stringResource(parkingPicker.role.labelRes)
    val listState = rememberLazyListState()
    var height by remember { mutableStateOf(120.dp) }
    LaunchedEffect(parkingPicker.role) {
        listState.scrollToItem(0)
    }

    LaunchedEffect(parkingPicker.loading) {
        if (!parkingPicker.loading && parkingPicker.candidates.isNotEmpty())
            height = LIST_HEIGHT
    }

    LaunchedEffect(parkingPicker.selectedLotId, parkingPicker.candidates) {
        val lotId = parkingPicker.selectedLotId ?: return@LaunchedEffect
        val index = parkingPicker.candidates.indexOfFirst { it.lot.id == lotId }
        if (index < 0) return@LaunchedEffect

        val info = listState.layoutInfo
        val item = info.visibleItemsInfo.firstOrNull { it.index == index }
        val viewportEnd = info.viewportEndOffset - info.afterContentPadding

        when {
            item == null -> listState.animateScrollToItem(index)

            item.offset + item.size > viewportEnd ->
                listState.animateScrollBy((item.offset + item.size - viewportEnd).toFloat())

            item.offset < info.viewportStartOffset -> listState.animateScrollToItem(index)
        }
    }

    CollapsedContextLine(
        otherRole = parkingPicker.otherRole,
        otherParked = parkingPicker.otherParked,
        showsMyLocation = parkingPicker.showsMyLocation,
    )

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(R.string.picker_title, roleLabel), fontSize = 14.sp)
        Text(
            text = stringResource(R.string.picker_show_all),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onClose),
        )
    }

    AnimatedContent(
        targetState = Target(parkingPicker, height),
        contentKey = { it.sheet.candidates.size },
        label = "radius",
    ) { target ->
        val current = target.sheet
        val rows = current.candidates
        Box(
            Modifier
                .fillMaxWidth()
                .height(target.height)
        ) {
            when {
                current.loading -> {
                    RowListPlaceholder(2)
                }

                rows.isEmpty() -> EmptyCandidates()
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 4.dp),
                ) {
                    items(
                        count = rows.size,
                        key = { index -> rows[index].lot.id },
                    ) { index ->
                        val candidate = rows[index]
                        CandidateRow(
                            candidate = candidate,
                            selected = current.selectedLotId == candidate.lot.id,
                            onSelect = { onSelectLot(candidate.lot.id) },
                        )
                    }
                }
            }
        }


    }


    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onClearParking,
            modifier = Modifier.height(48.dp),
        ) { Text(stringResource(R.string.picker_clear)) }
        Button(
            onClick = onClose,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
        ) { Text(stringResource(R.string.picker_confirm)) }
    }
}

@Composable
private fun CollapsedContextLine(
    otherRole: EndpointRole,
    otherParked: Boolean,
    showsMyLocation: Boolean,
) {
    val otherText = stringResource(
        if (otherParked) {
            R.string.picker_other_parked
        } else {
            R.string.picker_other_not_parked
        },
        stringResource(otherRole.labelRes),
    )
    val text = listOfNotNull(
        stringResource(R.string.my_location).takeIf { showsMyLocation },
        otherText,
    ).joinToString(stringResource(R.string.separator))

    Row(
        Modifier
            .fillMaxWidth()
            .height(24.dp)
            .alpha(0.45f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RailDot(size = 5.dp)
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CandidateRow(
    candidate: ParkingCandidate,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = tween(SheetMotion.PARTIAL_MS, easing = SheetMotion.Emphasized),
        label = "candidateBorder",
    )

    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ParkingBadge(selected)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = candidate.lot.name,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = candidateMeta(candidate),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.nav_option_selected),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EmptyCandidates() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.picker_empty_title, 1),
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.picker_empty_body),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}