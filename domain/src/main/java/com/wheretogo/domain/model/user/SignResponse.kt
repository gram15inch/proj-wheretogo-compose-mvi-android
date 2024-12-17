package com.wheretogo.domain.model.user

data class SignResponse(val status: Status, val profile: Profile? = null) {
    enum class Status { Success, Fail, Error }
}