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
            start = convertLatLng("37.24029064395548, 127.10127100051095"),
            goal = convertLatLng("37.255513946627794, 127.09415649509319"),
            waypoint =
            convertLatLng("37.221355259513686, 127.09719910417634")+"|"
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