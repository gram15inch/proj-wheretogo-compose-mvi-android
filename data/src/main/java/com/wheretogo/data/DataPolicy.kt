package com.wheretogo.data

const val NAVER_OPEN_API_URL = "https://naveropenapi.apigw.ntruss.com"
const val DATA_NULL = ""
const val DAY = 86400000L
const val CHECKPOINT_UPDATE_TIME = DAY

//파이어스토어 테이블명
enum class FireStoreTableName() {
    USER_TABLE,
    PROFILE_TABLE,
    LIKE_HISTORY_TABLE,
    BOOKMARK_TABLE,

    COURSE_TABLE,
    CHECKPOINT_TABLE,
    META_CHECKPOINT_TABLE,
    ROUTE_TABLE,
    LIKE_TABLE,
    COURSE_LIKE_TABLE,
    COMMENT_TABLE
}

fun FireStoreTableName.name(): String {
    return if (BuildConfig.DEBUG)
        "TEST_" + this.name
    else
        this.name
}

enum class LikeObject {
    COURSE_LIKE
}