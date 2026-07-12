package com.wheretogo.presentation.composable.photoviewer


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.theme.WhereTogoTheme


@Composable
fun NoLocationSection(
    modifier: Modifier = Modifier,
    onOpenCameraSettings: () -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    var reasonsExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (reasonsExpanded) 180f else 0f,
        label = "arrowRotation",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(cs.primary.copy(alpha = 0.06f))
                .padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.no_location_transparency_title),
                    color = cs.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(4.dp))
            stringArrayResource(R.array.no_location_usage_chips).forEach { label ->
                Row(
                    Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        label,
                        color = cs.primary,
                        fontSize = 12.5.sp,
                        lineHeight = 14.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 원인 섹션
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { reasonsExpanded = !reasonsExpanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.no_location_reasons_title),
                color = cs.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.no_location_action_learn_more),
                color = cs.onSurfaceVariant,
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(arrowRotation),
            )
        }

        AnimatedVisibility(visible = reasonsExpanded) {
            Column {
                Spacer(Modifier.height(6.dp))
                stringArrayResource(R.array.no_location_reasons).forEach { reason ->
                    Row(Modifier.padding(vertical = 3.dp)) {
                        Icon(
                            Icons.Outlined.FiberManualRecord,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(8.dp).padding(top = 6.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            reason,
                            color = cs.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = cs.outlineVariant)
        Spacer(Modifier.height(11.dp))

        // 해결 안내
        Text(
            stringResource(R.string.no_location_solution_title),
            color = cs.onSurface,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            stringResource(R.string.no_location_solution_body),
            color = cs.onSurfaceVariant,
            fontSize = 12.5.sp,
            lineHeight = 20.sp,
        )

        Spacer(Modifier.height(14.dp))

        // 액션 버튼
        OutlinedButton(
            onClick = onOpenCameraSettings,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 10.dp),
        ) {
            Icon(Icons.Outlined.Settings, null, Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text(stringResource(R.string.no_location_action_camera_settings), fontSize = 13.sp)
        }
    }
}

@Composable
@Preview
private fun NoLocationInfoCardAPreview() {
    WhereTogoTheme {
        Surface {
            NoLocationSection(
                modifier = Modifier.padding(16.dp),
                onOpenCameraSettings = {}
            )
        }
    }
}