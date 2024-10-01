package com.dhkim139.wheretogo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.dhkim139.wheretogo.ui.composable.DriveContent
import com.dhkim139.wheretogo.ui.composable.HomeContent
import com.dhkim139.wheretogo.ui.theme.WhereTogoTheme
import com.dhkim139.wheretogo.ui.theme.White100
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhereTogoTheme {
                Box(
                    modifier = Modifier
                        .background(Color.Gray)
                        .fillMaxSize().clickable {
                            startNaverMap()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(White100)
                            .padding(16.dp)
                    ) {
                        val displayMaxWidth = min(400.dp, maxWidth)
                        var contentIdx by remember { mutableIntStateOf(1) }
                        when (contentIdx) {
                            0 -> HomeContent(displayMaxWidth) { contentIdx = 1 }
                            1 -> DriveContent(displayMaxWidth)
                        }
                    }
                }
            }
        }
    }
    fun ComponentActivity.startNaverMap(start:LatLng= LatLng(37.24049254419747, 127.10069878544695),end:LatLng= LatLng(37.24022338235744, 127.10061868739378),waypoint:LatLng= LatLng(37.22248268378388, 127.09011137932174)){
        // val url = "nmap://actionPath?parameter=value&appname={wheretogo}"
        //val url = "nmap://route/car?slat=${start.latitude}&slng=${start.longitude}&sname=start&dlat=${end.latitude}&dlng=${end.longitude}&dname=end&appname=com.dhkim139.wheretogo"
        val url = "nmap://route/car?slat=${start.latitude}&slng=${start.longitude}&sname=start" +
                "&dlat=${end.latitude}&dlng=${end.longitude}&dname=end" +
                "&v1lat=${waypoint.latitude}&v1lng=${waypoint.longitude}&v1name=v1" +
                "&appname=com.dhkim139.wheretogo"
        //val url="nmap://map?lat=37.4979502&lng=127.0276368&zoom=20&appname=com.dhkim139.wheretogo"
        //val url="nmap://map?&com.dhkim139.wheretogo"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}



