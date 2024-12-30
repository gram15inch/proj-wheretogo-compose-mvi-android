package com.wheretogo.data.model.naver

data class Result(
    val code: Code,
    val name: String,
    val region: Region,
    val land: Land?,
)