package com.wheretogo.domain.model

import com.wheretogo.domain.UseCaseFailType

data class UseCaseResponse<T>(
    val status: Status,
    val data: T? = null,
    val failType: UseCaseFailType? = null,
    val log: String = "",
) {
    enum class Status { Success, Fail }
}