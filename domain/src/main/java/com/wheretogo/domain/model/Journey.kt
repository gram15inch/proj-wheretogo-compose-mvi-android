package com.wheretogo.domain.model

data class Journey(
    val code: Int = 0,
    var course: Course,
    var points: List<LatLng>,
){
    companion object{
        fun empty()=Journey(0,Course.empty(), emptyList())
    }
}
