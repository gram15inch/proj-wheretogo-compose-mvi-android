import wheretogo.AndroidX
import wheretogo.Dagger
import wheretogo.Kotlin
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

    //BOM
    implementation(platform(Kotlin.KOTLIN_BOM))
    implementation(platform(AndroidX.COMPOSE_BOM))
    androidTestImplementation(platform(AndroidX.COMPOSE_BOM))


    //compose
    implementation(AndroidX.COMPOSE_UI)
    implementation(AndroidX.COMPOSE_UI_GRAPHICS)
    implementation(AndroidX.COMPOSE_UI_TOOL_PREVIEW)
    implementation(AndroidX.COMPOSE_MATERIAL3)

    implementation(Libraries.LOTTIE_COMPOSE)

    implementation(AndroidX.LIFECYCLE_RUNTIME_KTX)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL_COMPOSE)
    implementation(AndroidX.ACTIVITY_COMPOSE)

    androidTestImplementation(AndroidX.COMPOSE_UI_TEST_JUNIT4)
    androidTestImplementation(Libraries.LOTTIE_COMPOSE)

    debugImplementation(AndroidX.COMPOSE_UI_TOOL)
    debugImplementation(AndroidX.COMPOSE_UI_TEST_MANIFEST)

    //navigation
    implementation(AndroidX.NAVIGATION_COMPOSE)

    //hilt
    implementation(Dagger.HILT_ANDROID)
    implementation(AndroidX.HILT_NAVIGATION_COMPOSE)

    ksp(AndroidX.HILT_COMPILER)
    ksp(Dagger.HILT_COMPILER)
    ksp(Dagger.HILT_ANDROID_COMPILER)

    //retrofit
    implementation(Squareup.RETROFIT)
    implementation(Squareup.RETROFIT_CONVERTER_MOSHI)
    implementation(Squareup.MOSHI_KOTLIN)

    //Map
    implementation("com.kakao.maps.open:android:2.11.9")
    implementation("com.naver.maps:map-sdk:3.19.1")
    implementation(files("libs/com.skt.Tmap_1.76.jar"))


    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    //etc
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.github.skydoves:landscapist-glide:2.4.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.31.1-alpha")

    implementation ("com.firebase:geofire-android:3.2.0")
    implementation ("com.firebase:geofire-android-common:3.2.0")
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
