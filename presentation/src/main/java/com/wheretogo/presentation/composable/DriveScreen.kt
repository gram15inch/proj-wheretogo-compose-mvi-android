package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.toMarkerTag
import com.wheretogo.presentation.R
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
    val listState = rememberLazyListState()
    BackHandler {
        navController.navigateUp()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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


        FadeAnimation(
            modifier = Modifier.align(alignment = Alignment.BottomCenter),
            visible = state.listState.isVisible
        ) {
            DriveList(data = state.listState.listData,
                listState = listState,
                onItemClick = { selectedItem ->
                    viewModel.handleIntent(DriveScreenIntent.ListItemClick(selectedItem))
                }
            )
        }

        FadeAnimation(
            modifier = Modifier.fillMaxSize(),
            visible = state.popUpState.isVisible
        ) {
            PopUpImage(
                onClick = {
                    viewModel.handleIntent(DriveScreenIntent.PopUpClick)
                },
                url = state.popUpState.url
            )
        }

        SlideAnimation(
            modifier = Modifier
                .padding(12.dp)
                .align(alignment = Alignment.BottomEnd),
            visible = state.floatingButtonState.isVisible
        ) {
            CircularButton(onClick = {
                viewModel.handleIntent(DriveScreenIntent.FloatingButtonClick)
            })
        }
    }

}

@Composable
fun CircularButton(onClick: () -> Unit, color: Color = Color.Blue) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(contentColor = color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_setting),
            contentDescription = "Icon Description",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun PopUpImage(onClick: () -> Unit, url: String) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    Box(
        Modifier
            .background(color = colorResource(R.color.gray_50))
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onClick()
            }
    ) {
        GlideImage(modifier = Modifier
            .align(Alignment.BottomStart)
            .widthIn(max = 250.dp)
            .heightIn(max = 500.dp)
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp)), imageModel = { url })
    }
}