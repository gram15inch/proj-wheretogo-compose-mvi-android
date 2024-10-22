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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.NaverMap
import com.wheretogo.domain.model.LatLng
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.DriveList
import com.wheretogo.presentation.composable.content.NaverMap
import com.wheretogo.presentation.feature.setCamera
import com.wheretogo.presentation.theme.Gray100
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.viewmodel.DriveViewModel
import kotlinx.coroutines.launch

@Composable
fun DriveScreen(navController: NavController, viewModel: DriveViewModel = hiltViewModel()) {
    var visible by remember { mutableStateOf(true) }
    val journeyInMap = viewModel.journeyGroupInMap.collectAsState()
    val journeyInList = viewModel.journeyGroupInList.collectAsState()
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    var camera by remember { mutableStateOf(LatLng()) }
    val coroutine = rememberCoroutineScope()
    val listState = rememberLazyListState()
    BackHandler {
        visible = false
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
                Text("${journeyInMap.value.size}", fontSize = 50.sp)
                Text("${journeyInList.value.size}", fontSize = 50.sp)
            }
            NaverMap(
                data = journeyInMap,
                onMapAsync = { map ->
                    naverMap = map
                },
                onLocationMove = { latLng ->

                },
                onCameraMove = { latLng ->
                    viewModel.fetchNearByJourneyInList(latLng)
                },
                onViewportChange = { camera, viewport ->
                    viewModel.fetchNearByJourneyInMap(camera, viewport)
                },
                onMarkerClick = { overlay ->
                    val code = overlay.tag
                    code?.let {
                        coroutine.launch {
                            val position = journeyInMap.value.first() { it.code == code }
                            naverMap!!.setCamera(position.course.start, 14.0)
                        }
                    }
                }
            )
            Box(modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                DriveList(journeyInList, listState = listState) { journey ->
                    camera = journey.course.start
                    naverMap?.apply {
                        if (cameraPosition.bearing != 0.0 || cameraPosition.tilt != 0.0)
                            setCamera(journey.course.start, 14.0, 0.0, 0.0)
                        else
                            setCamera(journey.course.start, 14.0, 90.0, -25.0)
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
fun DriveTopBarPreivew(){
    DriveTopBar()
}


