package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.SearchType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.SimpleAddress

interface SearchKeywordUseCase {
    suspend operator fun invoke(query: String, type :SearchType = SearchType.ALL): UseCaseResponse<List<SimpleAddress>>
}