package com.wheretogo.domain.model.user

data class SignResponse(val status: Status) {
    enum class Status { Success, Fail, Error }
}