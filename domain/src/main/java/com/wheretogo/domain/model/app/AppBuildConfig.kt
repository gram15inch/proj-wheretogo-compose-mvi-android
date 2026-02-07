package com.wheretogo.domain.model.app

data class AppBuildConfig(
    val firebaseCloudApiUrl: String,
    val naverMapsNtrussApigwUrl: String,
    val naverOpenApiUrl: String,
    val googleWebClientId: String,
    val tokenRequestKey: String,
    val naverMapsApigwClientIdKey: String,
    val naverMapsApigwClientSecretkey: String,
    val naverClientIdKey: String,
    val naverClientSecretKey: String,
    val tmapAppKey: String,
    val nativeAdId: String,
    val isCoolDown: Boolean,
    val isCrashlytics: Boolean,
    val isTokenLog: Boolean,
    val isTestUi: Boolean,
    val isAdPreLoad: Boolean,
    val dbPrefix: String,
)