package com.wheretogo.data.feature

import com.wheretogo.data.DataError
import com.wheretogo.data.toDataError
import com.wheretogo.data.toDomainError
import com.wheretogo.domain.DomainError

fun <T> Result<T?>.mapDomainError(): Result<T> =
    fold(
        onSuccess = {
            if (it == null)
                Result.failure(DomainError.NotFound())
            else
                Result.success(it)
        },
        onFailure = {
            val t = if (it is DataError) {
                it.toDomainError()
            } else {
                it
            }
            Result.failure(t)
        }
    )

inline fun <T, R> Result<T>.mapSuccess(
    transform: (T) -> Result<R>
): Result<R> = fold(
    onSuccess = transform,
    onFailure = { Result.failure(it) }
)

fun <T> Result<T?>.mapDataError(): Result<T> =
    fold(
        onSuccess = {
            if (it == null)
                Result.failure(DataError.NotFound(""))
            else
                Result.success(it)
        },
        onFailure = { Result.failure(it.toDataError()) }
    )


inline  fun <T> Result<T>.catchNotFound(transform: ()-> Result<T>): Result<T> =
    fold(
        onSuccess = {
            Result.success(it)
        },
        onFailure = {
            if (it is DataError.NotFound || it is DomainError.NotFound)
                transform()
            else
                Result.failure(it)
        }
    )


inline fun <T> dataErrorCatching(run: () -> T?): Result<T> {
    return runCatching {
        run()
    }.mapDataError()
}