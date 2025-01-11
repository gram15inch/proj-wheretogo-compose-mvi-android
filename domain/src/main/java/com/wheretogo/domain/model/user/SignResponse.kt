package com.wheretogo.domain.model.user

data class SignResponse(val status: Status, val profile: Profile? = null, val msg: String = "") {
    enum class Status { Success, Fail }
}