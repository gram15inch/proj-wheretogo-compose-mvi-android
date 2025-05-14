package com.wheretogo.data.model.user

data class UserDeleteRequest(
    val uid:String,
    val option:Int = 0
)
