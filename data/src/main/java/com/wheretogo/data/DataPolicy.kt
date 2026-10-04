package com.wheretogo.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.storage.StorageException
import com.wheretogo.data.feature.safeErrorBody
import com.wheretogo.domain.BanReason
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.SignErrorReason
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.IOException
import retrofit2.Response
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

data class DataBuildConfig(
    val firebaseCloudApiUrl: String,
    val naverOpenApiUrl: String,
    val googleWebClientId: String,
    val tokenRequestKey: String,
    val naverClientIdKey: String,
    val naverClientSecretKey: String,
    val isTokenLog: Boolean,
    val dbPrefix: String,
)

const val DATA_NULL = ""

// 전체 갱신용
val CheckpointPolicy = DefaultPolicy(60, 15)
val CommentPolicy = DefaultPolicy(20, 5)

@Serializable
enum class DataAuthCompany {
    GOOGLE,
    PROFILE
}

@Serializable
enum class DataHistoryType {
    COMMENT,
    COURSE,
    CHECKPOINT,
    LIKE,
    REPORT
}

@Serializable
enum class DataReportType {
    USER,
    COURSE,
    COMMENT,
    CHECKPOINT
}

enum class ImageFormat(val ext: String) {
    JPEG("jpg"), WEBP("webp")
}

@Serializable
enum class DataSettingAttr {
   TUTORIAL
}


class HttpCodeException(code: Int, errorCode: String?) : Exception()
class ResponseException(msg: String) : IllegalStateException(msg)

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class HttpError(val code: Int, val message: String?) : ApiResult<Nothing>
    data class NetworkError(val exception: IOException) : ApiResult<Nothing>
    data class UnknownError(val throwable: Throwable) : ApiResult<Nothing>
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.HttpError -> this
    is ApiResult.NetworkError -> this
    is ApiResult.UnknownError -> this
}

suspend fun<T> nullableApiCall(block: suspend () -> Response<T>): ApiResult<T?> = try {
    val response= block()

    if(!response.isSuccessful)
        throw HttpCodeException(response.code(), parseResponseCode(response))

    val body = response.body()

    ApiResult.Success(body)
}  catch (e: CancellationException) {
    throw e // 코루틴 취소는 삼키면 안 됨
} catch (e: IOException) {
    ApiResult.NetworkError(e)
} catch (e: Exception) {
    ApiResult.UnknownError(e)
}

suspend fun<T> apiCall(block: suspend () -> Response<T>): ApiResult<T> = try {
    val response= block()

    if(!response.isSuccessful)
        throw HttpCodeException(response.code(), parseResponseCode(response))

    val body = response.body() ?: run {
        throw ResponseException("200 인데 body 없음")
    }

    ApiResult.Success(body)
}  catch (e: CancellationException) {
    throw e // 코루틴 취소는 삼키면 안 됨
} catch (e: IOException) {
    ApiResult.NetworkError(e)
} catch (e: Exception) {
    ApiResult.UnknownError(e)
}

private fun parseResponseCode(e: Response<*>): String? =
    runCatching {
        e.errorBody()?.string()?.let { Json.decodeFromString<ErrorBody>(it).code }
    }.getOrNull()


@Serializable
private data class ErrorBody(
    val code: String? = null,
    val message: String? = null)

sealed class DataError: IOException(){
    data class NetworkError(val msg:String = ""): DataError()
    data class Unauthorized(val msg:String = ""): DataError()
    data class TooManyRequests(val msg:String = ""): DataError()
    data class ServerError(val msg:String = ""): DataError()
    data class UnexpectedException(val msg:String): DataError()
    data class UserNotFound(val msg:String = ""): DataError()

    data class UserUnavailable(val msg:String = ""): DataError()
    data class Forbidden(val msg:String = ""): DataError()
    data class AuthInvalid(val msg:String = ""): DataError()
    data class PublicTokenInvalid(val msg:String = ""): DataError()
    data class ArgumentInvalid(val msg:String = ""): DataError()
    data class NotFound(val msg:String = ""): DataError()
    data class Conflict(val msg:String = ""): DataError()
    data class RiskContent(val msg:String = ""): DataError()
    data class InternalError(val msg:String = ""): DataError()
}

fun Response<*>.toDataError(): DataError {
    val body = safeErrorBody()
    val msg = body?.message?:"알 수 없는 오류"
    return when(code()){
        400 -> DataError.ArgumentInvalid(msg)
        401 -> DataError.Unauthorized(msg)
        403 -> DataError.Forbidden(msg)
        404 -> DataError.NotFound(msg)
        409 -> DataError.Conflict(msg)
        422 -> DataError.RiskContent(msg)
        429 -> DataError.TooManyRequests(msg)
        503 -> DataError.UserUnavailable(msg)
        else -> {
            Timber.e(
                "HTTP [%d] message: %s",
                code(),
                msg
            )
            DataError.ServerError(msg)
        }
    }
}

fun StorageException.toDataError(): DataError{
    val msg = this.message?:""
    return when(httpResultCode){
        404 -> DataError.NotFound(msg)
        else -> {
            Timber.e("StorageException -> DataError: ${stackTraceToString()}")
            DataError.UnexpectedException(msg)
        }
    }
}

fun Throwable?.toDataError(): DataError{
    return when(this){
        is DataError -> this
        is StorageException -> toDataError()
        is UnknownHostException -> DataError.NetworkError("UnknownHostException")
        is SocketTimeoutException -> DataError.NetworkError("SocketTimeoutException")
        is java.io.IOException -> DataError.NetworkError("IOException")
        is FirebaseNetworkException -> DataError.NetworkError()
        is FirebaseAuthInvalidUserException -> DataError.AuthInvalid(SignErrorReason.SUSPEND_USER.name)
        null -> DataError.InternalError("알수없는 오류")
        else -> {
            Timber.e("Throwable -> DataError: ${stackTraceToString()}")
            DataError.UnexpectedException(message?:"")
        }
    }
}

fun DataError.toDomainError(): DomainError{
    return when(this){
        is DataError.NotFound->{ DomainError.NotFound(this.msg) }
        is DataError.NetworkError->{ DomainError.NetworkError(this.msg) }
        is DataError.ServerError->{
            Timber.tag("data").e(this.stackTraceToString())
            DomainError.NetworkError("Server Error")
        }
        is DataError.UserNotFound->{ DomainError.UserExpired(this.msg) }
        is DataError.Unauthorized->{ DomainError.Unauthorized(this.msg) }
        is DataError.UserUnavailable->{ DomainError.UserUnavailable(this.msg) }
        is DataError.AuthInvalid->{ DomainError.SignInError(this.msg) }
        is DataError.RiskContent->{ DomainError.PolicyDeny(BanReason.INAPPROPRIATE.name) }
        is DataError.TooManyRequests->{ DomainError.PolicyDeny(BanReason.OTHER.name) }
        else -> {
            Timber.e("DataError -> DomainError: ${stackTraceToString()}")
            DomainError.UnexpectedException()
        }
    }

}

fun<T> Result<T>.toDomainResult(): Result<T>{
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = {
            if(it is DataError)
                Result.failure(it.toDomainError())
            else
                Result.failure(it)
        }
    )
}

sealed interface CachePolicy {
    fun isExpired(timestamp: Long, isEmpty: Boolean): Boolean
}

data class DefaultPolicy(
    val minuteWhenEmpty: Int = 60,
    val minuteWhenNotEmpty: Int = 15
) : CachePolicy {
    override fun isExpired(timestamp: Long, isEmpty: Boolean): Boolean {
        val refreshDuration =
            TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timestamp)
        return when {
            isEmpty && refreshDuration >= minuteWhenEmpty -> true
            !isEmpty && refreshDuration >= minuteWhenNotEmpty -> true
            else -> false
        }
    }
}


//파이어스토어 컬렉션명
enum class FireStoreCollections {
    USER,
    BOOKMARK,
    HISTORY,

    COURSE,
    CHECKPOINT,
    ROUTE,
    ROUTE_PATH,
    LIKE,
    IMAGE,

    COMMENT,
    REPORT,

    PRIVATE,
}