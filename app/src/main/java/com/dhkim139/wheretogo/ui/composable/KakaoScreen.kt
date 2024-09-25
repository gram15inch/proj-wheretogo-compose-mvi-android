package com.dhkim139.wheretogo.ui.composable

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dhkim139.wheretogo.R
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.internal.RenderViewController
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.route.RouteLineStylesSet

@Composable
fun KakaoScreen(setRoute: (MapView) -> Unit) {
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KakaoMapView(setRoute)
    }

}

@Composable
fun KakaoMapView(setRoute: (MapView) -> Unit) {
    AndroidView(modifier = Modifier
        .height(400.dp), factory = { context ->
        MapView(context).apply {
            this.start(
                object : MapLifeCycleCallback() {

                    override fun onMapDestroy() {
                        Log.d("tst2", "onMapDestroy")
                    }

                    override fun onMapError(p0: Exception?) {
                        Log.d("tst2", "onMapError$p0")
                    }
                },
                object : KakaoMapReadyCallback() {
                    override fun onMapReady(kakaoMap: KakaoMap) {
                        setRouteLine(kakaoMap)
                    }

                    override fun getPosition(): LatLng {
                        return LatLng.from(37.24029064395548, 127.10127100051095)
                    }

                    override fun getZoomLevel(): Int {
                        return 15
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

            setRoute.invoke(this)
        }
    })
}

fun setRouteLine(map: KakaoMap) {
    val layer = map.routeLineManager!!.layer
    val styles1 = RouteLineStyles.from(RouteLineStyle.from(16f, R.color.black))

    val stylesSet = RouteLineStylesSet.from(styles1)

// RouteLineSegment 생성
    val segment = RouteLineSegment.from(
        listOf(
            LatLng.from(37.24029064395548, 127.10127100051095),
            LatLng.from(37.221355259513686, 127.09719910417634),
            LatLng.from(37.2225901972304, 127.0901178706114),
            LatLng.from(37.255513946627794, 127.09415649509319),
            LatLng.from(37.24029064395548, 127.10127100051095),
        )
    ).setStyles(stylesSet.getStyles(0))
    val wtm= RenderViewController.toLatLngFromWTM(14151644.5670411,4476884.7883667)
    val wcong= RenderViewController.toLatLngFromWCong(14151830.4260629,4477364.0640943)
    Log.d("tst2","wtm: $wtm\n")
    Log.d("tst2","Wcong: $wcong")
// RouteLineOptions 생성
    val options = RouteLineOptions.from(segment).setStylesSet(stylesSet)

// RouteLineLayer에 RouteLine 추가
    val routeLine = layer.addRouteLine(options)
    map.moveCamera(
        CameraUpdateFactory.newCenterPosition(
            LatLng.from(37.24029064395548, 127.10127100051095), 13
        )
    )
}