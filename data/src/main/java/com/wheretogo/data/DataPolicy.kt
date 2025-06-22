package com.wheretogo.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wheretogo.domain.DomainError

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
            if(!BuildConfig.DEBUG)
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
//파이어스토어 컬렉션명
enum class FireStoreCollections {
    USER,
    BOOKMARK,
    HISTORY,

    COURSE,
    CHECKPOINT,
    ROUTE,
    LIKE,

    COMMENT,
    REPORT,
    REPORT_CONTENT,

    PRIVATE,
}

fun FireStoreCollections.name(): String {
    return if (BuildConfig.DEBUG)
        "TEST_" + this.name
    else
        "RELEASE_" + this.name
}

fun getDbOption():Int{
    return if(BuildConfig.DEBUG) 0 else 1
}

enum class LikeObject {
    COURSE_LIKE
}