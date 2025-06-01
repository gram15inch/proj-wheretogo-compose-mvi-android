import wheretogo.AndroidConfig
import wheretogo.AndroidX
import wheretogo.Dagger
import wheretogo.Kotlin
import wheretogo.Compose
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

    defaultConfig {
        buildConfigField( "String", "KAKAO_NATIVE_APP_KEY", getAppKey("kakaoNativeApp"))
        buildConfigField( "String", "KAKAO_REST_API_KEY", getAppKey("kakaoNativeApp"))
        buildConfigField( "String", "KAKAO_ADMIN_KEY", getAppKey("kakaoNativeApp"))

        buildConfigField( "String", "TMAP_APP_KEY", getAppKey("tmapApp"))

        buildConfigField( "String", "GOOGLE_WEB_CLIENT_ID_KEY", getAppKey("googleWebClientId"))

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
    implementation(project(mapOf("path" to ":domain")))

    implementation(AndroidX.CORE_KTX)

    // BOM
    implementation(platform(Kotlin.KOTLIN_BOM))
    implementation(platform(Compose.COMPOSE_BOM))

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
    implementation(Squareup.RETROFIT_CONVERTER_MOSHI)
    implementation(Squareup.MOSHI_KOTLIN)

    // Goggle
    implementation(Google.IDENTITY_GOOGLEID)
    implementation(Google.PLAY_SERVICES_LOCATION)
    implementation(Google.ACCOMPANIST_NAVIGATION_ANIMATION)

    // Libraries
    implementation(Libraries.KAKAO_MAPS)
    implementation(Libraries.NAVER_MAPS)
    implementation(files("libs/com.skt.Tmap_1.76.jar"))
    implementation(Libraries.LOTTIE_COMPOSE)
    implementation(Libraries.SHIMMER_COMPOSE)
    implementation(Libraries.LANDSCAPIST_GLIDE)
    implementation(Libraries.FIREBASE_GEOFIRE)
    implementation(Libraries.FIREBASE_GEOFIRE_COMMON)
}

fun getAppKey(propertyKey: String): String {
    val localPropertiesFile = File(rootProject.projectDir, "local.properties")
    val properties = Properties()

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    } else {
        localPropertiesFile.createNewFile()
    }

    val defaultValue = "\"yourAppKey\""

    if (!properties.containsKey(propertyKey)) {
        properties[propertyKey] = defaultValue
        localPropertiesFile.outputStream().use { properties.store(it, null) }
    }
    return properties[propertyKey].toString()
}
