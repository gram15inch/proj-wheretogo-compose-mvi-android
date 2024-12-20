package com.wheretogo.presentation

import androidx.annotation.StringRes

enum class CommentType(@StringRes val typeRes: Int) {
    ONE(R.string.oneline_review), DETAIL(R.string.detail_review)
}