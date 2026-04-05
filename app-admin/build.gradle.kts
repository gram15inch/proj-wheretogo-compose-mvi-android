import java.util.Properties
import kotlin.toString

plugins {
    id("wheretogo.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.dhkim139.admin.wheretogo"

    signingConfigs {
        create("release") {
            keyAlias = getUploadKey("keyAlias")
            keyPassword = getUploadKey("keyPassword")
            storeFile = File(getUploadKey("storeFile"))
            storePassword = getUploadKey("storePassword")
        }
    }

    defaultConfig {
        applicationId = "com.dhkim139.admin.wheretogo"

        versionCode = 1
        versionName = "1.0"

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
            buildConfigField("String",  "GOOGLE_WEB_CLIENT_ID_KEY", getLocalProperties("googleStagingWebClientId"))
        }

        release {// 앱 설정 변경용
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID_KEY", getLocalProperties("googleWebClientId"))
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
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
    implementation(libs.androidx.core.splashscreen)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Firebase (BOM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
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

    // Retrofit / Moshi
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.moshi)
    implementation(libs.squareup.moshi.kotlin)

    // Google
    implementation(libs.google.identity.googleid)
    implementation(libs.google.play.services.location)
    implementation(libs.google.material)

    // Other
    implementation(libs.coil.compose)
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

