package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.Comment
import com.wheretogo.domain.model.toMarkerTag
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.DriveList
import com.wheretogo.presentation.composable.content.FadeAnimation
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.viewmodel.DriveViewModel

@Composable
fun DriveScreen(navController: NavController, viewModel: DriveViewModel = hiltViewModel()) {
    val state by viewModel.driveScreenState.collectAsState()
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    BackHandler {
        navController.navigateUp()
    }

    Column(modifier = Modifier.zIndex(1f)) {
        Text("${state.mapState.mapData.size}", fontSize = 50.sp)
    }
    NaverMap(
        modifier = Modifier.zIndex(0f),
        overlayMap = state.mapState.mapData,
        onMapAsync = { map ->
            naverMap = map
            viewModel.handleIntent(DriveScreenIntent.MapIsReady)
        },
        onLocationMove = { latLng ->
            viewModel.handleIntent(DriveScreenIntent.UpdateLocation(latLng))
        },
        onCameraMove = { latLng, vp ->
            viewModel.handleIntent(DriveScreenIntent.UpdateCamera(latLng, vp))
        },
        onCourseMarkerClick = { overlay ->
            val marker = overlay as Marker
            viewModel.handleIntent(DriveScreenIntent.CourseMarkerClick(marker.tag!!.toMarkerTag()))
        },
        onCheckPointMarkerClick = { overlay ->
            val marker = overlay as Marker
            viewModel.handleIntent(DriveScreenIntent.CheckPointMarkerClick(marker.tag!!.toMarkerTag()))
        }
    )

    FadeAnimation(modifier = Modifier, visible = state.popUpState.isVisible) {
        BlurEffect(onClick = {
            viewModel.handleIntent(DriveScreenIntent.PopUpClick)
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        FadeAnimation(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            visible = state.listState.isVisible
        ) {
            OneHandArea {
                DriveList(
                    data = state.listState.listData,
                    onItemClick = { selectedItem ->
                        viewModel.handleIntent(DriveScreenIntent.ListItemClick(selectedItem))
                    }
                )
            }
        }

        FadeAnimation(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            visible = state.popUpState.isVisible
        ) {
            OneHandArea {
                ExtendArea(
                    holdContent = {
                        PopUpImage(
                            modifier = Modifier.align(alignment = Alignment.BottomStart),
                            onClick = {
                                viewModel.handleIntent(DriveScreenIntent.PopUpClick)
                            },
                            url = state.popUpState.imageUrl
                        )
                    },
                    moveContent = {
                        SlideAnimation(
                            modifier = Modifier.graphicsLayer(clip = true),
                            visible = state.popUpState.isCommentVisible,
                            direction = if (screenSize(true) > 650.dp) AnimationDirection.CenterRight else AnimationDirection.CenterDown
                        ) {
                            PopUpComment(
                                modifier = Modifier.align(alignment = Alignment.BottomStart),
                                onClick = {

                                },
                                state.popUpState.comment.data
                            )
                        }
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SlideAnimation(
                modifier = Modifier,
                visible = state.floatingButtonState.isCommentVisible,
                direction = AnimationDirection.RightCenter
            ) {
                CircularButton(icon = R.drawable.ic_comment,
                    onClick = {
                        viewModel.handleIntent(DriveScreenIntent.CommentFloatingButtonClick)
                    })
            }

            SlideAnimation(
                modifier = Modifier,
                visible = state.floatingButtonState.isFoldVisible,
                direction = AnimationDirection.RightCenter
            ) {
                CircularButton(icon = R.drawable.ic_fold_up,
                    onClick = {
                        viewModel.handleIntent(DriveScreenIntent.FoldFloatingButtonClick)
                    })
            }
        }
    }

}


@Composable
fun CircularButton(
    @DrawableRes icon: Int,
    color: Color = Color.Blue,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(contentColor = color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = "Icon Description",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun PopUpImage(modifier: Modifier, onClick: () -> Unit, url: String) {
    GlideImage(modifier = modifier
        .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp)
        .clip(RoundedCornerShape(16.dp)), imageModel = { url })
}

@Composable
fun PopUpComment(modifier: Modifier, onClick: () -> Unit, data: List<Comment>) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    Box(
        modifier = modifier
            .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.teal_200))
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onClick()
            }
    )
}

@Composable
fun OneHandArea(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .width(650.dp)
    ) {
        content()
    }
}

@Composable
fun BlurEffect(onClick: () -> Unit) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }

    Box(
        Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.gray_50))
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onClick()
            }
    ) {

    }
}

@Composable
fun ExtendArea(holdContent: @Composable () -> Unit, moveContent: @Composable () -> Unit) {
    if (screenSize(true) >= 650.dp) {
        Row() {
            holdContent()
            moveContent()
        }
    } else {
        Box() {
            holdContent()
            moveContent()
        }
    }

}

@Composable
fun screenSize(isWidth: Boolean): Dp {
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    return if (isWidth) screenWidthDp.dp else screenHeightDp.dp
}