package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.SearchType
import com.wheretogo.domain.model.address.SimpleAddress

interface SearchKeywordUseCase {
    suspend operator fun invoke(query: String, type: SearchType = SearchType.ALL): Result<List<SimpleAddress>>
}