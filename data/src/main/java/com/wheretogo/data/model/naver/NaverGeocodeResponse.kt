package com.wheretogo.data.model.naver

data class NaverGeocodeResponse(
    val addresses: List<Addresse>,
    val errorMessage: String,
    val meta: Meta,
    val status: String
)