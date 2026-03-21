package com.wheretogo.data.model.content

data class ContentFilterRequest(
    val filters: List<Filter> = emptyList(),
    val orderBy: OrderBy? = null,
    val limit: Int = 50
)

data class Filter(
    val field: String,
    val operator: Operator,
    val value: Any
)

enum class Operator {
    EQUALS,
    GREATER_THAN,
    LESS_THAN,
    CONTAINS,
}

data class OrderBy(
    val field: String,
    val direction: Direction = Direction.ASC,
)

enum class Direction { ASC, DESC }