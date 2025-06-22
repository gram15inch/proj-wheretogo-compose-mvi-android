import wheretogo.AndroidConfig
import wheretogo.AndroidX
import wheretogo.Firebase
import wheretogo.Dagger
import wheretogo.Google
import wheretogo.Kotlin
import wheretogo.Libraries
import wheretogo.Squareup
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.wheretogo.data"
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        consumerProguardFiles(
            "consumer-default-rules.pro",
            "consumer-retrofit-rules.pro"
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    defaultConfig {
        buildConfigField( "String", "NAVER_MAPS_APIGW_CLIENT_ID_KEY", getAppKey("naverMapsApigwClientId"))
        buildConfigField( "String", "NAVER_MAPS_APIGW_CLIENT_SECRET_KEY", getAppKey("naverMapsApigwClientSecret"))
        buildConfigField( "String", "NAVER_CLIENT_ID_KEY", getAppKey("naverClientId"))
        buildConfigField( "String", "NAVER_CLIENT_SECRET_KEY", getAppKey("naverClientSecret"))

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
}

dependencies {
    implementation(project(mapOf("path" to ":domain")))

    // Bom
    implementation(platform(Kotlin.KOTLIN_BOM))

    // Androidx
    api(AndroidX.ROOM_KTX)
    implementation(AndroidX.CORE_KTX)
    implementation(AndroidX.EXIFINTERFACE)
    implementation(AndroidX.DATASTORE_PREFERENCES)

    // Dagger
    implementation(Dagger.HILT_ANDROID)
    ksp(Dagger.HILT_COMPILER)

    // Retrofit
    api(Squareup.RETROFIT)
    api(Squareup.RETROFIT_CONVERTER_MOSHI)
    api(Squareup.MOSHI_KOTLIN)

    // Room
    api(AndroidX.ROOM_RUNTIME)
    ksp(AndroidX.ROOM_COMPILER)

    // Firebase
    implementation(platform(Firebase.FIREBASE_BOM))
    implementation(Firebase.FIREBASE_CRASHLYTICS)
    implementation(Firebase.FIREBASE_DATABASE)
    implementation(Firebase.FIREBASE_FIRESTORE_KTX)
    implementation(Firebase.FIREBASE_STORAGE_KTX)
    implementation(Firebase.FIREBASE_AUTH_KTX)


    // Libraries
    implementation(Google.IDENTITY_GOOGLEID)
    implementation(Libraries.HUXHORN_SULKY_ULID)
}

fun getAppKey(propertyKey: String): String {
    val propertiesFile = File(rootProject.projectDir, "local.properties")
    val properties = Properties()

    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { properties.load(it) }
    } else {
        propertiesFile.createNewFile()
    }

    val defaultValue = "\"yourAppKey\""

    if (!properties.containsKey(propertyKey)) {
        properties[propertyKey] = defaultValue
        propertiesFile.outputStream().use { properties.store(it, null) }
    }
    return properties[propertyKey].toString()
}
