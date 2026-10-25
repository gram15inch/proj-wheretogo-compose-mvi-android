package com.dhkim139.feature.navigationoption.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhkim139.core.ui.theme.WhereTogoTheme
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

@Composable
internal fun RowListPlaceholder(
    count: Int = 3,
    modifier: Modifier = Modifier,
) {
    val nameFractions = listOf(0.66f, 0.52f, 0.74f, 0.58f)
    val metaFractions = listOf(0.40f, 0.33f, 0.46f, 0.36f)
    val shimmer = rememberShimmer(ShimmerBounds.View)

    Column(
        modifier
            .fillMaxWidth()
            .shimmer(shimmer)
            .clearAndSetSemantics { },
    ) {
        repeat(count) { i ->
            RowPlaceholder(
                nameWidthFraction = nameFractions[i % nameFractions.size],
                metaWidthFraction = metaFractions[i % metaFractions.size],
            )
        }
    }
}

@Composable
private fun RowPlaceholder(
    nameWidthFraction: Float,
    metaWidthFraction: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 11.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier
            .size(28.dp)
            .block(RoundedCornerShape(6.dp)))
        Spacer(Modifier.width(10.dp))
        Column(
            Modifier
                .weight(1f)
                .height(37.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier
                .fillMaxWidth(nameWidthFraction)
                .height(13.dp)
                .block())
            Spacer(Modifier.height(7.dp))
            Box(Modifier
                .fillMaxWidth(metaWidthFraction)
                .height(11.dp)
                .block())
        }
        Spacer(Modifier.width(30.dp))
    }
}

@Composable
private fun Modifier.block(shape: Shape = RoundedCornerShape(4.dp)) =
    background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f), shape)

@Preview(name = "Placeholder - Light", showBackground = true, widthDp = 360)
@Composable
private fun RowRowPlaceholderPreview() {
    WhereTogoTheme {
        Surface {
            RowListPlaceholder(count = 3, modifier = Modifier.padding(12.dp))
        }
    }
}