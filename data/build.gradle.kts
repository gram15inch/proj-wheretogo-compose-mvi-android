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
        buildConfigField( "String", "NAVER_MAPS_APIGW_CLIENT_ID_KEY", getLocalProperties("naverMapsApigwClientId"))
        buildConfigField( "String", "NAVER_MAPS_APIGW_CLIENT_SECRET_KEY", getLocalProperties("naverMapsApigwClientSecret"))
        buildConfigField( "String", "NAVER_CLIENT_ID_KEY", getLocalProperties("naverClientId"))
        buildConfigField( "String", "NAVER_CLIENT_SECRET_KEY", getLocalProperties("naverClientSecret"))

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
    buildTypes {
        val test = "\"TEST\""
        val release = "\"RELEASE\""

        debug {
            buildConfigField( "String", "FIREBASE", test)
        }
        create("qa") {
            buildConfigField( "String", "FIREBASE", release)
        }
        release {
            buildConfigField( "String", "FIREBASE", release)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }


}

kotlin {
    jvmToolchain(17)
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

fun getLocalProperties(key: String): String {
    val propertiesFile = File(rootProject.projectDir, "local.properties")
    val properties = Properties()

    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { properties.load(it) }
    } else {
        propertiesFile.createNewFile()
    }

    val defaultValue = "\"yourAppKey\""

    if (!properties.containsKey(key)) {
        properties[key] = defaultValue
        propertiesFile.outputStream().use { properties.store(it, null) }
    }
    return properties[key].toString()
}
