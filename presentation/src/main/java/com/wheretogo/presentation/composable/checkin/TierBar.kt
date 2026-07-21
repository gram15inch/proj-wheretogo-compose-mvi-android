package com.wheretogo.presentation.composable.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.model.Tier
import com.wheretogo.presentation.theme.Palette
import com.wheretogo.presentation.theme.WhereTogoTheme


@Composable
fun TierBar(
    tier: Tier,
    achieved: Boolean,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (achieved) cs.primaryContainer.copy(alpha = 0.4f) else Palette.Gray100.copy(
            alpha = 0.2f
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = tier.icon,
                contentDescription = null,
                tint = if (achieved) tier.color.copy(alpha = 0.6f) else cs.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(tier.nameRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (achieved) FontWeight.Bold else FontWeight.Normal,
                    color = if (achieved) cs.onSurface else cs.onSurfaceVariant,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.tier_requirement,
                        tier.requiredCount,
                        tier.requiredCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                )
            }
            if (achieved) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TierRoadmapItemPreviews() {
    WhereTogoTheme {
        Column {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Tier.default.forEach {
                    TierBar(it, true)
                }
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Tier.default.forEach {
                    TierBar(it, false)
                }
            }
        }

    }
}
