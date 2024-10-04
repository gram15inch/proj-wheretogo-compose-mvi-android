package com.dhkim139.wheretogo.data.repository

import android.util.Log
import com.dhkim139.wheretogo.BuildConfig
import com.dhkim139.wheretogo.data.datasource.database.MapDatabase
import com.dhkim139.wheretogo.data.datasource.service.NaverMapApiService
import com.dhkim139.wheretogo.data.model.map.Course
import com.dhkim139.wheretogo.data.model.map.LocalMap
import com.dhkim139.wheretogo.domain.repository.MapRepository
import com.naver.maps.geometry.LatLng
import com.dhkim139.wheretogo.domain.model.LatLng as LocalLatLng
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(private val mapApiService: NaverMapApiService,private val mapDatabase: MapDatabase) :
    MapRepository {
    override suspend fun getMaps(): List<LocalMap> {
        return mapDatabase.mapDao().selectAll()
    }

    override suspend fun getMap(course: Course): LocalMap {
        return mapDatabase.mapDao().select(course.code).run {
            this ?: return LocalMap(
                    code = course.code,
                    course = course,
                    points = getPoints(course).map { LocalLatLng(it.latitude, it.longitude) }
                ).apply {
                    mapDatabase.mapDao().insert(this)
                }
        }
    }

    override suspend fun setMap(map: LocalMap) {
        mapDatabase.mapDao().insert(map)
    }

   private suspend fun getPoints(course: Course):List<LatLng>{
        val msg=mapApiService.getRouteWayPoint(
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

    private fun convertLatLng(latlng:LocalLatLng):String =  "${latlng.longitude}, ${latlng.latitude}"
    private fun convertWaypoints(waypoints:List<LocalLatLng>):String {
        var str = ""
        waypoints.forEach {
            str+= convertLatLng(it)+"|"
        }
        return str
    }
}