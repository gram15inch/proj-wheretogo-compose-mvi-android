import com.dhkim139.wheretogo.AndroidX
import com.dhkim139.wheretogo.Versions
import com.dhkim139.wheretogo.Kotlin
import com.dhkim139.wheretogo.UnitTest
import com.dhkim139.wheretogo.AndroidTest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.dhkim139.wheretogo"
    compileSdk = Versions.COMPILE_SDK_VERSION

    defaultConfig {
        applicationId = "com.dhkim139.wheretogo"
        minSdk = Versions.MIN_SDK_VERSION
        targetSdk = Versions.TARGET_SDK_VERSION
        versionCode = Versions.VERSION_CODE
        versionName = Versions.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.KOTLIN_COMPILER_EXTENSION_VERSION
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(AndroidX.CORE_KTX)
    implementation(platform(Kotlin.KOTLIN_BOM))
    implementation(AndroidX.LIFECYCLE_RUNTIME_KTX)
    implementation(AndroidX.ACTIVITY_COMPOSE)
    implementation(platform(AndroidX.COMPOSE_BOM))
    implementation(AndroidX.COMPOSE_UI)
    implementation(AndroidX.COMPOSE_UI_GRAPHICS)
    implementation(AndroidX.COMPOSE_UI_TOOL_PREVIEW)
    implementation(AndroidX.COMPOSE_MATERIAL3)


    testImplementation(UnitTest.JUNIT)

    androidTestImplementation(AndroidTest.ANDROID_JUNIT)
    androidTestImplementation(AndroidTest.ESPRESSO_CORE)
    androidTestImplementation(platform(AndroidX.COMPOSE_BOM))
    androidTestImplementation(AndroidX.COMPOSE_UI_TEST_JUNIT4)

    debugImplementation(AndroidX.COMPOSE_UI_TOOL)
    debugImplementation(AndroidX.COMPOSE_UI_TEST_MANIFEST)
}
