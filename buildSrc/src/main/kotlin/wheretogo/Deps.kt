package wheretogo


object Versions {

    const val KOTLIN_COMPILER_EXTENSION_VERSION = "1.5.15"

    const val KOTLIN_VERSION     = "2.0.0"
    const val CORE      = "1.8.0"

    const val LIFECYCLE = "2.8.5"

    const val TEST = "1.5.2"

    const val ACTIVITY = "1.9.2"
    const val BOM = "2024.09.01"

    const val HILT     = "2.52"

    const val RETROFIT     = "2.9.0"
    const val MOSHI     = "1.13.0"


    const val MOCKK      = "1.10.5"
    const val Lottie      = "6.5.2"



    const val JUNIT         = "4.13.2"
    const val JUPITER       = "5.10.2"
    const val JUNIT5_TEST   = "1.4.0"
    const val ANDROID_JUNIT = "1.1.5"
    const val ESPRESSO_CORE = "3.5.1"
}

object Kotlin {
    const val KOTLIN_BOM = "org.jetbrains.kotlin:kotlin-bom:${Versions.KOTLIN_VERSION}"
}

object AndroidX {
    const val CORE_KTX                = "androidx.core:core-ktx:${Versions.CORE}"//


    const val LIFECYCLE_RUNTIME_KTX   = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}"
    const val LIFECYCLE_VIEWMODEL_COMPOSE   = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0"



    const val ACTIVITY_COMPOSE  = "androidx.activity:activity-compose:${Versions.ACTIVITY}"

    const val COMPOSE_BOM              = "androidx.compose:compose-bom:${Versions.BOM}"
    const val COMPOSE_UI               = "androidx.compose.ui:ui"
    const val COMPOSE_UI_GRAPHICS      = "androidx.compose.ui:ui-graphics"
    const val COMPOSE_UI_TOOL_PREVIEW  = "androidx.compose.ui:ui-tooling-preview"
    const val COMPOSE_UI_TOOL          = "androidx.compose.ui:ui-tooling"
    const val COMPOSE_UI_TEST_JUNIT4   = "androidx.compose.ui:ui-test-junit4"
    const val COMPOSE_UI_TEST_MANIFEST = "androidx.compose.ui:ui-test-manifest"
    const val COMPOSE_MATERIAL3        = "androidx.compose.material3:material3"

    const val HILT_NAVIGATION_COMPOSE = "androidx.hilt:hilt-navigation-compose:1.0.0"

    const val TEST_RUNNER       ="androidx.test:runner:${Versions.TEST}"
}

object Google {
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
    const val MOCKK = "io.mockk:mockk:${Versions.MOCKK}"
    const val LOTTIE_COMPOSE = "com.airbnb.android:lottie-compose:${Versions.Lottie}"

}


object UnitTest {
    const val JUNIT                     = "junit:junit:${Versions.JUNIT}"
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