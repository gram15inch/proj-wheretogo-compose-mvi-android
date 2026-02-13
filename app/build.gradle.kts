import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Properties

plugins {
    id("wheretogo.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("de.mannodermaus.android-junit5")
}

android {
    namespace = "com.dhkim139.wheretogo"

    signingConfigs {
        create("release") {
            keyAlias = getUploadKey("keyAlias")
            keyPassword = getUploadKey("keyPassword")
            storeFile = File(getUploadKey("storeFile"))
            storePassword = getUploadKey("storePassword")
        }
    }

    defaultConfig {
        applicationId = "com.dhkim139.wheretogo"
        versionCode = 41
        versionName = "1.0-rc12"

        testInstrumentationRunner = "com.dhkim139.wheretogo.TestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["adsMobAppId"] = getLocalProperties("adsMobAppId")
        manifestPlaceholders["naverMapClientId"] = getLocalProperties("naverMapClientId")

        buildConfigField("String",  "API_ACCESS_KEY", getLocalProperties("apiAccessKey"))

        buildConfigField("String", "NAVER_MAPS_APIGW_CLIENT_ID_KEY", getLocalProperties("naverMapsApigwClientId"))
        buildConfigField("String", "NAVER_MAPS_APIGW_CLIENT_SECRET_KEY", getLocalProperties("naverMapsApigwClientSecret"))
        buildConfigField("String", "NAVER_CLIENT_ID_KEY", getLocalProperties("naverClientId"))
        buildConfigField("String", "NAVER_CLIENT_SECRET_KEY", getLocalProperties("naverClientSecret"))

        buildConfigField( "String", "TMAP_APP_KEY", getLocalProperties("tmapApp"))

        buildConfigField( "String", "NATIVE_AD_ID", getLocalProperties("nativeAdId"))

        buildConfigField("String", "NAVER_MAPS_NTRUSS_APIGW_URL", getLocalProperties("NAVER_MAPS_NTRUSS_APIGW_URL"))
        buildConfigField("String", "NAVER_OPEN_API_URL", getLocalProperties("NAVER_OPEN_API_URL"))
        buildConfigField("String", "FIREBASE_CLOUD_API_URL", getLocalProperties("FIREBASE_CLOUD_API_URL"))
        buildConfigField("String", "FIREBASE_CLOUD_STAGING_API_URL", getLocalProperties("FIREBASE_CLOUD_STAGING_API_URL"))
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("String",  "BuildType", "\"debug\"")
            buildConfigField("String",  "GOOGLE_WEB_CLIENT_ID_KEY", getLocalProperties("googleStagingWebClientId"))
       }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            buildConfigField("String",  "BuildType", "\"release\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID_KEY", getLocalProperties("googleWebClientId"))
        }
    }

    buildFeatures {
        compose = true
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

    // Kotlin (BOM)
    implementation(platform(libs.kotlin.bom))

    // Compose (BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Firebase (BOM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences.core.jvm)

    // Hilt compiler (AndroidX)
    ksp(libs.androidx.hilt.compiler)

    // Dagger / Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    kspTest(libs.dagger.hilt.android.compiler)
    kspAndroidTest(libs.dagger.hilt.android.compiler)


    // Libraries
    implementation(libs.timber)

    // Android Test
    androidTestImplementation(libs.dagger.hilt.android.testing)
    androidTestImplementation(libs.google.identity.googleid)
    androidTestImplementation(libs.lottie.compose)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Unit Test
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.dagger.hilt.android.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.cash.turbine)
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

