import wheretogo.AndroidX
import wheretogo.Dagger
import wheretogo.Kotlin
import wheretogo.Squareup

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.wheretogo.domain"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    implementation(AndroidX.CORE_KTX)
    implementation(platform(Kotlin.KOTLIN_BOM))

    //hilt
    implementation(Dagger.HILT_ANDROID)
    ksp(Dagger.HILT_COMPILER)

    //retrofit
    implementation (Squareup.RETROFIT)
    implementation (Squareup.RETROFIT_CONVERTER_MOSHI)
    implementation(Squareup.MOSHI_KOTLIN)
}