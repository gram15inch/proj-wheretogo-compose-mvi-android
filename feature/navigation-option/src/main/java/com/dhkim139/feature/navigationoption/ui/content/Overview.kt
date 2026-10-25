package com.dhkim139.feature.navigationoption.ui.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim139.core.ui.screen.pressHighlightEffect
import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.R
import com.dhkim139.feature.navigationoption.ui.DIMMED_ALPHA
import com.dhkim139.feature.navigationoption.ui.DeliveryNotice
import com.dhkim139.feature.navigationoption.ui.OverviewRow
import com.dhkim139.feature.navigationoption.ui.RailDot
import com.dhkim139.feature.navigationoption.ui.SheetContent
import com.dhkim139.feature.navigationoption.ui.SheetMotion
import com.dhkim139.feature.navigationoption.ui.choiceSubtitle
import com.dhkim139.feature.navigationoption.ui.noticeLabel

@Composable
internal fun Overview(
    overview: SheetContent.Overview,
    onToggleMyLocation: () -> Unit,
    onOpenItem: (EndpointRole) -> Unit,
    onChangeMapApp: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        overview.rows.forEach { row ->
            when (row) {
                OverviewRow.MyLocation -> MyLocationRow(
                    overview.useMyLocation,
                    onToggleChange = onToggleMyLocation
                )

                is OverviewRow.Endpoint -> EndpointRow(
                    row = row,
                    onClick = { onOpenItem(row.endpoint.role) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(
            visible = overview.notice != null,
            enter = fadeIn(tween(SheetMotion.PARTIAL_MS)) +
                    expandVertically(tween(SheetMotion.PARTIAL_MS)),
            exit = fadeOut(tween(SheetMotion.EXIT_MS)) +
                    shrinkVertically(tween(SheetMotion.PARTIAL_MS)),
        ) {
            Column {
                NoticeLine(notice = overview.notice, mapApp = overview.mapApp)
                Spacer(Modifier.height(4.dp))
            }
        }
        NavigateButton(
            mapApp = overview.mapApp,
            onConfirm = onConfirm,
            onChangeApp = onChangeMapApp,
        )
    }
}


@Composable
private fun MyLocationRow(
    checked: Boolean,
    onToggleChange: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RailDot(size = 6.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.overview_my_location_row),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        SmallSwitch(
            checked = checked,
            onCheckedChange = {
                onToggleChange()
            },
            contentDescription = null,
        )
    }
}

@Composable
private fun SmallSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    trackWidth: Dp = 36.dp,
    trackHeight: Dp = 20.dp,
    thumbSize: Dp = 16.dp,
    thumbPadding: Dp = 2.dp,
) {
    val spec = tween<Dp>(durationMillis = 150)
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = spec,
        label = "thumbOffset",
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(150),
        label = "trackColor",
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.outline,
        animationSpec = tween(150),
        label = "thumbColor",
    )

    Box(
        modifier
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor),
        )
    }
}

@Composable
private fun EndpointRow(
    row: OverviewRow.Endpoint,
    onClick: () -> Unit,
) {
    val choice = row.choice
    val parked = choice.parked
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val roleLabel = stringResource(row.endpoint.role.labelRes)
    val rowAlpha by animateFloatAsState(
        targetValue = if (row.delivered) 1f else DIMMED_ALPHA,
        animationSpec = tween(SheetMotion.PARTIAL_MS),
        label = "endpointRowAlpha",
    )

    Row(
        Modifier
            .fillMaxWidth()
            .pressHighlightEffect(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = row.canPark,
                onClick = onClick,
            )
            .padding(vertical = 10.dp)
            .padding(start = 5.dp)
            .alpha(rowAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CourseThumbnail(row.endpoint.role)
        Spacer(Modifier.width(15.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = choice.lotName?.takeIf { parked } ?: roleLabel,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (parked) {
                    choiceSubtitle(choice, roleLabel)
                } else {
                    row.endpoint.addressText
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(8.dp))
        StatusPill(parked = parked, canPark = row.canPark)
    }
}

@Composable
private fun CourseThumbnail(role: EndpointRole) {
    val icon = if (role == EndpointRole.START) Icons.Default.Adjust else Icons.Default.Flag
    Box(
        Modifier
            .size(26.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun NoticeLine(notice: DeliveryNotice?, mapApp: MapApp) {
    var last by remember { mutableStateOf(notice) }
    if (notice != null) last = notice
    val shown = last ?: return

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = noticeLabel(shown, mapApp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusPill(parked: Boolean, canPark: Boolean) {
    AnimatedContent(
        targetState = parked,
        transitionSpec = {
            (fadeIn(tween(SheetMotion.PARTIAL_MS)) + scaleIn(initialScale = 0.8f))
                .togetherWith(fadeOut(tween(SheetMotion.EXIT_MS)) + scaleOut(targetScale = 0.8f))
        },
        label = "statusPill",
    ) { isParked ->
        if (!isParked) {
            Text(
                text = stringResource(
                    if (canPark) R.string.overview_status_none
                    else R.string.overview_status_locked
                ),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.alpha(if (canPark) 1f else DIMMED_ALPHA),
            )
        } else {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    text = stringResource(R.string.overview_status_parked),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun NavigateButton(
    mapApp: MapApp,
    onConfirm: () -> Unit,
    onChangeApp: () -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        Button(
            onClick = onConfirm,
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
        ) {
            Text(
                stringResource(
                    R.string.overview_navigate_with,
                    stringResource(mapApp.labelRes),
                )
            )
        }
        Spacer(Modifier.width(1.dp))
        Button(
            onClick = onChangeApp,
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .width(48.dp)
                .height(48.dp),
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.change_map_app),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
