package com.dhkim139.wheretogo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dhkim139.wheretogo.BuildConfig
import com.dhkim139.wheretogo.data.datasource.service.NaverMapApiService
import com.dhkim139.wheretogo.data.model.map.Course
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(private val naverMapApiService : NaverMapApiService) :ViewModel() {

    suspend fun callApi(course: Course):List<LatLng>{
        val msg=naverMapApiService.getRouteWayPoint(
            BuildConfig.NAVER_CLIENT_ID_KEY,
            BuildConfig.NAVER_CLIENT_SECRET_KEY,
            start = convertLatLng(course.start),
            goal = convertLatLng(course.goal),
            waypoints = convertWaypoints(course.waypoints)
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


    private fun convertLatLng(latlng:com.dhkim139.wheretogo.domain.model.LatLng):String =  "${latlng.longitude}, ${latlng.latitude}"
    private fun convertWaypoints(waypoints:List<com.dhkim139.wheretogo.domain.model.LatLng>):String {
        var str = ""
        waypoints.forEach {
            str+= convertLatLng(it)+"|"
        }
        return str
    }




}