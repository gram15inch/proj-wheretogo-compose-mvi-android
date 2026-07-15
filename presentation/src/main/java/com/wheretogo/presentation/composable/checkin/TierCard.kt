package com.wheretogo.presentation.composable.checkin

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
import androidx.compose.material.icons.outlined.Approval
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.model.Tier
import com.wheretogo.presentation.theme.Palette
import com.wheretogo.presentation.theme.WhereTogoTheme


@Composable
fun TierCard(
    certifiedCount: Int,
    modifier: Modifier = Modifier,
    tiers: List<Tier> = Tier.default,
) {
    val cs = MaterialTheme.colorScheme
    val currentTier = tiers.lastOrNull { certifiedCount >= it.requiredCount }
    val nextTier = tiers.firstOrNull { certifiedCount < it.requiredCount }
    val remaining = nextTier?.let { it.requiredCount - certifiedCount } ?: 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Palette.Cream,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    StampCountChip(
                        text = stringResource(
                            R.string.tier_certified_format,
                            certifiedCount,
                        ),
                    )

                    Spacer(Modifier.weight(1f))

                    if (nextTier != null) {
                        Text(
                            stringResource(R.string.tier_remaining_format, remaining),
                            color = cs.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    } else {
                        Icon(
                            Icons.Outlined.WorkspacePremium,
                            contentDescription = stringResource(R.string.tier_max_title),
                            tint = cs.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentTier != null) {
                    TierBadgePill(
                        name = stringResource(currentTier.nameRes),
                        icon = currentTier.icon,
                        color = currentTier.color,
                        achieved = true,
                    )
                }
                if (nextTier != null) {
                    val currentRequired = currentTier?.requiredCount ?: 0
                    val totalDots = nextTier.requiredCount - currentRequired - 1
                    val activeDots = certifiedCount - currentRequired

                    if (totalDots in 0..8) {
                        DotsSegment(
                            activeDots = activeDots,
                            totalDots = totalDots,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        ConnectorLine(solid = false, modifier = Modifier.width(14.dp))
                    }
                    TierBadgePill(
                        name = stringResource(nextTier.nameRes),
                        icon = nextTier.icon,
                        color = nextTier.color,
                        achieved = false,
                    )
                } else {
                    ConnectorLine(solid = true, modifier = Modifier.weight(1f))
                    ConnectorLine(solid = true, modifier = Modifier.weight(1f))
                    ChampionBadgePill()
                }
            }
        }
    }
}


@Composable
private fun StampCountChip(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Approval, null, tint = Palette.Gray250, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Palette.Gray250)
        }
        Spacer(Modifier.height(3.dp))
        Box(Modifier.width(50.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(Palette.OutlineGray.copy(alpha = 0.4f)))
    }
}

@Composable
private fun DotsSegment(
    activeDots: Int,
    totalDots: Int,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(totalDots) { index ->
            ConnectorLine(
                solid = index < activeDots,
                modifier = Modifier.weight(1f),
            )
            when {
                index < activeDots - 1 -> {
                    ConnectorLine(solid = true, modifier = Modifier.width(9.dp))
                }

                index == activeDots - 1 -> {
                    Box(
                        Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(cs.primary),
                    )
                }

                else -> {
                    Box(
                        Modifier
                            .size(9.dp)
                            .dashlessRing(cs.outline, 1.5.dp),
                    )
                }
            }
        }
        ConnectorLine(solid = false, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ConnectorLine(solid: Boolean, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val color = if (solid) cs.primary else Palette.OutlineGray
    val density = LocalDensity.current
    Box(
        modifier
            .height(2.dp)
            .drawBehind {
                val effect = if (solid) null
                else PathEffect.dashPathEffect(
                    with(density) { floatArrayOf(3.dp.toPx(), 4.dp.toPx()) }, 0f,
                )
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    pathEffect = effect,
                )
            },
    )
}


@Composable
private fun TierBadgePill(name: String, icon: ImageVector, color: Color, achieved: Boolean) {
    val shape = RoundedCornerShape(50)  // 완전 pill
    val cs = MaterialTheme.colorScheme
    val contentColor =
        if (achieved) color
        else Palette.OutlineGray

    Row(
        Modifier
            .then(
                if (achieved) Modifier
                    .clip(shape)
                    .background(contentColor.copy(alpha = 0.1f))
                else Modifier.dashedBorder(contentColor, shape)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            name,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp,
        )
    }
}

@Composable
private fun ChampionBadgePill(modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Box(modifier) {
        Row(
            Modifier
                .padding(top = 7.dp, end = 5.dp)
                .clip(shape)
                .background(cs.primary)
                .padding(horizontal = 13.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = cs.onPrimary,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                stringResource(R.string.tier_champion),
                color = cs.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(Palette.Cream)
                .padding(2.dp)
                .clip(CircleShape)
                .background(cs.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = cs.onPrimaryContainer,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}


private fun Modifier.dashlessRing(color: Color, width: Dp) = drawBehind {
    drawCircle(
        color = color,
        style = Stroke(width = width.toPx()),
        radius = (size.minDimension - width.toPx()) / 2,
    )
}


private fun Modifier.dashedBorder(color: Color, shape: RoundedCornerShape) = drawBehind {
    val stroke = Stroke(
        width = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f,
        ),
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(10.dp.toPx()),
    )
}


@Preview(showBackground = true)
@Composable
private fun TierCardPreviews() {
    WhereTogoTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TierCard(certifiedCount = 0)
            TierCard(certifiedCount = 2)
            TierCard(certifiedCount = 4)
            TierCard(certifiedCount = 5)
            TierCard(certifiedCount = 6)
            TierCard(certifiedCount = 15)
        }
    }
}
