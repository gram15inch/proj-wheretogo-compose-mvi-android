import wheretogo.AndroidConfig
import wheretogo.AndroidX
import wheretogo.Dagger
import wheretogo.Kotlin
import wheretogo.Libraries
import wheretogo.UnitTest

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.wheretogo.domain"
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
    }

    buildTypes{
        create("qa") {}
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // BOM
    implementation(platform(Kotlin.KOTLIN_BOM))

    // AndroidX
    implementation(AndroidX.CORE_KTX)
    implementation(AndroidX.EXIFINTERFACE)

    // Dagger
    implementation(Dagger.HILT_ANDROID)
    ksp(Dagger.HILT_COMPILER)

    // Libraries
    implementation(Libraries.FIREBASE_GEOFIRE)
    implementation(Libraries.FIREBASE_GEOFIRE_COMMON)
    implementation(Libraries.HUXHORN_SULKY_ULID)
    implementation(Libraries.KOMORAN)

    //Test
    testImplementation(UnitTest.JUNIT_JUPITER)
    testImplementation(UnitTest.JUNIT_JUPITER_API)
    testImplementation(UnitTest.JUNIT_JUPITER_PARAMS)
    testImplementation(UnitTest.JUNIT_JUPITER_ENGINE)
    testImplementation(UnitTest.JUNIT_VINTAGE_ENGINE)
}