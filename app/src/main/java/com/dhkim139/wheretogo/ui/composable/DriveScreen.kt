package com.dhkim139.wheretogo.ui.composable

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapType
import com.kakao.vectormap.MapView
import com.kakao.vectormap.MapViewInfo
import java.lang.Exception

@Composable
fun DriveContent(displayMaxWidth: Dp, click: () -> Unit) {

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .clickable { click.invoke() },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopBar(displayMaxWidth)
        MyCustomView()
        BottomBar(displayMaxWidth)
    }
}

@Composable
fun MyCustomView() {
    AndroidView(modifier = Modifier.width(400.dp).height(400.dp),factory = { context ->
        MapView(context).apply {
            this.start(
                object : MapLifeCycleCallback() {

                    override fun onMapDestroy() {
                        Log.d("tst2","onMapDestroy")
                    }

                    override fun onMapError(p0: Exception?) {
                        Log.d("tst2","onMapError$p0")
                    }
                },
                object : KakaoMapReadyCallback(){
                    override fun onMapReady(p0: KakaoMap) {
                        Log.d("tst2","onMapReady: $p0")
                    }

                    override fun getPosition(): LatLng {
                        return LatLng.from(37.406960, 127.115587)
                    }

                    override fun getZoomLevel(): Int {
                        return 15
                    }

                    override fun getMapViewInfo(): MapViewInfo {
                        return MapViewInfo.from(MapType.NORMAL.name)
                    }

                    override fun getViewName(): String {
                        return "Wheretogo"
                    }

                    override fun isVisible(): Boolean {
                        return true
                    }

                    override fun getTag(): Any {
                        return "wheretogo tag"
                    }
                }
            )
        }
    })
}