package com.dhkim139.wheretogo.domain.repository

import com.dhkim139.wheretogo.data.model.map.Course
import com.dhkim139.wheretogo.data.model.map.LocalMap

interface MapRepository {
    suspend fun getMaps(): List<LocalMap>
    suspend fun getMap(course: Course): LocalMap
    suspend fun setMap(map: LocalMap)
}