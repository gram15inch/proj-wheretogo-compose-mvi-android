package com.dhkim139.wheretogo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dhkim139.wheretogo.BuildConfig
import com.dhkim139.wheretogo.data.datasource.service.NaverMapApiService
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(private val naverMapApiService : NaverMapApiService) :ViewModel() {

    suspend fun callApi():List<LatLng>{
        val msg=naverMapApiService.getRouteWayPoint(
            BuildConfig.NAVER_CLIENT_ID_KEY,
            BuildConfig.NAVER_CLIENT_SECRET_KEY,
            start = convertLatLng("37.24049254419747, 127.10069878544695"),
           // goal = convertLatLng("37.279593663738545, 127.11749212526078"),
            goal = convertLatLng("37.24022338235744, 127.10061868739378"),
            waypoints =
            convertLatLng("37.22248268378388, 127.09011137932174")
        )


        if(msg.body()?.currentDateTime!=null){
            val r= msg.body()!!.route.traoptimal.map { it.path }.first().map { LatLng(it[1],it[0]) }
            Log.d("tst","size: ${r.size}\n")
            return r
        }else{
            Log.d("tst","${msg}")
            Log.d("tst","${msg.body()}")
            return  listOf()
        }
    }


    private fun convertLatLng(str:String):String=str.split(",").run { "${this[1]}, ${this[0]}" }
}