package com.wheretogo.data.model.naver

data class AddressElement(
    val code: String,
    val longName: String,
    val shortName: String,
    val types: List<String>
)