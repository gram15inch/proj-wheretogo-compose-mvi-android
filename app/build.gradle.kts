import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import wheretogo.AndroidX
import wheretogo.Versions
import wheretogo.Kotlin
import wheretogo.UnitTest
import wheretogo.AndroidTest
import wheretogo.Google
import wheretogo.Libraries
import org.gradle.api.tasks.testing.logging.TestLogEvent
import wheretogo.Squareup
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("de.mannodermaus.android-junit5")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.dhkim139.wheretogo"
    compileSdk = 34

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf()
        applicationId = "com.dhkim139.wheretogo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
        vectorDrawables {
            useSupportLibrary = true
        }

    }
    signingConfigs {
        val keystoreProperties = Properties().apply { load(project.rootProject.file("keystore.properties").inputStream()) }
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    defaultConfig {
        buildConfigField( "String", "KAKAO_NATIVE_APP_KEY", getAppKey("kakaoNativeApp"))
        buildConfigField( "String", "KAKAO_REST_API_KEY", getAppKey("kakaoNativeApp"))
        buildConfigField( "String", "KAKAO_ADMIN_KEY", getAppKey("kakaoNativeApp"))

        buildConfigField( "String", "NAVER_CLIENT_ID_KEY", getAppKey("naverClientId"))
        buildConfigField( "String", "NAVER_CLIENT_SECRET_KEY", getAppKey("naverClientSecret"))

        buildConfigField( "String", "TMAP_APP_KEY", getAppKey("tmapApp"))

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
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
    packaging {
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


    //Map
    implementation("com.kakao.maps.open:android:2.11.9")
    implementation("com.naver.maps:map-sdk:3.19.1")
    implementation(files("libs/com.skt.Tmap_1.76.jar"))

    //retrofit
    implementation (Squareup.RETROFIT)
    implementation (Squareup.RETROFIT_CONVERTER_MOSHI)
    implementation(Squareup.MOSHI_KOTLIN)

    //Room
    implementation(AndroidX.ROOM_RUNTIME)
    implementation(AndroidX.ROOM_KTX)
    annotationProcessor(AndroidX.ROOM_COMPILER)
    testImplementation(AndroidX.ROOM_TESTING)
    ksp(AndroidX.ROOM_COMPILER)
}

tasks.withType(Test::class) {
    useJUnitPlatform()
    testLogging {
        events.addAll(arrayOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED))
    }
}

fun getAppKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir, providers).getProperty(propertyKey)
}

