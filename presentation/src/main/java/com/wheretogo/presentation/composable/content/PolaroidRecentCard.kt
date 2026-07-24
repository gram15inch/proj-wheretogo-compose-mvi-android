package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wheretogo.domain.RecentCardSituation
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.pressHighlightEffect
import com.wheretogo.presentation.model.home.RecentCardUiState
import com.wheretogo.presentation.theme.Palette
import com.wheretogo.presentation.theme.WhereTogoTheme
import java.util.concurrent.TimeUnit


@Composable
fun PolaroidRecentCard(
    state: RecentCardUiState,
    onCardClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val headline = rememberSituationHeadline(state.situation)
    val lastStampLabel = rememberLastStampLabel(state.stampAt)
    val kaomoji = rememberSituationKaomoji(state.situation)


    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cs.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressHighlightEffect(interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onCardClick,
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PolaroidFrame(imageModel = state.imageModel)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleSmall,
                    lineHeight = 22.sp,
                    color = cs.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (lastStampLabel == null) {
                        "$kaomoji · ${stringResource(R.string.recent_card_total_empty)}"
                    } else {
                        "$kaomoji · $lastStampLabel"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }


            AddButton(highlighted = state.isEmpty, onClick = onAddClick)
        }
    }
}


@Composable
private fun PolaroidFrame(
    imageModel: String?,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .rotate(-3f)
            .clip(RoundedCornerShape(6.dp))
            .background(cs.surface)
            .border(1.dp, cs.outline.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(start = 6.dp, top = 6.dp, end = 6.dp, bottom = 18.dp),
    ) {
        val imageModifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(3.dp))

        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = imageModifier,
            )
        } else {
            Box(
                modifier = imageModifier.background(Palette.Cream),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun AddButton(
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val unActive = cs.outline
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(40.dp)
            .background(if (highlighted) cs.primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = if (highlighted) cs.onPrimary else unActive,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun PolaroidRecentCardPreview() {
    WhereTogoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PolaroidRecentCard(
                state = RecentCardUiState(
                    imageModel = null,
                    stampAt = System.currentTimeMillis(),
                    situation = RecentCardSituation.PAUSED,
                ),
            )
            PolaroidRecentCard(
                state = RecentCardUiState(
                    imageModel = null,
                    stampAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10),
                    situation = RecentCardSituation.DORMANT,
                ),
            )
            PolaroidRecentCard(state = RecentCardUiState.Empty)
        }
    }
}