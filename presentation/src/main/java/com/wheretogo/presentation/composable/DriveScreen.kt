package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.presentation.R
import com.wheretogo.presentation.theme.Gray100
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.viewmodel.DriveViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DriveScreen(navController: NavController, viewModel: DriveViewModel = hiltViewModel()) {
    var visible by remember { mutableStateOf(true) }
    val data = viewModel.journeyGroup.collectAsState()
    var camera by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val coroutine = rememberCoroutineScope()
    val listState = rememberLazyListState()
    BackHandler {
        visible = false
        navController.navigateUp()
    }
    AnimatedVisibility(
        visible = visible,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DriveTopBar()
            Box(modifier = Modifier.fillMaxSize()) {
                NaverMapScreen(data,
                    camera = camera,
                    onViewPortChange = { camera, viewport ->
                        viewModel.fetchNearByJourney(camera, viewport)
                    },
                    onMarkerClick = { overlay ->
                        val code = overlay.tag as Int?
                        code?.let {
                            coroutine.launch {
                                val position = data.value.indexOfFirst { it.code == code }
                                listState.animateScrollToItem(position)
                            }
                        }
                    }
                )
                Box(modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                    DriveList(data, listState = listState) { journey ->
                        camera = journey.course.start
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

@Composable
fun DriveList(
    data: State<List<Journey>>,
    listState: LazyListState,
    onItemClick: (Journey) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .height(300.dp)
            .background(Gray100),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState
    ) {

        items(data.value) { item ->
            DriveListItem(Modifier.clickable {
                onItemClick(item)
            }, item)
        }
    }
}

@Composable
fun DriveListItem(modifier: Modifier, item: Journey) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        Row(modifier = modifier.fillMaxWidth()) {
            GlideImage(
                modifier = Modifier.size(80.dp),
                imageModel = { R.drawable.ic_setting },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    modifier =
                    Modifier
                        .wrapContentSize(),
                    text = item.course.waypoints.size.toString(),
                    fontSize = 24.sp
                )
                Text(
                    modifier = Modifier.wrapContentSize(),
                    text = item.code.toString(),
                    fontSize = 24.sp
                )
            }
        }
    }
}
