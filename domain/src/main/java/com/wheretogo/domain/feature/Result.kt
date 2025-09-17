package com.wheretogo.domain.feature

import com.wheretogo.domain.toDomainError

inline fun <T, R> Result<T>.domainMap(
    transform: (T) -> Result<R>
): Result<R> = fold(
    onSuccess = transform,
    onFailure = { Result.failure(it.toDomainError()) }
)

inline fun <A, B, R> Result<A>.zip(
    other: Result<B>,
    transform: (A, B) -> R
): Result<R> {
    return this.fold(
        onSuccess = { a ->
            other.fold(
                onSuccess = { b -> Result.success(transform(a, b)) },
                onFailure = { e -> Result.failure(e) }
            )
        },
        onFailure = { e -> Result.failure(e) }
    )
}

fun <T> List<Result<T>>.flatMap(): Result<List<T>> {
    val values = mutableListOf<T>()
    for (r in this) {
        r.fold(
            onSuccess = { values.add(it) },
            onFailure = { return Result.failure(it) }
        )
    }
    return Result.success(values)
}

suspend fun <T> List<Result<Any>>.domainFlatMap(onSuccess: suspend () -> Result<T>): Result<T> {
    for (r in this) {
        r.fold(
            onSuccess = { },
            onFailure = { return Result.failure(it.toDomainError()) }
        )
    }
    return onSuccess()
}