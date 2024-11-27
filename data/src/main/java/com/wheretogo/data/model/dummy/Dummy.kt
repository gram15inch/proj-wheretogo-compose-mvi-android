package com.wheretogo.data.model.dummy

import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.LatLng

val checkpoint1006 = listOf(
    CheckPoint(id = 101, latLng = LatLng(latitude = 37.2763159, longitude = 127.115934)),
    CheckPoint(id = 102, latLng = LatLng(latitude = 37.2852041, longitude = 127.1056534)),
    CheckPoint(id = 103, latLng = LatLng(latitude = 37.2733587, longitude = 127.1056238)),
    CheckPoint(id = 104, latLng = LatLng(latitude = 37.2753486, longitude = 127.1139649)),
)

val checkpoint1001 = listOf(
    CheckPoint(id = 105, latLng = LatLng(latitude = 37.2398789, longitude = 127.1010123)),
    CheckPoint(id = 106, latLng = LatLng(latitude = 37.2599458, longitude = 127.0906937)),
    CheckPoint(id = 107, latLng = LatLng(latitude = 37.2345334, longitude = 127.1017744)),
    CheckPoint(id = 108, latLng = LatLng(latitude = 37.2372852, longitude = 127.1019662)),
)


val checkpoint1002 = listOf(
    CheckPoint(id = 109, latLng = LatLng(latitude = 37.2738242, longitude = 127.0657947)),
    CheckPoint(id = 110, latLng = LatLng(latitude = 37.2734441, longitude = 127.0595312)),
    CheckPoint(id = 111, latLng = LatLng(latitude = 37.2850941, longitude = 127.0562707)),
    CheckPoint(id = 112, latLng = LatLng(latitude = 37.2806484, longitude = 127.0689434)),
)

fun getCheckpointDummy(code: Int): List<CheckPoint> {
    return when (code) {
        1001 -> checkpoint1001
        1002 -> checkpoint1002
        1006 -> checkpoint1006
        else -> emptyList()
    }
}