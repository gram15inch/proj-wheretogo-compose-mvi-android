package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.model.util.AppCache

interface ClearCacheUseCase {
    suspend operator fun invoke(
        caches: Set<AppCache>
    ): Result<Unit>
}