package com.wheretogo.domain.model

data class UseCaseResponse(val status: Status) {
    enum class Status { Success, Fail, Error }
}