package com.wheretogo.presentation.composable

import android.util.Log
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.DriveList
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.feature.naver.HideOverlayMap
import com.wheretogo.presentation.feature.naver.rotateCamera
import com.wheretogo.presentation.feature.naver.setCamera
import com.wheretogo.presentation.model.toDomainLatLng
import com.wheretogo.presentation.theme.Gray100
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.viewmodel.DriveViewModel
import kotlinx.coroutines.launch

@Composable
fun DriveScreen(navController: NavController, viewModel: DriveViewModel = hiltViewModel()) {
    val journeyInMap by viewModel.journeyGroupInMap.collectAsState()
    val journeyInList by viewModel.journeyGroupInList.collectAsState()
    val journeyInOverlay by viewModel.journeyGroupInOverlay.collectAsState()
    val overlayWhenHide by remember { mutableStateOf(HideOverlayMap(null)) }
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var camera by remember { mutableStateOf(LatLng()) }
    var viewport by remember { mutableStateOf(Viewport()) }
    var location by remember { mutableStateOf(LatLng()) }

    val coroutine = rememberCoroutineScope()
    val listState = rememberLazyListState()
    BackHandler {
        navController.navigateUp()
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DriveTopBar()
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Text("${journeyInMap.size}", fontSize = 50.sp)
                Text("${journeyInList.size}", fontSize = 50.sp)
            }
            NaverMap(
                data = journeyInOverlay,
                onMapAsync = { map ->
                    naverMap = map
                    overlayWhenHide.naverMap=map
                },
                onLocationMove = { latLng ->
                    location = latLng
                },
                onCameraMove = { latLng ->
                    Log.d("tst2", "onCameraMove ${latLng}")
                    camera = latLng
                    viewModel.fetchNearByJourneyInList(latLng)
                    viewModel.fetchNearByJourneyInMap(latLng, viewport)
                },
                onViewportChange = { _, vp ->
                    viewport = vp
                },
                onMarkerClick = { overlay ->
                    coroutine.launch {
                        naverMap?.setCamera((overlay as Marker).position.toDomainLatLng(), 13.0)
                    }
                }
            )
            Box(modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                DriveList(journeyInList, listState = listState) { journey ->
                    naverMap?.rotateCamera(journey.course.start)

                    if(overlayWhenHide.isEmpty()) {
                        for (list in journeyInList) {
                            if (list.code != journey.code) {
                                journeyInOverlay[list.code]?.apply {
                                    overlayWhenHide[this.code] = this
                                }
                            }
                        }
                    }else{
                        overlayWhenHide.clear()
                    }
                }
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

private fun hideOverlayWithout(){

}