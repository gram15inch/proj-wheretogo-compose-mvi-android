import wheretogo.AndroidConfig
import wheretogo.AndroidX
import wheretogo.Dagger
import wheretogo.Kotlin
import wheretogo.Compose
import wheretogo.Firebase
import wheretogo.Google
import wheretogo.Libraries
import wheretogo.Squareup
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.wheretogo.presentation"
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
    }

    buildTypes{
        val testNativeAdId = "\"ca-app-pub-3940256099942544/2247696110\""
        debug {
            buildConfigField( "Boolean", "TEST_UI", "true")
            buildConfigField( "String", "NATIVE_AD_ID", testNativeAdId)
        }
        create("qa") {
            buildConfigField( "Boolean", "TEST_UI", "false")
            buildConfigField( "String", "NATIVE_AD_ID", testNativeAdId)
        }
        release {
            buildConfigField( "Boolean", "TEST_UI", "false")
            buildConfigField( "String", "NATIVE_AD_ID", getLocalProperties("nativeAdId"))
        }
    }

    buildFeatures{
        buildConfig = true
    }

    defaultConfig {
        buildConfigField( "String", "KAKAO_NATIVE_APP_KEY", getLocalProperties("kakaoNativeApp"))
        buildConfigField( "String", "KAKAO_REST_API_KEY", getLocalProperties("kakaoNativeApp"))
        buildConfigField( "String", "KAKAO_ADMIN_KEY", getLocalProperties("kakaoNativeApp"))

        buildConfigField( "String", "TMAP_APP_KEY", getLocalProperties("tmapApp"))

        manifestPlaceholders["adsMobAppId"] = getLocalProperties("adsMobAppId")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(mapOf("path" to ":domain")))

    implementation(AndroidX.CORE_KTX)

    // BOM
    implementation(platform(Kotlin.KOTLIN_BOM))
    implementation(platform(Compose.COMPOSE_BOM))

    // KOTLIN
    implementation(Kotlin.KOTLINX_COROUTINES_PLAY_SERVICES)

    // Compose
    implementation(Compose.COMPOSE_UI)
    implementation(Compose.COMPOSE_UI_GRAPHICS)
    implementation(Compose.COMPOSE_UI_TOOL_PREVIEW)
    implementation(Compose.COMPOSE_MATERIAL3)
    debugImplementation(Compose.COMPOSE_UI_TOOL)
    debugImplementation(Compose.COMPOSE_UI_TEST_MANIFEST)

    // AndroidX
    implementation(AndroidX.LIFECYCLE_RUNTIME_KTX)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL_COMPOSE)
    implementation(AndroidX.ACTIVITY_COMPOSE)
    implementation(AndroidX.NAVIGATION_COMPOSE)
    implementation(AndroidX.HILT_NAVIGATION_COMPOSE)
    implementation(AndroidX.EXIFINTERFACE)
    implementation(AndroidX.CREDENTIALS)
    implementation(AndroidX.CREDENTIALS_AUTH)
    implementation (AndroidX.BROWSER)
    ksp(AndroidX.HILT_COMPILER)

    // Dagger
    implementation(Dagger.HILT_ANDROID)
    ksp(Dagger.HILT_COMPILER)
    ksp(Dagger.HILT_ANDROID_COMPILER)

    // Retrofit
    implementation(Squareup.RETROFIT)

    // Goggle
    implementation(Google.IDENTITY_GOOGLEID)
    implementation(Google.PLAY_SERVICES_LOCATION)
    implementation(Google.PLAY_SERVICES_ADS)

    // Firebase
    implementation(platform(Firebase.FIREBASE_BOM))
    implementation(Firebase.FIREBASE_CRASHLYTICS)

    // Libraries
    implementation(Libraries.NAVER_MAPS)
    implementation(files("libs/com.skt.Tmap_1.76.jar"))
    implementation(Libraries.LOTTIE_COMPOSE)
    implementation(Libraries.LANDSCAPIST_GLIDE)
    implementation(Libraries.SHIMMER_COMPOSE)
    implementation(Libraries.JAKEWHARTON_TIMBER)
}

fun getLocalProperties(key: String): String{
    val localPropertiesFile = File(rootProject.projectDir, "local.properties")
    val properties = Properties()

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }

    return properties[key].toString()
}
