import org.gradle.api.tasks.testing.logging.TestLogEvent
import wheretogo.AndroidConfig
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
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        applicationId = "com.dhkim139.wheretogo"
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.TARGET_SDK
        versionCode = 38
        versionName = "1.0-rc9"

        testInstrumentationRunner = "com.dhkim139.wheretogo.TestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["adsMobAppId"] = getLocalProperties("adsMobAppId")
        manifestPlaceholders["naverMapClientId"] = getLocalProperties("naverMapClientId")
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
        debug {
            applicationIdSuffix = ".debug"
            buildConfigField( "Boolean", "CRASHLYTICS", "false")
            buildConfigField("String",  "GOOGLE_WEB_CLIENT_ID_KEY", getLocalProperties("googleStagingWebClientId"))
            buildConfigField("String",  "API_ACCESS_KEY", getLocalProperties("apiAccessKey"))

        }
        create("qa") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true

            buildConfigField( "Boolean", "CRASHLYTICS", "false")
            buildConfigField("String","GOOGLE_WEB_CLIENT_ID_KEY", getLocalProperties("googleWebClientId"))
            buildConfigField("String",  "API_ACCESS_KEY", getLocalProperties("apiAccessKey"))
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            buildConfigField( "Boolean", "CRASHLYTICS", "true")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID_KEY", getLocalProperties("googleWebClientId"))
            buildConfigField("String",  "API_ACCESS_KEY", getLocalProperties("apiAccessKey"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) }
    )
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

    // Dagger
    implementation(Dagger.HILT_ANDROID)
    ksp(Dagger.HILT_COMPILER)
    kspTest(Dagger.HILT_ANDROID_COMPILER)
    kspAndroidTest(Dagger.HILT_ANDROID_COMPILER)

    // Firebase
    implementation(platform(Firebase.FIREBASE_BOM))
    implementation(Firebase.FIREBASE_CRASHLYTICS)
    implementation(Firebase.FIREBASE_ANALYTICS)
    implementation(Firebase.FIREBASE_FIRESTORE_KTX)
    implementation(Firebase.FIREBASE_STORAGE_KTX)
    implementation(Firebase.FIREBASE_AUTH_KTX)

    // Libraries
    implementation(Libraries.JAKEWHARTON_TIMBER)

    // Test
    androidTestImplementation(Dagger.HILT_ANDROID_TESTING)
    androidTestImplementation(UnitTest.JUNIT_JUPITER_API)
    androidTestImplementation(UnitTest.JUNIT_JUPITER_PARAMS)
    androidTestImplementation(UnitTest.JUNIT_JUPITER_ENGINE)
    androidTestImplementation(UnitTest.JUNIT_VINTAGE_ENGINE)
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



    testImplementation(UnitTest.JUNIT_JUPITER_PARAMS)
    testImplementation(UnitTest.JUNIT_JUPITER_ENGINE)
    testImplementation(UnitTest.JUNIT_VINTAGE_ENGINE)
    testImplementation(UnitTest.JETBRAINS_KOTLINX_COROUTINES_TEST)
    testImplementation(UnitTest.CASH_TURBINE)
    testImplementation(Libraries.NAVER_MAPS)
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

fun getLocalProperties(key: String): String {
    val localPropertiesFile = rootProject.file("local.properties")
    val properties = Properties()

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }

    return properties[key].toString()
}

