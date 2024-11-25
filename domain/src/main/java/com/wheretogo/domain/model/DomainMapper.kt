package com.wheretogo.domain.model

import com.wheretogo.domain.USER_DATE_FORMAT
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.MarkerTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



fun MarkerTag.toCheckPointTag(url: MarkerTag) = "$code/${id}"
fun MarkerTag.toCourseTag(url: MarkerTag) = "$code"
fun Any.toMarkerTag() = (this as String).split('/').run {
    when (size) {
        1 -> MarkerTag(this[0].toInt())
        2 -> MarkerTag(this[0].toInt(), this[1].toInt())
        else -> MarkerTag()
    }
}

fun getCheckPointMarkerTag(code: Int, checkPoint: CheckPoint) = "$code/${checkPoint.id}"
fun getCourseMarkerTag(code: Int) = "$code"
fun getCoursePathOverlayTag(code: Int) = "$code"


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