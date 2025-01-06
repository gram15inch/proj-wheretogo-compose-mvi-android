package com.wheretogo.domain.model

data class UseCaseResponse(val status: Status, val msg: String = "") {
    enum class Status { Success, Fail, Error }
}