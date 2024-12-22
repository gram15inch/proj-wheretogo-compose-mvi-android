package com.wheretogo.domain


const val COURSE_TYPE = 0
const val CHECKPOINT_TYPE = 1
const val PATH_TYPE = 2

const val DAY = 86400000L
const val CHECKPOINT_UPDATE_TIME = DAY

const val USER_DATE_FORMAT = "yyyy-MM-dd"
const val DOMAIN_EMPTY = ""


enum class AUTH_COMPANY { GOOGLE }

enum class HistoryType {
    COMMENT, LIKE, BOOKMARK
}

