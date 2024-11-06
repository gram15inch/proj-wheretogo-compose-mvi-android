package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.DriveList
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.feature.naver.rotateCamera
import com.wheretogo.presentation.feature.naver.setCamera
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.toDomainLatLng
import com.wheretogo.presentation.theme.Gray100
import com.wheretogo.presentation.theme.hancomMalangFontFamily
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
            onMarkerClick = { overlay ->
                val marker = overlay as Marker
                naverMap?.setCamera(marker.position.toDomainLatLng(), 11.0)
                viewModel.handleIntent(DriveScreenIntent.MarkerClick(marker.tag as Int))
            }
        )

        if (state.listState.isVisible)
            Box(modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                DriveList(data = state.listState.listData,
                    listState = listState,
                    onItemClick = { selectedItem ->
                        naverMap?.rotateCamera(selectedItem.course.start)
                        viewModel.handleIntent(DriveScreenIntent.ListItemClick(selectedItem))
                    }
                )
            }
        }
    }
}

@Composable
fun DriveTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.where_to_go),
            fontSize = 24.sp,
            fontFamily = hancomMalangFontFamily,
            color = Gray100
        )
    }
}


@Preview
@Composable
fun DriveTopBarPreivew() {
    DriveTopBar()
}
