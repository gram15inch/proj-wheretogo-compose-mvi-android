package com.wheretogo.domain.model

import com.wheretogo.domain.model.map.CheckPoint


const val COURSE_MIN = 1000
const val CHECKPOINT_MIN = 0


data class MarkerTag(val code: Int = -1, val id: Int = -1)


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
