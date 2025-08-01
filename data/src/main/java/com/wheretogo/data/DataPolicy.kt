package com.wheretogo.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.model.map.Snapshot
import java.util.concurrent.TimeUnit

const val NAVER_OPEN_API_APIGW_URL = "https://naveropenapi.apigw.ntruss.com"
const val NAVER_MAPS_NTRUSS_APIGW_URL = "https://maps.apigw.ntruss.com/"
const val NAVER_OPEN_API_URL = "https://openapi.naver.com/"
const val FIREBASE_CLOUD_API_URL = "https://asia-northeast3-where-to-go-35813.cloudfunctions.net/"

const val DATA_NULL = ""
const val IMAGE_DOWN_MAX_MB = 10

sealed class DataError: Exception(){
    data class NetworkError(val msg:String = ""): DataError()
    data class UserInvalid(val msg:String = ""): DataError()
    data class ServerError(val msg:String = ""): DataError()
    data class UnexpectedException(val throwable:Throwable): DataError()
}

fun Exception.toDataError():DataError{
    return when(this){
        is DataError -> this
        is FirebaseNetworkException -> DataError.NetworkError()
        is FirebaseAuthInvalidUserException -> DataError.UserInvalid()
        else -> DataError.UnexpectedException(this)
    }
}

fun DataError.toDomainError():DomainError{
    return when(this){
        is DataError.NetworkError->{ DomainError.NetworkError() }
        is DataError.ServerError->{ DomainError.NetworkError("Server Error") }
        is DataError.UserInvalid->{ DomainError.UserInvalid() }
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
    fun isExpired(snapshot: Snapshot): Boolean
}

data object CheckpointPolicy : CachePolicy {
    override fun isExpired(snapshot: Snapshot): Boolean {
        val refreshDuration =
            TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - snapshot.timeStamp)
        return when {
            snapshot.indexIdGroup.isEmpty() && refreshDuration >= 60 ->  true
            snapshot.indexIdGroup.isNotEmpty() && refreshDuration >= 15 -> true
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

fun getDbOption():Int{
    return when(BuildConfig.FIREBASE){
        "RELEASE" ->  1
        else -> 0
    }
}

enum class LikeObject {
    COURSE_LIKE
}