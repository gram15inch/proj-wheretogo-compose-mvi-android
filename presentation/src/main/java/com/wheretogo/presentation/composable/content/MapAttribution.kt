package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim139.core.ui.screen.expandedClickable
import com.dhkim139.core.ui.theme.Palette
import com.wheretogo.presentation.R

@Composable
internal fun MapAttribution(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val geoapifyUrl = stringResource(R.string.attribution_geoapify_url)
    val osmUrl = stringResource(R.string.attribution_osm_url)
    Row(
        modifier
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 3.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        AttributionLink(stringResource(R.string.attribution_geoapify)) { uriHandler.openUri(geoapifyUrl) }
        AttributionText("|")
        AttributionLink(stringResource(R.string.attribution_osm)) { uriHandler.openUri(osmUrl) }
    }
}
@Composable
private fun AttributionLink(text: String, onClick: () -> Unit) {
    AttributionText(text,
        Modifier.expandedClickable(
            horizontal = 6.dp,
            vertical = 10.dp,
            role = Role.Button,
            onClick = onClick,
        ),
    )
}

@Composable
private fun AttributionText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, color = Palette.MapAttr, fontSize = 10.sp, lineHeight = 12.sp)
}
