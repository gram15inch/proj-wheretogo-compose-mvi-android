package com.wheretogo.domain.model.course

object LoadingConfig {

    const val SYNC_INTERVAL_MS = 15 * 60 * 1000 // 15분

    const val SYNC_PAGE_LIMIT = 500 // 서버 한계: 1000

    const val SYNC_MAX_PAGES = 8

    const val DECODED_CACHE_ENTRIES = 64
}