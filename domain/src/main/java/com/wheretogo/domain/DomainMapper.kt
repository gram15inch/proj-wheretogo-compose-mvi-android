package com.wheretogo.domain


import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.model.map.OverlayTag
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

fun parseMarkerTag(stringTag: String): OverlayTag {
    val items = stringTag.split("/")
    return OverlayTag(
        overlayId = items.getOrNull(0) ?: "",
        parentId = items.getOrNull(1) ?: "",
        itemType = items.getOrNull(2)?.toInt() ?: -1,
    )
}

fun OverlayTag.toStringTag() = "${this.overlayId}/${this.parentId}/${this.itemType}"


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