package com.wheretogo.domain.model.user

data class UserResponse(val status: Status, val profile: Profile? = null, val msg: String = "") {
    enum class Status { Success, Fail }
}