package com.wheretogo.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wheretogo.data.feature.safeErrorBody
import com.wheretogo.domain.DomainError
import retrofit2.Response
import java.util.concurrent.TimeUnit

const val NAVER_OPEN_API_APIGW_URL = "https://naveropenapi.apigw.ntruss.com"
const val NAVER_MAPS_NTRUSS_APIGW_URL = "https://maps.apigw.ntruss.com/"
const val NAVER_OPEN_API_URL = "https://openapi.naver.com/"
const val FIREBASE_CLOUD_API_URL = "https://asia-northeast3-where-to-go-35813.cloudfunctions.net/"
const val FIREBASE_CLOUD_STAGING_API_URL = "https://asia-northeast3-where-to-go-staging.cloudfunctions.net/"

const val DATA_NULL = ""
const val IMAGE_DOWN_MAX_MB = 10

val CheckpointPolicy = DefaultPolicy(60, 15)
val CommentPolicy = DefaultPolicy(20, 5)
val CoursePolicy = DefaultPolicy(60, 15)

sealed class DataError: Exception(){
    data class NetworkError(val msg:String = ""): DataError()
    data class UserInvalid(val msg:String = ""): DataError()
    data class Unauthorized(val msg:String = ""): DataError()
    data class ArgumentInvalid(val msg:String = ""): DataError()
    data class Forbidden(val msg:String = ""): DataError()
    data class NotFound(val msg:String = ""): DataError()
    data class Conflict(val msg:String = ""): DataError()
    data class TooManyRequests(val msg:String = ""): DataError()
    data class ServerError(val msg:String = ""): DataError()
    data class InternalError(val msg:String = ""): DataError()
    data class UnexpectedException(val throwable:Throwable): DataError()
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
        429 -> DataError.TooManyRequests(msg)
        else -> DataError.ServerError(msg)
    }
}


fun Throwable?.toDataError():DataError{
    return when(this){
        is DataError -> this
        is FirebaseNetworkException -> DataError.NetworkError()
        is FirebaseAuthInvalidUserException -> DataError.UserInvalid()
        null -> DataError.InternalError("알수없는 오류")
        else -> DataError.UnexpectedException(this)
    }
}

fun DataError.toDomainError():DomainError{
    return when(this){
        is DataError.NetworkError->{ DomainError.NetworkError() }
        is DataError.ServerError->{ DomainError.NetworkError("Server Error") }
        is DataError.UserInvalid->{ DomainError.UserInvalid() }
        is DataError.Unauthorized->{ DomainError.UserInvalid() }
        else -> {
            FirebaseCrashlytics.getInstance().recordException(this)
            DomainError.UnexpectedException(this)
        }
    }

}

fun<T> Result<T>.toDomainResult():Result<T>{
    return fold(
        onSuccess = { Result.success(it)},
        onFailure = {
            Result.failure((it as DataError).toDomainError())
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
    LIKE,
    META,

    COMMENT,
    REPORT,
    REPORT_CONTENT,

    PRIVATE,
}

fun FireStoreCollections.name(): String {
    return when(BuildConfig.FIREBASE){
        "RELEASE" ->  "RELEASE_" + this.name
        else -> "TEST_" + this.name
    }
}

fun firebaseApiUrlByBuild(): String{
    return when(BuildConfig.FIREBASE){
        "RELEASE" -> FIREBASE_CLOUD_API_URL
        else -> FIREBASE_CLOUD_STAGING_API_URL
    }
}

enum class LikeObject {
    COURSE_LIKE
}