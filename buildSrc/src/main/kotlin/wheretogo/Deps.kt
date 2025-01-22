package wheretogo


object Versions {

    const val KOTLIN_COMPILER_EXTENSION_VERSION = "1.5.15"

    const val KOTLIN_VERSION     = "2.0.0"
    const val CORE      = "1.13.1"

    const val LIFECYCLE = "2.8.5"

    const val NAVIGATION = "2.8.0"

    const val TEST = "1.6.1"

    const val ACTIVITY = "1.9.2"
    const val COMPOSE_BOM = "2024.09.02"

    const val HILT     = "2.52"

    const val ROOM     = "2.6.1"

    const val RETROFIT     = "2.9.0"
    const val MOSHI     = "1.13.0"


    const val JUNIT         = "4.13.2"
    const val JUPITER       = "5.11.4"
    const val JUNIT5_TEST   = "1.4.0"
    const val ANDROID_JUNIT = "1.1.5"
    const val ESPRESSO_CORE = "3.6.1"


    const val FIREBASE = "33.3.0"
    const val WORK = "2.9.1"
}

object Kotlin {
    const val KOTLIN_BOM = "org.jetbrains.kotlin:kotlin-bom:${Versions.KOTLIN_VERSION}"
}

object AndroidX {
    const val CORE_KTX                = "androidx.core:core-ktx:${Versions.CORE}"


    const val LIFECYCLE_RUNTIME_KTX         = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}"
    const val LIFECYCLE_VIEWMODEL_COMPOSE   = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.LIFECYCLE}"

    const val ACTIVITY_COMPOSE  = "androidx.activity:activity-compose:${Versions.ACTIVITY}"

    const val COMPOSE_BOM              = "androidx.compose:compose-bom:${Versions.COMPOSE_BOM}"
    const val COMPOSE_UI               = "androidx.compose.ui:ui"
    const val COMPOSE_UI_GRAPHICS      = "androidx.compose.ui:ui-graphics"
    const val COMPOSE_UI_TOOL_PREVIEW  = "androidx.compose.ui:ui-tooling-preview"
    const val COMPOSE_UI_TOOL          = "androidx.compose.ui:ui-tooling"
    const val COMPOSE_UI_TEST_JUNIT4   = "androidx.compose.ui:ui-test-junit4"
    const val COMPOSE_UI_TEST_MANIFEST = "androidx.compose.ui:ui-test-manifest"
    const val COMPOSE_MATERIAL3        = "androidx.compose.material3:material3"
    const val NAVIGATION_COMPOSE        = "androidx.navigation:navigation-compose:${Versions.NAVIGATION}"

    const val HILT_NAVIGATION_COMPOSE   = "androidx.hilt:hilt-navigation-compose:1.2.0"
    const val HILT_COMPILER             = "androidx.hilt:hilt-compiler:1.2.0"
    const val HILT_COMMON               = "androidx.hilt:hilt-common:1.2.0"
    const val HILT_WORK                 = "androidx.hilt:hilt-work:1.2.0"

    const val ROOM_RUNTIME      ="androidx.room:room-runtime:${Versions.ROOM}"
    const val ROOM_KTX          ="androidx.room:room-ktx:${Versions.ROOM}"
    const val ROOM_COMPILER     ="androidx.room:room-compiler:${Versions.ROOM}"
    const val ROOM_TESTING      ="androidx.room:room-testing:${Versions.ROOM}"


    const val WORK_RUNTIME_KTX      ="androidx.work:work-runtime-ktx:${Versions.WORK}"


    const val TEST_RUNNER       = "androidx.test:runner:${Versions.TEST}"
    const val TEST_CORE_KTX     = "androidx.test:core-ktx:${Versions.TEST}"
}

object Dagger {
    const val HILT_ANDROID          = "com.google.dagger:hilt-android:${Versions.HILT}"
    const val HILT_COMPILER         = "com.google.dagger:hilt-compiler:${Versions.HILT}"
    const val HILT_ANDROID_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
    const val HILT_ANDROID_TESTING  = "com.google.dagger:hilt-android-testing:${Versions.HILT}"

}

object Squareup {
    const val RETROFIT                 = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val RETROFIT_CONVERTER_MOSHI = "com.squareup.retrofit2:converter-moshi:${Versions.RETROFIT}"
    const val MOSHI_KOTLIN             = "com.squareup.moshi:moshi-kotlin:${Versions.MOSHI}"
}

object Libraries {
    const val MOCKK = "io.mockk:mockk:1.10.5"
    const val MOCKITO_JUNIT_JUPITER = "org.mockito:mockito-junit-jupiter:3.9.0"
    const val LOTTIE_COMPOSE = "com.airbnb.android:lottie-compose:6.5.2"
}


object UnitTest {
    const val JUNIT_VINTAGE_ENGINE      = "org.junit.vintage:junit-vintage-engine:${Versions.JUPITER}"
    const val JUNIT_JUPITER_API         = "org.junit.jupiter:junit-jupiter-api:${Versions.JUPITER}"
    const val JUNIT_JUPITER_ENGINE      = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUPITER}"
    const val JUNIT_JUPITER_PARAMS      = "org.junit.jupiter:junit-jupiter-params:${Versions.JUPITER}"

    const val JUNIT5_TEST_CORE          = "de.mannodermaus.junit5:android-test-core:${Versions.JUNIT5_TEST}"
    const val JUNIT5_TEST_RUNNER        = "de.mannodermaus.junit5:android-test-runner:${Versions.JUNIT5_TEST}"
}

object AndroidTest {
    const val ANDROID_JUNIT = "androidx.test.ext:junit:${Versions.ANDROID_JUNIT}"
    const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:${Versions.ESPRESSO_CORE}"
}

object Firebase{
    const val FIREBASE_BOM = "com.google.firebase:firebase-bom:${Versions.FIREBASE}"
    const val FIREBASE_CRASHLYTICS = "com.google.firebase:firebase-crashlytics"
    const val FIREBASE_ANALYTICS = "com.google.firebase:firebase-analytics"
    const val FIREBASE_DATABASE = "com.google.firebase:firebase-database"
    const val FIREBASE_FIRESTORE_KTX = "com.google.firebase:firebase-firestore-ktx"
    const val FIREBASE_STORAGE_KTX = "com.google.firebase:firebase-storage-ktx"
}