package com.wheretogo.data

const val NAVER_OPEN_API_APIGW_URL = "https://naveropenapi.apigw.ntruss.com"
const val NAVER_OPEN_API_URL = "https://openapi.naver.com/"

const val DATA_NULL = ""
const val IMAGE_DOWN_MAX_MB = 10
//파이어스토어 컬렉션명
enum class FireStoreCollections {
    USER,
    BOOKMARK,
    HISTORY,

    COURSE,
    CHECKPOINT,
    ROUTE,
    LIKE,

    COMMENT,
    REPORT,

    PUBLIC,
    PRIVATE,
}

fun FireStoreCollections.name(): String {
    return if (BuildConfig.DEBUG)
        "TEST_" + this.name
    else
        "RELEASE_" + this.name
}

enum class LikeObject {
    COURSE_LIKE
}