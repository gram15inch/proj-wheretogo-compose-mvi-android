package com.wheretogo.domain.model

import com.wheretogo.domain.UseCaseFailType

data class UseCaseResponse(
    val status: Status,
    val failType: UseCaseFailType? = null,
    val msg: String = ""
) {
    enum class Status { Success, Fail }
}