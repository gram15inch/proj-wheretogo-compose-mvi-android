package com.wheretogo.presentation.composable.content

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.ExportMap
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.callMap
import com.wheretogo.presentation.model.AdItem
import com.wheretogo.presentation.state.FloatingButtonState
import com.wheretogo.presentation.theme.Gray100
import com.wheretogo.presentation.theme.hancomSansFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun FloatingButtons(
    modifier: Modifier = Modifier,
    state: FloatingButtonState = FloatingButtonState(),
    course: Course,
    isNotOtherVisible : Boolean,
    onCommentClick: () -> Unit,
    onCheckpointAddClick: () -> Unit,
    onInfoClick: () -> Unit,
    onExportMapClick: () -> Unit,
    onMapAppClick: (Result<Unit>) -> Unit,
    onFoldClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isCommentVisible = state.isCommentVisible && isNotOtherVisible && !state.isBackPlateVisible
    val isCheckpointAddVisible =  state.isCheckpointAddVisible && isNotOtherVisible && !state.isBackPlateVisible
    val isInfoVisible: Boolean = state.isInfoVisible && isNotOtherVisible && !state.isBackPlateVisible
    val isExportVisible: Boolean = state.isExportVisible && isNotOtherVisible
    val isBackPlateVisible: Boolean = state.isBackPlateVisible && state.isExportVisible
    val isFoldVisible: Boolean = state.isFoldVisible && isNotOtherVisible
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        val context = LocalContext.current
        val buttonEndPadding = 12.dp
        SlideAnimation(
            modifier = Modifier
                .fillMaxSize(),
            visible = isBackPlateVisible,
            direction = AnimationDirection.CenterRight  
        ) {
            AdaptiveAd(modifier=Modifier.padding(
                start = buttonEndPadding,
                end = buttonEndPadding,
                top = 10.dp,
                bottom = buttonEndPadding + 10.dp

            ), nativeAd = state.adItemGroup.firstOrNull()?.nativeAd)
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(if (isBackPlateVisible) 0.dp else 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = buttonEndPadding)
        ) {
            SlideAnimation(
                modifier = Modifier,
                visible = isCommentVisible,
                direction = AnimationDirection.RightCenter
            ) {
                CircleButton(
                    Modifier.padding(end = buttonEndPadding),
                    icon = R.drawable.ic_message
                ) {
                    onCommentClick()
                }
            }

            SlideAnimation(
                modifier = Modifier,
                visible = isCheckpointAddVisible,
                direction = AnimationDirection.RightCenter
            ) {
                CircleButton(
                    Modifier.padding(
                        top = if (isBackPlateVisible) 10.dp else 0.dp,
                        end = buttonEndPadding
                    ), icon = R.drawable.ic_location
                ) {
                    onCheckpointAddClick()
                }
            }

            SlideAnimation(
                modifier = Modifier,
                visible = isInfoVisible ,
                direction = AnimationDirection.RightCenter
            ) {
                CircleButton(
                    Modifier.padding(
                        top = if (isBackPlateVisible) 10.dp else 0.dp,
                        end = buttonEndPadding
                    ), icon = R.drawable.ic_info
                ) {
                    onInfoClick()
                }
            }

            SlideAnimation(
                modifier = Modifier,
                visible = isExportVisible,
                direction = AnimationDirection.RightCenter
            ) {
                CirclePlateButton(
                    icon = R.drawable.ic_share,
                    isBackPlate = isBackPlateVisible,
                    buttonEndPadding = buttonEndPadding,
                    onNaverClick = { isLongClick ->
                        scope.launch {
                            onMapAppClick(context.callMap(ExportMap.NAVER, course, !isLongClick))
                        }
                    },
                    onKaKaoClick = { isLongClick ->
                        scope.launch {
                            onMapAppClick(context.callMap(ExportMap.KAKAO, course, !isLongClick))
                        }
                    },
                    onTClick = { isLongClick ->
                        scope.launch {
                            onMapAppClick(context.callMap(ExportMap.SKT, course, !isLongClick))
                        }
                    },
                ) {
                    onExportMapClick()
                }
            }

            SlideAnimation(
                modifier = Modifier.padding(end = buttonEndPadding),
                visible = isFoldVisible,
                direction = AnimationDirection.RightCenter
            ) {
                CircleButton(
                    modifier = Modifier,
                    icon = R.drawable.ic_close
                ) {
                    onFoldClick()
                }
            }

        }
    }
}


@Composable
fun CircleButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    color: Color = colorResource(R.color.white_85),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(60.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = "Icon Description",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun CirclePlateButton(
    modifier: Modifier = Modifier,
    icon: Int,
    isBackPlate: Boolean,
    buttonEndPadding: Dp,
    onNaverClick: (Boolean) -> Unit,
    onTClick: (Boolean) -> Unit,
    onKaKaoClick: (Boolean) -> Unit,
    onExportClick: () -> Unit,
) {
    var targetOffset by remember { mutableStateOf(0.dp) }
    val circleSize = 60.dp
    val squareSize = 52.dp
    val backPlateHeight = 80.dp

    targetOffset = if (isBackPlate) (-13).dp else 0.dp

    var lastClickTime by remember { mutableLongStateOf(0L) }
    val onIntervalClick: ()->Unit = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= 300L) {
            lastClickTime = currentTime
            onExportClick()
        }
    }

    Box(modifier = modifier) {
        if (isBackPlate) {
            //백플레이트
            Backplate(backPlateHeight, buttonEndPadding, squareSize, circleSize,
                onButtonClick = onIntervalClick)

            Column(modifier=Modifier) {
                Row( // 사각형
                    modifier = Modifier
                        .graphicsLayer(
                            translationY = with(LocalDensity.current) { (-15).dp.toPx() },
                            clip = false
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        SquareScaleButton(
                            modifier = Modifier.size(squareSize),
                            icon = R.drawable.lg_k,
                            caption = stringResource(R.string.k_way)
                        ) {
                            onKaKaoClick(it)
                        }

                        SquareScaleButton(
                            modifier = Modifier.size(squareSize),
                            icon = R.drawable.lg_n,
                            caption = stringResource(R.string.n_way)
                        ) {
                            onNaverClick(it)
                        }

                        SquareScaleButton(
                            modifier = Modifier.size(squareSize),
                            icon = R.drawable.lg_t,
                            caption = stringResource(R.string.t_way)
                        ) {
                            onTClick(it)
                        }

                    }
                    Spacer(modifier = Modifier.size(circleSize))
                }
            }
        } else {
            CircleButton(
                modifier=Modifier.padding(end = buttonEndPadding)
                    .align(alignment = Alignment.CenterEnd),icon, onClick = onIntervalClick
            )
        }
    }
}

@Composable
fun InfoText(modifier: Modifier = Modifier, text:String){
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_info),
            contentDescription = "Icon Description",
            modifier = Modifier.size(12.dp),
            alignment = Alignment.Center
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SquareScaleButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    caption: String,
    onClick: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "scaleAnimation"
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .scale(scale)
                .shadow(elevation = 5.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            scope.launch {
                                delay(500)
                                pressed = false
                            }
                        },
                        onTap = {
                            onClick(false)
                        },
                        onLongPress = {
                            onClick(true)
                        }
                    )
                },
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Icon Description",
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = caption,
            fontFamily = hancomSansFontFamily,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun Backplate(backPlateHeight: Dp, buttonEndPadding: Dp, squareSize: Dp, circleSize: Dp, onButtonClick:()->Unit={}) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(40.dp))
            .height(backPlateHeight)
            .background(Color.White)
            .padding(end = buttonEndPadding)
    ) {
        Row {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                Spacer(modifier = Modifier.size(squareSize))
                Spacer(modifier = Modifier.size(squareSize))
                Spacer(modifier = Modifier.size(squareSize))
            }
            Spacer(modifier = Modifier.size(circleSize))
        }
        CircleButton(modifier = Modifier.align(alignment = Alignment.CenterEnd), icon = R.drawable.ic_share, onClick = onButtonClick)
        InfoText(Modifier.padding(start = 30.dp).align(alignment = Alignment.BottomStart),
            text = stringResource(R.string.exclude_my_location_on_long_press))
    }
}

@Preview(name="portrait", widthDp = 400, heightDp = 600)
@Preview(name="landscape", widthDp = 700, heightDp = 400)
@Composable
fun FloatingLandscapeButtonPreview() {
    FloatingButtons(
        modifier = Modifier.background(Gray100),
        state = FloatingButtonState(
            adItemGroup = emptyList(),
            isCommentVisible = true,
            isCheckpointAddVisible = true,
            isExportVisible = true,
            isFoldVisible = true,
            isInfoVisible = true,
            isBackPlateVisible = true,
        ),
        course = Course(),
        isNotOtherVisible = true,
        {}, {}, {}, {}, {}, {}
    )
}
