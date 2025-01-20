import wheretogo.AndroidX
import wheretogo.Firebase
import wheretogo.Dagger
import wheretogo.Kotlin
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

    defaultConfig {
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

    implementation(AndroidX.CORE_KTX)
    api(AndroidX.ROOM_KTX)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    //bom
    implementation(platform(Kotlin.KOTLIN_BOM))

    //hilt
    implementation(Dagger.HILT_ANDROID)
    ksp(Dagger.HILT_COMPILER)

    //retrofit
    api(Squareup.RETROFIT)
    api(Squareup.RETROFIT_CONVERTER_MOSHI)
    api(Squareup.MOSHI_KOTLIN)


    //Room
    api(AndroidX.ROOM_RUNTIME)
    ksp(AndroidX.ROOM_COMPILER)

    implementation(platform(Firebase.FIREBASE_BOM))
    implementation(Firebase.FIREBASE_DATABASE)
    implementation(Firebase.FIREBASE_FIRESTORE_KTX)
    implementation(Firebase.FIREBASE_STORAGE_KTX)

    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
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
