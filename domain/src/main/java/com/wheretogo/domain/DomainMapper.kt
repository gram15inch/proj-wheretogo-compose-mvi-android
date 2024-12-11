package com.wheretogo.domain


import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MarkerTag
import com.wheretogo.domain.model.map.MetaCheckPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun LatLng.toGeoHash(length: Int): String {
    return GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude), length)
}

fun LatLng.toGeoHashBound(radius: Double): Pair<String, String> {
    return GeoFireUtils.getGeoHashQueryBounds(GeoLocation(latitude, longitude), radius).first()
        .run { Pair(startHash, endHash) }
}

fun MarkerTag.toCheckPointTag(url: MarkerTag) = "$code/${id}"
fun MarkerTag.toCourseTag(url: MarkerTag) = "$code"
fun Any.toMarkerTag() = (this as String).split('/').run {
    when (size) {
        1 -> MarkerTag(this[0].toInt())
        2 -> MarkerTag(this[0].toInt(), this[1].toInt())
        else -> MarkerTag()
    }
}

fun getCheckPointMarkerTag(checkPointId: String) = checkPointId.hashCode()
fun getCourseMarkerTag(code: Int) = "$code"
fun getCoursePathOverlayTag(code: Int) = "$code"
fun getCourseMarkerTag(code: String) = code.hashCode()
fun getCoursePathOverlayTag(code: String) = code.hashCode()


fun formatMillisToDate(millis: Long, pattern: String = USER_DATE_FORMAT): String {
    val date = Date(millis)
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return dateFormat.format(date)
}

fun parseDateToMillis(dateString: String, pattern: String = USER_DATE_FORMAT): Long {
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    val date = dateFormat.parse(dateString)
    return date?.time ?: throw IllegalArgumentException("Invalid date format or value")
}

fun List<CheckPoint>.toMetaCheckPoint(
    timestamp: Long = 0L
): MetaCheckPoint {
    return MetaCheckPoint(
        checkPointIdGroup = map { it.checkPointId },
        timeStamp = timestamp
    )
}