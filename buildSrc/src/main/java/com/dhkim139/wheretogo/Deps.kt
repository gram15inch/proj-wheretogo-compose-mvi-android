package com.dhkim139.wheretogo


object Versions {

    const val COMPILE_SDK_VERSION = 34
    const val MIN_SDK_VERSION     = 24
    const val TARGET_SDK_VERSION  = 34
    const val VERSION_CODE        = 1
    const val VERSION_NAME        = "1.0"

    const val KOTLIN_COMPILER_EXTENSION_VERSION = "1.3.2"


    const val KOTLIN_VERSION     = "1.8.0"
    const val KOTLINX_COROUTINES = "1.6.1"
    const val BUILD_GRADLE       = "4.2.1"
    const val APP_COMPAT         = "1.6.1"

    const val CORE_KTX      = "1.8.0"
    const val ACTIVITY_KTX  = "1.2.3"
    const val FRAGMENT_KTX  = "1.3.4"
    const val LIFECYCLE_KTX = "2.8.4"

    const val ROOM = "2.5.0"
    const val TEST = "1.5.2"

    const val NAVIGATION = "2.5.3"

    const val COMPOSE = "1.9.1"
    const val BOM = "2022.10.00"

    const val CONSTRAINT_LAYOUT = "2.1.4"
    const val LEGACY            = "1.0.0"
    const val ANNOTATION        = "1.5.0"

    const val HILT     = "2.44"
    const val MATERIAL = "1.8.0"

    const val RETROFIT             = "2.9.0"
    const val RETROFIT_JAKEWHARTON = "0.9.2"

    const val OKHTTP = "5.0.0-alpha.2"


    const val MOSHI = "1.13.0"



    const val MOCKK      = "1.10.5"
    const val TIMBER     = "5.0.1"
    const val AMARJAIN07 = "1.0.3"
    const val GLIDE      = "4.11.0"



    const val JUNIT         = "4.13.2"
    const val JUPITER       = "5.9.0"
    const val JUNIT5        = "1.3.0"
    const val ANDROID_JUNIT = "1.1.5"
    const val ESPRESSO_CORE = "3.5.1"
}

object Kotlin{
    const val KOTLIN_STDLIB      = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN_VERSION}"
    const val KOTLIN_BOM         = "org.jetbrains.kotlin:kotlin-bom:${Versions.KOTLIN_VERSION}"
    const val COROUTINES_CORE    = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLINX_COROUTINES}"
    const val COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.KOTLINX_COROUTINES}"
    const val COROUTINES_TEST    = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.KOTLINX_COROUTINES}"
}

object AndroidX {
    const val CORE_KTX                = "androidx.core:core-ktx:${Versions.CORE_KTX}"//
    const val APP_COMPAT              = "androidx.appcompat:appcompat:${Versions.APP_COMPAT}"//

    const val ACTIVITY_KTX            = "androidx.activity:activity-ktx:${Versions.ACTIVITY_KTX}"
    const val FRAGMENT_KTX            = "androidx.fragment:fragment-ktx:${Versions.FRAGMENT_KTX}"

    const val LIFECYCLE_RUNTIME_KTX             = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE_KTX}"
    const val LIFECYCLE_VIEWMODEL_KTX = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE_KTX}"
    const val LIFECYCLE_LIVEDATA_KTX  = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.LIFECYCLE_KTX}"

    const val ROOM_RUNTIME            = "androidx.room:room-runtime:${Versions.ROOM}"
    const val ROOM_COMPILER           = "androidx.room:room-compiler:${Versions.ROOM}"
    const val ROOM_KTX                = "androidx.room:room-ktx:${Versions.ROOM}"
    const val ROOM_PAGING             = "androidx.room:room-paging:${Versions.ROOM}"


    const val NAVIGATION_FRAGMENT                   = "androidx.navigation:navigation-fragment:${Versions.NAVIGATION}"
    const val NAVIGATION_UI                         = "androidx.navigation:navigation-ui:${Versions.NAVIGATION}"
    const val NAVIGATION_FRAGMENT_KTX               = "androidx.navigation:navigation-fragment-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_UI_KTX                     = "androidx.navigation:navigation-ui-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_DYNAMIC_FEATURES_FRAGMENT  = "androidx.navigation:navigation-dynamic-features-fragment:${Versions.NAVIGATION}"
    const val NAVIGATION_TESTING                    = "androidx.navigation:navigation-testing:${Versions.NAVIGATION}"
    const val NAVIGATION_COMPOSE                    = "androidx.navigation:navigation-compose:${Versions.NAVIGATION}"


    const val ACTIVITY_COMPOSE  = "androidx.activity:activity-compose:${Versions.COMPOSE}"

    const val COMPOSE_BOM              = "androidx.compose:compose-bom:${Versions.BOM}"
    const val COMPOSE_UI               = "androidx.compose.ui:ui"
    const val COMPOSE_UI_GRAPHICS      = "androidx.compose.ui:ui-graphics"
    const val COMPOSE_UI_TOOL_PREVIEW  = "androidx.compose.ui:ui-tooling-preview"
    const val COMPOSE_UI_TOOL          = "androidx.compose.ui:ui-tooling"
    const val COMPOSE_UI_TEST_JUNIT4   = "androidx.compose.ui:ui-test-junit4"
    const val COMPOSE_UI_TEST_MANIFEST = "androidx.compose.ui:ui-test-manifest"
    const val COMPOSE_MATERIAL3        = "androidx.compose.material3:material3"

    const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.CONSTRAINT_LAYOUT}"//
    const val LEGACY_SUPPORT_V4 = "androidx.legacy:legacy-support-v4:${Versions.LEGACY}"//
    const val ANNOTATION        = "androidx.annotation:annotation:${Versions.ANNOTATION}"//1.5.0

    const val TEST_RUNNER       ="androidx.test:runner:${Versions.TEST}"//

}

object Google {
    const val HILT_ANDROID          = "com.google.dagger:hilt-android:${Versions.HILT}"//
    const val HILT_COMPILER         = "com.google.dagger:hilt-compiler:${Versions.HILT}"//
    const val HILT_ANDROID_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"//
    const val HILT_ANDROID_TESTING  = "com.google.dagger:hilt-android-testing:${Versions.HILT}"//

    const val MATERIAL = "com.google.android.material:material:${Versions.MATERIAL}"//
}

object Libraries {
    const val RETROFIT                           = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"//
    const val RETROFIT_CONVERTER_GSON            = "com.squareup.retrofit2:converter-gson:${Versions.RETROFIT}"//
    const val RETROFIT_CONVERTER_MOSHI           = "com.squareup.retrofit2:converter-moshi:${Versions.RETROFIT}"//
    const val RETROFIT_KOTLIN_COROUTINES_ADAPTER = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:${Versions.RETROFIT_JAKEWHARTON}"//

    const val OKHTTP                     = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"//
    const val OKHTTP_LOGGING_INTERCEPTOR = "com.squareup.okhttp3:logging-interceptor:${Versions.OKHTTP}"//



    const val MOSHI          = "com.squareup.moshi:moshi:${Versions.MOSHI}"
    const val MOSHI_KOTLIN   = "com.squareup.moshi:moshi-kotlin:${Versions.MOSHI}"
    const val MOSHI_ADAPTERS = "com.squareup.moshi:moshi-adapters:${Versions.MOSHI}"
    const val MOSHI_CODEGEN  = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.MOSHI}"



    const val MOCKK = "io.mockk:mockk:${Versions.MOCKK}"
    const val GLIDE = "com.github.bumptech.glide:glide:${Versions.GLIDE}"
}


object UnitTest {
    const val JUNIT                     = "junit:junit:${Versions.JUNIT}"
    const val JUNIT_JUPITER_API         = "org.junit.jupiter:junit-jupiter-api:${Versions.JUPITER}"
    const val JUNIT_JUPITER_ENGINE      = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUPITER}"
    const val JUNIT_JUPITER_PARAMS      = "org.junit.jupiter:junit-jupiter-params:${Versions.JUPITER}"

    const val JUNIT5_TEST_CORE          = "de.mannodermaus.junit5:android-test-core:${Versions.JUNIT5}"
    const val JUNIT5_TEST_RUNNER        = "de.mannodermaus.junit5:android-test-runner:${Versions.JUNIT5}"
}

object AndroidTest {
    const val ANDROID_JUNIT = "androidx.test.ext:junit:${Versions.ANDROID_JUNIT}"
    const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:${Versions.ESPRESSO_CORE}"
}