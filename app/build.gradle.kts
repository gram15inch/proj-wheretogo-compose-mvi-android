import org.gradle.api.tasks.testing.logging.TestLogEvent
import wheretogo.AndroidX
import wheretogo.Firebase
import wheretogo.Dagger
import wheretogo.Kotlin
import wheretogo.Compose
import wheretogo.Libraries
import wheretogo.UnitTest
import wheretogo.Versions
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("de.mannodermaus.android-junit5")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.dhkim139.wheretogo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dhkim139.wheretogo"
        minSdk = 24
        targetSdk = 34
        versionCode = 20
        versionName = "1.2.0-rc9"

        testInstrumentationRunner = "com.dhkim139.wheretogo.TestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = getUploadKey("keyAlias")
            keyPassword = getUploadKey("keyPassword")
            storeFile = File(getUploadKey("storeFile"))
            storePassword = getUploadKey("storePassword")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    implementation(project(mapOf("path" to ":presentation")))
    implementation(project(mapOf("path" to ":data")))
    implementation(project(mapOf("path" to ":domain")))

    implementation(platform(Kotlin.KOTLIN_BOM))

    // AndroidX
    implementation(AndroidX.CORE_KTX)
    implementation(AndroidX.LIFECYCLE_RUNTIME_KTX)
    implementation(AndroidX.WORK_RUNTIME_KTX)
    implementation(AndroidX.TEST_CORE_KTX)
    implementation(AndroidX.HILT_COMMON)
    implementation(AndroidX.HILT_WORK)
    implementation(AndroidX.HILT_NAVIGATION_COMPOSE)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL_COMPOSE)
    implementation(AndroidX.ACTIVITY_COMPOSE)
    implementation(AndroidX.DATASTORE_PREFERENCE_CORE)
    ksp(AndroidX.HILT_COMPILER)

    // Compose
    implementation(platform(Compose.COMPOSE_BOM))
    implementation(Compose.COMPOSE_UI)
    implementation(Compose.COMPOSE_UI_GRAPHICS)
    implementation(Compose.COMPOSE_UI_TOOL_PREVIEW)
    implementation(Compose.COMPOSE_MATERIAL3)
    debugImplementation(Compose.COMPOSE_UI_TOOL)
    debugImplementation(Compose.COMPOSE_UI_TEST_MANIFEST)

    // Libraries
    implementation(Libraries.LOTTIE_COMPOSE)


    // Dagger
    implementation(Dagger.HILT_ANDROID)
    ksp(Dagger.HILT_COMPILER)

    // Firebase
    implementation(platform(Firebase.FIREBASE_BOM))
    implementation(Firebase.FIREBASE_CRASHLYTICS)
    implementation(Firebase.FIREBASE_ANALYTICS)
    implementation(Firebase.FIREBASE_FIRESTORE_KTX)
    implementation(Firebase.FIREBASE_STORAGE_KTX)
    implementation(Firebase.FIREBASE_AUTH_KTX)

    // Test
    androidTestImplementation(Dagger.HILT_ANDROID_TESTING)
    androidTestImplementation(UnitTest.JUNIT_JUPITER_API)
    androidTestImplementation(UnitTest.JUNIT_JUPITER_PARAMS)
    androidTestImplementation(UnitTest.JUNIT_JUPITER_ENGINE)
    androidTestImplementation(UnitTest.JUNIT_VINTAGE_ENGINE)
    androidTestImplementation(Libraries.HUXHORN_SULKY_ULID)
    androidTestImplementation(Libraries.LIBRARIES_IDENTITY_GOOGLEID)
    androidTestImplementation(Libraries.LOTTIE_COMPOSE)
    androidTestImplementation(Compose.COMPOSE_UI_TEST_JUNIT4)
    androidTestImplementation(AndroidX.TEST_UIAUTOMATOR)
    androidTestImplementation(AndroidX.TEST_RUNNER)
    androidTestImplementation(platform(Compose.COMPOSE_BOM))

    testImplementation(UnitTest.JUNIT_JUPITER_API)
    testImplementation(UnitTest.JUNIT_JUPITER_PARAMS)
    testImplementation(UnitTest.JUNIT_JUPITER_ENGINE)
    testImplementation(UnitTest.JUNIT_VINTAGE_ENGINE)
    testImplementation(Dagger.HILT_ANDROID_TESTING)
    testImplementation(Libraries.MOCKK)

    kspTest(Dagger.HILT_ANDROID_COMPILER)
    kspAndroidTest(Dagger.HILT_ANDROID_COMPILER)
}

tasks.withType(Test::class) {
    useJUnitPlatform()
    testLogging {
        events.addAll(arrayOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED))
    }
}

fun getUploadKey(propertyKey: String): String {
    val propertiesFile = File(rootProject.projectDir, "keystore.properties")
    val properties = Properties()

    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { properties.load(it) }
    } else {
        propertiesFile.createNewFile()
    }

    val defaultValue = "yourUploadKey"

    if (!properties.containsKey(propertyKey)) {
        properties[propertyKey] = defaultValue
        propertiesFile.outputStream().use { properties.store(it, null) }
    }
    return properties[propertyKey].toString()
}

