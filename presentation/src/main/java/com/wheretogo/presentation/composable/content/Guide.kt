package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.animation.highlightRoundedCorner
import com.wheretogo.presentation.state.GuideState
import com.wheretogo.presentation.theme.Gray50
import com.wheretogo.presentation.theme.Gray5050
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.toStrRes

@Composable
fun GuidePopup(
    modifier: Modifier = Modifier,
    state: GuideState,
    onClick: (DriveTutorialStep) -> Unit = {}
) {
    val step = state.tutorialStep
    val infos by remember { mutableStateOf(DriveTutorialStep.entries.filter { it.tag == "info" }) }
    val pagerState = rememberPagerState { infos.size }
    var infoText by remember { mutableIntStateOf(R.string.blank) }
    var infoPage by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(step) {
        val res = step.toStrRes()
        when {
            step == DriveTutorialStep.SKIP -> {
                isVisible = false
            }

            res != null -> {
                isVisible = true
                infoText = res
                infoPage = "${infos.indexOf(step) + 1}/${infos.size}"
                pagerState.animateScrollToPage(step.ordinal)
            }
        }
    }

    SlideAnimation(
        modifier = modifier,
        visible = isVisible,
        direction = AnimationDirection.CenterRight
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .sizeIn(maxWidth = 220.dp)
                .highlightRoundedCorner(state.isHighlight, width = 4.dp, 14.dp)
                .shadow(elevation = 5.dp, shape = RoundedCornerShape(14.dp))
                .background(Gray50)
                .run {
                    if (step == DriveTutorialStep.DRIVE_GUIDE_DONE)
                        clickable { onClick(step) }
                    else
                        this
                }
        ) {
            if (step == DriveTutorialStep.DRIVE_GUIDE_DONE)
                Image(
                    modifier = Modifier
                        .zIndex(1f)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Gray5050)
                        .size(25.dp)
                        .padding(5.dp),
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "x"
                )
            Column(horizontalAlignment = Alignment.End) {
                Row(modifier = Modifier.padding(top = 8.dp, start = 10.dp, end = 10.dp)) {
                    HorizontalPager(
                        modifier = Modifier.sizeIn(minHeight = 40.dp),
                        state = pagerState,
                        userScrollEnabled = false
                    ) { page ->
                        GuideText(text = stringResource(infoText))
                    }
                }
                Text(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 2.dp, end = 5.dp),
                    text = infoPage,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontFamily = hancomSansFontFamily
                )
            }
        }
    }

}

@Composable
fun GuideText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        fontFamily = hancomMalangFontFamily
    )
}

@Preview(widthDp = 400, heightDp = 400)
@Composable
fun GuidePopupPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        GuidePopup(
            state = GuideState(
                tutorialStep = DriveTutorialStep.DRIVE_LIST_ITEM_CLICK
            )
        )
    }
}