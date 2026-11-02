package com.wheretogo.data.model.naver

import kotlinx.serialization.Serializable

@Serializable
data class Result(
    val code: Code,
    val name: String,
    val region: Region,
    val land: Land?,
)