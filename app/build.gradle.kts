import org.gradle.api.tasks.testing.logging.TestLogEvent
import wheretogo.AndroidX
import wheretogo.Firebase
import wheretogo.Dagger
import wheretogo.Kotlin
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
        versionCode = 6
        versionName = "1.1.3"

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
            isMinifyEnabled = false
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

    implementation(AndroidX.CORE_KTX)
    implementation(AndroidX.LIFECYCLE_RUNTIME_KTX)
    implementation(AndroidX.WORK_RUNTIME_KTX)
    implementation(AndroidX.TEST_CORE_KTX)

    implementation(platform(Kotlin.KOTLIN_BOM))
    implementation(platform(AndroidX.COMPOSE_BOM))
    implementation("androidx.datastore:datastore-preferences-core-jvm:1.1.2")
    androidTestImplementation(AndroidX.TEST_RUNNER)
    androidTestImplementation(platform(AndroidX.COMPOSE_BOM))

    //compose
    implementation(AndroidX.COMPOSE_UI)
    implementation(AndroidX.COMPOSE_UI_GRAPHICS)
    implementation(AndroidX.COMPOSE_UI_TOOL_PREVIEW)
    implementation(AndroidX.COMPOSE_MATERIAL3)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL_COMPOSE)
    implementation(AndroidX.ACTIVITY_COMPOSE)

    implementation(Libraries.LOTTIE_COMPOSE)

    androidTestImplementation(AndroidX.COMPOSE_UI_TEST_JUNIT4)
    androidTestImplementation(Libraries.LOTTIE_COMPOSE)

    debugImplementation(AndroidX.COMPOSE_UI_TOOL)
    debugImplementation(AndroidX.COMPOSE_UI_TEST_MANIFEST)


    //hilt
    implementation(Dagger.HILT_ANDROID)
    implementation(AndroidX.HILT_COMMON)
    implementation(AndroidX.HILT_WORK)
    implementation(AndroidX.HILT_NAVIGATION_COMPOSE)
    testImplementation(Libraries.MOCKK)

    ksp(AndroidX.HILT_COMPILER)
    ksp(Dagger.HILT_COMPILER)

    testImplementation(Dagger.HILT_ANDROID_TESTING)
    kspTest(Dagger.HILT_ANDROID_COMPILER)
    androidTestImplementation(Dagger.HILT_ANDROID_TESTING)
    kspAndroidTest(Dagger.HILT_ANDROID_COMPILER)

    // test
    androidTestImplementation(UnitTest.JUNIT_JUPITER_API)
    androidTestImplementation(UnitTest.JUNIT_JUPITER_PARAMS)
    androidTestImplementation(UnitTest.JUNIT_JUPITER_ENGINE)
    androidTestImplementation(UnitTest.JUNIT_VINTAGE_ENGINE)
    testImplementation(UnitTest.JUNIT_JUPITER_API)
    testImplementation(UnitTest.JUNIT_JUPITER_PARAMS)
    testImplementation(UnitTest.JUNIT_JUPITER_ENGINE)
    testImplementation(UnitTest.JUNIT_VINTAGE_ENGINE)

    // firebase
    implementation(platform(Firebase.FIREBASE_BOM))
    implementation(Firebase.FIREBASE_CRASHLYTICS)
    implementation(Firebase.FIREBASE_ANALYTICS)
    implementation(Firebase.FIREBASE_FIRESTORE_KTX)
    implementation(Firebase.FIREBASE_STORAGE_KTX)

    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
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

