package com.wheretogo.presentation.composable

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naver.maps.map.util.FusedLocationSource
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.presentation.R
import com.wheretogo.presentation.c1
import com.wheretogo.presentation.c2
import com.wheretogo.presentation.c3
import com.wheretogo.presentation.c4
import com.wheretogo.presentation.c5
import com.wheretogo.presentation.c6
import com.wheretogo.presentation.c7
import com.wheretogo.presentation.theme.Gray100
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.viewmodel.DriveViewModel
import kotlinx.coroutines.Dispatchers

@Composable
fun DriveContent(navController: NavController, viewModel: DriveViewModel = hiltViewModel()) {
    var visible by remember { mutableStateOf(true) }
    val data = viewModel.journeyGroup.collectAsState()
    LaunchedEffect(Dispatchers.IO) {
   /*     if (viewModel.journeyGroup.value.isEmpty())
            listOf(c1, c2, c3, c4, c5, c6, c7).forEach { viewModel.refreshJourney(it)}*/

        if(viewModel.journeyGroup.value.isEmpty())
            viewModel.fetchNearByJourney(LatLng(c5.start.latitude,c5.start.longitude),5000)

    }
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
                NaverScreen(data)
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



