package wheretogo


object AndroidConfig {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 24
    const val TARGET_SDK = 35
}

object Versions {

    const val KOTLIN_COMPILER_EXTENSION_VERSION = "1.5.15"

    const val KOTLIN_VERSION     = "2.0.0"
    const val CORE      = "1.13.1"

    const val LIFECYCLE = "2.8.5"

    const val NAVIGATION = "2.8.0"

    const val TEST = "1.6.1"

    const val ACTIVITY = "1.9.2"
    const val COMPOSE_BOM = "2024.09.02"

    const val HILT     = "1.2.0"
    const val DAGGER     = "2.52"

    const val ROOM     = "2.6.1"

    const val RETROFIT     = "2.9.0"
    const val MOSHI     = "1.13.0"
    const val SERIALIZATION     = "1.8.0"


    const val JUPITER       = "5.11.4"
    const val JUNIT5_TEST   = "1.4.0"
    const val ANDROID_JUNIT = "1.1.5"
    const val ESPRESSO_CORE = "3.6.1"


    const val FIREBASE_BOM = "33.3.0"
    const val WORK = "2.9.1"

    const val CREDENTIALS = "1.3.0"
    const val FILREBASE_GEOFIRE = "3.2.0"
    const val DATASTORE = "1.1.3"
}

object Kotlin {
    const val KOTLIN_BOM = "org.jetbrains.kotlin:kotlin-bom:${Versions.KOTLIN_VERSION}"
    const val KOTLINX_COROUTINES_PLAY_SERVICES = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0"
    const val KOTLINX_SERIALIZATION_CBOR = "org.jetbrains.kotlinx:kotlinx-serialization-cbor:${Versions.SERIALIZATION}"
    const val KOTLINX_SERIALIZATION_JSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.SERIALIZATION}"
}

object AndroidX {
    const val CORE_KTX                  = "androidx.core:core-ktx:${Versions.CORE}"

    const val LIFECYCLE_RUNTIME_KTX         = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}"
    const val LIFECYCLE_VIEWMODEL_COMPOSE   = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.LIFECYCLE}"

    const val ACTIVITY_COMPOSE          = "androidx.activity:activity-compose:${Versions.ACTIVITY}"
    const val NAVIGATION_COMPOSE        = "androidx.navigation:navigation-compose:${Versions.NAVIGATION}"
    const val DATASTORE_PREFERENCE_CORE = "androidx.datastore:datastore-preferences-core-jvm:${Versions.DATASTORE}"
    const val DATASTORE_PREFERENCES     = "androidx.datastore:datastore-preferences:${Versions.DATASTORE}"
    const val CREDENTIALS               = "androidx.credentials:credentials:${Versions.CREDENTIALS}"
    const val CREDENTIALS_AUTH          = "androidx.credentials:credentials-play-services-auth:${Versions.CREDENTIALS}"
    const val EXIFINTERFACE             = "androidx.exifinterface:exifinterface:1.3.7"
    const val BROWSER                   = "androidx.browser:browser:1.8.0"
    const val SECURITY_CRYPTO           = "androidx.security:security-crypto:1.1.0"

    const val HILT_NAVIGATION_COMPOSE   = "androidx.hilt:hilt-navigation-compose:${Versions.HILT}"
    const val HILT_COMPILER             = "androidx.hilt:hilt-compiler:${Versions.HILT}"
    const val HILT_COMMON               = "androidx.hilt:hilt-common:${Versions.HILT}"
    const val HILT_WORK                 = "androidx.hilt:hilt-work:${Versions.HILT}"

    const val ROOM_RUNTIME      = "androidx.room:room-runtime:${Versions.ROOM}"
    const val ROOM_KTX          = "androidx.room:room-ktx:${Versions.ROOM}"
    const val ROOM_COMPILER     = "androidx.room:room-compiler:${Versions.ROOM}"
    const val ROOM_TESTING      = "androidx.room:room-testing:${Versions.ROOM}"

    const val WORK_RUNTIME_KTX  = "androidx.work:work-runtime-ktx:${Versions.WORK}"

    const val TEST_RUNNER       = "androidx.test:runner:${Versions.TEST}"
    const val TEST_CORE_KTX     = "androidx.test:core-ktx:${Versions.TEST}"
    const val TEST_UIAUTOMATOR  = "androidx.test.uiautomator:uiautomator:2.3.0"
}

object Compose{
    const val COMPOSE_BOM              = "androidx.compose:compose-bom:${Versions.COMPOSE_BOM}"
    const val COMPOSE_UI               = "androidx.compose.ui:ui"
    const val COMPOSE_UI_GRAPHICS      = "androidx.compose.ui:ui-graphics"
    const val COMPOSE_UI_TOOL_PREVIEW  = "androidx.compose.ui:ui-tooling-preview"
    const val COMPOSE_UI_TOOL          = "androidx.compose.ui:ui-tooling"
    const val COMPOSE_UI_TEST_JUNIT4   = "androidx.compose.ui:ui-test-junit4"
    const val COMPOSE_UI_TEST_MANIFEST = "androidx.compose.ui:ui-test-manifest"
    const val COMPOSE_MATERIAL3        = "androidx.compose.material3:material3"
}

object Dagger {
    const val HILT_ANDROID          = "com.google.dagger:hilt-android:${Versions.DAGGER}"
    const val HILT_COMPILER         = "com.google.dagger:hilt-compiler:${Versions.DAGGER}"
    const val HILT_ANDROID_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.DAGGER}"
    const val HILT_ANDROID_TESTING  = "com.google.dagger:hilt-android-testing:${Versions.DAGGER}"
}

object Squareup {
    const val RETROFIT                 = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val RETROFIT_CONVERTER_MOSHI = "com.squareup.retrofit2:converter-moshi:${Versions.RETROFIT}"
    const val MOSHI_KOTLIN             = "com.squareup.moshi:moshi-kotlin:${Versions.MOSHI}"
}

object Firebase{
    const val FIREBASE_BOM           = "com.google.firebase:firebase-bom:${Versions.FIREBASE_BOM}"
    const val FIREBASE_CRASHLYTICS   = "com.google.firebase:firebase-crashlytics"
    const val FIREBASE_ANALYTICS     = "com.google.firebase:firebase-analytics"
    const val FIREBASE_DATABASE      = "com.google.firebase:firebase-database"
    const val FIREBASE_FIRESTORE_KTX = "com.google.firebase:firebase-firestore-ktx"
    const val FIREBASE_STORAGE_KTX   = "com.google.firebase:firebase-storage-ktx"
    const val FIREBASE_AUTH_KTX      = "com.google.firebase:firebase-auth"
}

object Google{
    const val IDENTITY_GOOGLEID = "com.google.android.libraries.identity.googleid:googleid:1.1.1"
    const val PLAY_SERVICES_LOCATION = "com.google.android.gms:play-services-location:21.3.0"
    const val PLAY_SERVICES_ADS = "com.google.android.gms:play-services-ads:24.4.0"
}

object Libraries {
    const val MOCKK                 = "io.mockk:mockk:1.13.13"
    const val MOCKITO_JUNIT_JUPITER = "org.mockito:mockito-junit-jupiter:3.9.0"
    const val LOTTIE_COMPOSE        = "com.airbnb.android:lottie-compose:6.5.2"
    const val HUXHORN_SULKY_ULID        = "de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.2.0"
    const val LIBRARIES_IDENTITY_GOOGLEID = "com.google.android.libraries.identity.googleid:googleid:1.1.1"
    const val KAKAO_MAPS = "com.kakao.maps.open:android:2.11.9"
    const val NAVER_MAPS = "com.naver.maps:map-sdk:3.22.1"
    const val KOMORAN = "com.github.shin285:KOMORAN:3.3.9"

    const val JAKEWHARTON_TIMBER = "com.jakewharton.timber:timber:5.0.1"
    const val SHIMMER_COMPOSE = "com.valentinilk.shimmer:compose-shimmer:1.3.3"
    const val LANDSCAPIST_GLIDE = "com.github.skydoves:landscapist-glide:2.4.0"
    const val FIREBASE_GEOFIRE = "com.firebase:geofire-android:${Versions.FILREBASE_GEOFIRE}"
    const val FIREBASE_GEOFIRE_COMMON = "com.firebase:geofire-android-common:${Versions.FILREBASE_GEOFIRE}"
}

object UnitTest {
    const val JUNIT_VINTAGE_ENGINE      = "org.junit.vintage:junit-vintage-engine:${Versions.JUPITER}"
    const val JUNIT_JUPITER             = "org.junit.jupiter:junit-jupiter:${Versions.JUPITER}"
    const val JUNIT_JUPITER_API         = "org.junit.jupiter:junit-jupiter-api:${Versions.JUPITER}"
    const val JUNIT_JUPITER_ENGINE      = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUPITER}"
    const val JUNIT_JUPITER_PARAMS      = "org.junit.jupiter:junit-jupiter-params:${Versions.JUPITER}"

    const val JUNIT5_TEST_CORE          = "de.mannodermaus.junit5:android-test-core:${Versions.JUNIT5_TEST}"
    const val JUNIT5_TEST_RUNNER        = "de.mannodermaus.junit5:android-test-runner:${Versions.JUNIT5_TEST}"

    const val JETBRAINS_KOTLINX_COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2"
    const val CASH_TURBINE = "app.cash.turbine:turbine:1.2.1"
}

object AndroidTest {
    const val ANDROID_JUNIT = "androidx.test.ext:junit:${Versions.ANDROID_JUNIT}"
    const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:${Versions.ESPRESSO_CORE}"
}