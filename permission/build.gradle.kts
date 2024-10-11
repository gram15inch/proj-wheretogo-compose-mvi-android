import org.gradle.api.tasks.testing.logging.TestLogEvent
import wheretogo.AndroidTest
import wheretogo.AndroidX
import wheretogo.Google
import wheretogo.Kotlin
import wheretogo.Libraries
import wheretogo.UnitTest

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("de.mannodermaus.android-junit5")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.wheretogo.permission"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(AndroidX.LIFECYCLE_RUNTIME_KTX)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL_COMPOSE)
    implementation(AndroidX.ACTIVITY_COMPOSE)
    implementation(platform(AndroidX.COMPOSE_BOM))


    //compose
    implementation(AndroidX.COMPOSE_UI)
    implementation(AndroidX.COMPOSE_UI_GRAPHICS)
    implementation(AndroidX.COMPOSE_UI_TOOL_PREVIEW)
    implementation(AndroidX.COMPOSE_MATERIAL3)

    implementation(Libraries.LOTTIE_COMPOSE)
    androidTestImplementation(AndroidX.COMPOSE_UI_TEST_JUNIT4)
    androidTestImplementation(platform(AndroidX.COMPOSE_BOM))
    androidTestImplementation(Libraries.LOTTIE_COMPOSE)

    debugImplementation(AndroidX.COMPOSE_UI_TOOL)
    debugImplementation(AndroidX.COMPOSE_UI_TEST_MANIFEST)

    //hilt
    implementation(Google.HILT_ANDROID)
    implementation(AndroidX.HILT_NAVIGATION_COMPOSE)
    testImplementation (Google.HILT_ANDROID_TESTING)
    testImplementation(Libraries.MOCKK)

    androidTestImplementation (Google.HILT_ANDROID_TESTING)

    ksp(Google.HILT_COMPILER)
    kspTest (Google.HILT_ANDROID_COMPILER)
    kspAndroidTest (Google.HILT_ANDROID_COMPILER)

    // junit5
    testImplementation (UnitTest.JUNIT_JUPITER_API)
    testRuntimeOnly (UnitTest.JUNIT_JUPITER_ENGINE)
    testImplementation (UnitTest.JUNIT_JUPITER_PARAMS)

    androidTestImplementation (AndroidX.TEST_RUNNER)
    androidTestImplementation (UnitTest.JUNIT_JUPITER_API)

    androidTestImplementation (UnitTest.JUNIT5_TEST_CORE)
    androidTestRuntimeOnly (UnitTest.JUNIT5_TEST_RUNNER)

    testImplementation(UnitTest.JUNIT)
    testImplementation(UnitTest.JUNIT_VINTAGE_ENGINE)

    androidTestImplementation(AndroidTest.ANDROID_JUNIT)
    androidTestImplementation(AndroidTest.ESPRESSO_CORE)
    androidTestImplementation(platform(AndroidX.COMPOSE_BOM))

    //etc
    implementation("com.google.android.gms:play-services-location:21.3.0")
}

tasks.withType(Test::class) {
    useJUnitPlatform()
    testLogging {
        events.addAll(arrayOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED))
    }
}