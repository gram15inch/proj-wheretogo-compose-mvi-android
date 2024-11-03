package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.model.CheckPoint
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.DriveList
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.feature.naver.HideOverlayMap
import com.wheretogo.presentation.feature.naver.hideOverlayWithoutItem
import com.wheretogo.presentation.feature.naver.rotateCamera
import com.wheretogo.presentation.feature.naver.setCamera
import com.wheretogo.presentation.model.toDomainLatLng
import com.wheretogo.presentation.theme.Gray100
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.viewmodel.DriveViewModel
import kotlinx.coroutines.launch

@Composable
fun DriveScreen(navController: NavController, viewModel: DriveViewModel = hiltViewModel()) {
    val journeyGroup by viewModel.journeyGroup.collectAsState()
    val visibleOverlayMap by viewModel.visibleOverlayGroup.collectAsState()
    val isResetOverlay by viewModel.isRefreshOverlay.collectAsState()
    val hiddenOverlayMap by remember { mutableStateOf(HideOverlayMap()) }
    var hiddenCheckPointOverlay by remember { mutableStateOf(emptyList<CheckPoint>()) }

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var camera by remember { mutableStateOf(LatLng()) }
    var viewport by remember { mutableStateOf(Viewport()) }
    var location by remember { mutableStateOf(LatLng()) }

    val coroutine = rememberCoroutineScope()
    val listState = rememberLazyListState()
    BackHandler {
        navController.navigateUp()
    }
    if (isResetOverlay) {
        hiddenOverlayMap.clear()
        viewModel.resetJourneyGroup()
        hiddenCheckPointOverlay.forEach {
            viewModel.hideCheckPointOverlay(it)
        }
        hiddenCheckPointOverlay = emptyList()
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DriveTopBar()
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.zIndex(1f)) {
                Text("${visibleOverlayMap.size}", fontSize = 50.sp)
                Text("${hiddenOverlayMap.size}", fontSize = 50.sp)
            }
            NaverMap(
                modifier = Modifier.zIndex(0f),
                overlayMap = visibleOverlayMap,
                onMapAsync = { map ->
                    naverMap = map
                },
                onLocationMove = { latLng ->
                    location = latLng
                },
                onCameraMove = { latLng, vp ->
                    camera = latLng
                    viewport = vp
                    viewModel.fetchNearByJourneyInMap(latLng, viewport)
                    viewModel.fetchNearByJourneyInList(latLng)
                },
                onMarkerClick = { overlay ->
                    coroutine.launch {
                        val marker = overlay as Marker
                        naverMap?.setCamera(marker.position.toDomainLatLng(), 11.0)
                        hiddenOverlayMap.hideOverlayWithoutItem(marker.tag as Int, visibleOverlayMap)
                        viewModel.hideJourneyWithoutItem(marker.tag as Int)
                    }
                }
            )
            Box(modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                DriveList(data = journeyGroup,
                    listState = listState,
                    onItemClick = { selectedItem ->
                        naverMap?.rotateCamera(selectedItem.course.start)
                        hiddenOverlayMap.hideOverlayWithoutItem(selectedItem.code, visibleOverlayMap)
                        viewModel.hideJourneyWithoutItem(selectedItem.code)

                        if(hiddenCheckPointOverlay.isEmpty()){
                            for(item in selectedItem.checkPoints)
                                viewModel.showCheckPointOverlay(item)
                            hiddenCheckPointOverlay = selectedItem.checkPoints
                        }else{
                            hiddenCheckPointOverlay.forEach {
                                viewModel.hideCheckPointOverlay(it)
                            }
                            hiddenCheckPointOverlay = emptyList()
                        }

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
