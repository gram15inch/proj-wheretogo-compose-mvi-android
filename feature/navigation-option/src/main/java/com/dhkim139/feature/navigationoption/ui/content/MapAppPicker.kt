package com.dhkim139.feature.navigationoption.ui.content

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.R
import com.dhkim139.feature.navigationoption.ui.SheetContent


@Composable
internal fun MapAppPicker(
    mapApps: SheetContent.MapAppPicker,
    onSelect: (MapApp) -> Unit,
    onClose: () -> Unit,
) {
    val splitGroups = mapApps.splitGroups

    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(R.string.app_picker_title), fontSize = 15.sp)
        Text(
            text = stringResource(R.string.app_picker_close),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onClose),
        )
    }

    if (splitGroups) GroupLabel(R.string.app_picker_group_full, first = true)
    mapApps.full.forEach { app ->
        MapAppRow(
            app = app,
            selected = app == mapApps.selected,
            subtitle = null,
            onClick = { onSelect(app) },
        )
    }

    if (splitGroups) GroupLabel(R.string.app_picker_group_limited, first = false)
    mapApps.limited.forEach { app ->
        MapAppRow(
            app = app,
            selected = app == mapApps.selected,
            subtitle = if (splitGroups) {
                stringResource(R.string.app_picker_limit)
            } else {
                null
            },
            onClick = { onSelect(app) },
        )
    }
}


@Composable
private fun GroupLabel(@StringRes textRes: Int, first: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = if (first) 0.dp else 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(textRes),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun MapAppRow(
    app: MapApp,
    selected: Boolean,
    subtitle: String?,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .then(
                if (selected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, shape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .alpha(if (subtitle == null) 1f else 0.65f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(26.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(7.dp)),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(app.labelRes),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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

