package com.wheretogo.presentation.composable.content

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skt.Tmap.TMapTapi
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.CallTMap
import com.wheretogo.presentation.feature.callNaverMap
import com.wheretogo.presentation.theme.hancomSansFontFamily

@Preview
@Composable
fun FloatingButtonPreview() {
    FloatingButtons(modifier = Modifier
        .width(300.dp)
        .background(colorResource(R.color.gray_C7C7C7_80)),
        Course(), true, true, true, true, true, true,
        {}, {}, {}, {}, {})
}


@Composable
fun FloatingButtons(
    modifier: Modifier = Modifier,
    course: Course,
    isCommentVisible: Boolean,
    isCheckpointAddVisible: Boolean,
    isExportVisible: Boolean,
    isFoldVisible: Boolean,
    isInfoVisible: Boolean,
    isExportBackPlate: Boolean,
    onCommentClick: () -> Unit,
    onCheckpointAddClick: () -> Unit,
    onInfoClick: () -> Unit,
    onExportMapClick: () -> Unit,
    onFoldClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        val context = LocalContext.current
        val buttonEndPadding = 12.dp
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(if (isExportBackPlate) 0.dp else 10.dp),
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        ) {
            SlideAnimation(
                modifier = Modifier,
                visible = isCommentVisible,
                direction = AnimationDirection.RightCenter
            ) {
                Box(modifier = Modifier.padding(end = buttonEndPadding)) {
                    CircleButton(icon = R.drawable.ic_message) {
                        onCommentClick()
                    }
                }

            }

            SlideAnimation(
                modifier = Modifier,
                visible = isCheckpointAddVisible,
                direction = AnimationDirection.RightCenter
            ) {
                Box(
                    modifier = Modifier.padding(
                        top = if (isExportBackPlate) 10.dp else 0.dp,
                        end = buttonEndPadding
                    )
                ) {
                    CircleButton(icon = R.drawable.ic_location) {
                        onCheckpointAddClick()
                    }
                }

            }

            SlideAnimation(
                modifier = Modifier,
                visible = isInfoVisible,
                direction = AnimationDirection.RightCenter
            ) {
                Box(
                    modifier = Modifier.padding(
                        top = if (isExportBackPlate) 10.dp else 0.dp,
                        end = buttonEndPadding
                    )
                ) {
                    CircleButton(icon = R.drawable.ic_info) {
                        onInfoClick()
                    }
                }

            }

            SlideAnimation(
                modifier = Modifier,
                visible = isExportVisible,
                direction = AnimationDirection.RightCenter
            ) {
                Box(modifier = Modifier.graphicsLayer(clip = false)) {
                    CirclePlateButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        icon = R.drawable.ic_share,
                        isBackPlate = isExportBackPlate && isExportVisible,
                        buttonEndPadding = buttonEndPadding,
                        onNaverClick = {
                            context.callNaverMap(course)
                            onExportMapClick()
                        },
                        onKaKaoClick = {
                            //todo 구현
                            onExportMapClick()
                        },
                        onTClick = {
                            TMapTapi(context).CallTMap(course)
                            onExportMapClick()
                        },
                    ) {
                        onExportMapClick()
                    }
                }
            }

            SlideAnimation(
                modifier = Modifier,
                visible = isFoldVisible,
                direction = AnimationDirection.RightCenter
            ) {

                Box(
                    modifier = Modifier.padding(end = buttonEndPadding)
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
}


@Composable
fun CircleButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    color: Color = colorResource(R.color.blue),
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
    onNaverClick: () -> Unit,
    onTClick: () -> Unit,
    onKaKaoClick: () -> Unit,
    onExportClick: () -> Unit,
) {
    var targetOffset by remember { mutableStateOf(0.dp) }
    val animatedOffset by animateDpAsState(targetValue = targetOffset)
    val circleSize = 60.dp
    val squareSize = 52.dp
    val backPlateHeight = 80.dp


    if (isBackPlate)
        targetOffset = -12.dp
    else
        targetOffset = 0.dp

    if (isBackPlate) {
        Box( //백플레이트
            modifier = Modifier
                .clip(RoundedCornerShape(40.dp))
                .height(backPlateHeight)
                .background(Color.White)
                .padding(end = buttonEndPadding)
        ) {
            Row {
                Row(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Spacer(modifier = Modifier.size(squareSize))
                    Spacer(modifier = Modifier.size(squareSize))
                    Spacer(modifier = Modifier.size(squareSize))
                }
                Spacer(modifier = Modifier.size(circleSize))
            }
        }

        Row( // 사각형
            modifier = Modifier
                .graphicsLayer(
                    translationY = with(LocalDensity.current) { animatedOffset.toPx() },
                    clip = false
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                SquareButton(
                    modifier = Modifier.size(squareSize),
                    icon = R.drawable.lg_kakao,
                    caption = "카카오맵"
                ) {
                    onKaKaoClick()
                }

                SquareButton(
                    modifier = Modifier.size(squareSize),
                    icon = R.drawable.lg_naver,
                    caption = "네이버맵"
                ) {
                    onNaverClick()
                }

                SquareButton(
                    modifier = Modifier.size(squareSize),
                    icon = R.drawable.lg_tmap,
                    caption = "티맵"
                ) {
                    onTClick()
                }

            }

            Spacer(modifier = Modifier.size(circleSize))
        }
    }

    Box( // 원형
        modifier = modifier
            .padding(end = buttonEndPadding)
    ) {
        CircleButton(
            icon = icon
        ) {
            onExportClick()
        }
    }
}

@Composable
fun SquareButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    caption: String,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.shadow(elevation = 5.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(contentColor = color),
            contentPadding = PaddingValues(0.dp)
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