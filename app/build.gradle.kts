import com.dhkim139.wheretogo.AndroidX
import com.dhkim139.wheretogo.Versions
import com.dhkim139.wheretogo.Kotlin
import com.dhkim139.wheretogo.UnitTest
import com.dhkim139.wheretogo.AndroidTest
import com.dhkim139.wheretogo.Google
import com.dhkim139.wheretogo.Libraries
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("de.mannodermaus.android-junit5")
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
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    junitPlatform {
        instrumentationTests.includeExtensions.set(true)
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

    implementation(Google.HILT_ANDROID)
    kapt (Google.HILT_COMPILER)

    testImplementation (Google.HILT_ANDROID_TESTING)

    kaptTest (Google.HILT_ANDROID_COMPILER)

    androidTestImplementation (Google.HILT_ANDROID_TESTING)

    kaptAndroidTest (Google.HILT_ANDROID_COMPILER)
    testImplementation(Libraries.MOCKK)


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
    androidTestImplementation(AndroidX.COMPOSE_UI_TEST_JUNIT4)

    debugImplementation(AndroidX.COMPOSE_UI_TOOL)
    debugImplementation(AndroidX.COMPOSE_UI_TEST_MANIFEST)
}

kapt {
    correctErrorTypes = true
}

tasks.withType(Test::class) {
    useJUnitPlatform()
    testLogging {
        events.addAll(arrayOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED))
    }
}

