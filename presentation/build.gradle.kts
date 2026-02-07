import java.util.Properties

plugins {
    id("wheretogo.android.library")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.wheretogo.presentation"

    defaultConfig {
        manifestPlaceholders["adsMobAppId"] = getLocalProperties("adsMobAppId")
    }
}

dependencies {
    implementation(project(mapOf("path" to ":domain")))

    // AndroidX
    implementation(libs.androidx.core.ktx)

    // BOM
    implementation(platform(libs.kotlin.bom))
    implementation(platform(libs.androidx.compose.bom))

    // Kotlin
    implementation(libs.kotlinx.coroutines.play.services)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // AndroidX
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.browser)
    ksp(libs.androidx.hilt.compiler)

    // Dagger / Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.dagger.hilt.android.compiler)

    // Retrofit
    implementation(libs.squareup.retrofit)

    // Google
    implementation(libs.google.identity.googleid)
    implementation(libs.google.play.services.location)
    implementation(libs.google.play.services.ads)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    // Libraries
    implementation(libs.naver.maps)
    api(files("libs/com.skt.Tmap_1.76.jar"))
    implementation(libs.lottie.compose)
    implementation(libs.landscapist.glide)
    implementation(libs.compose.shimmer)
    implementation(libs.timber)
}

fun getLocalProperties(key: String): String{
    val localPropertiesFile = File(rootProject.projectDir, "local.properties")
    val properties = Properties()

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }

    return properties[key].toString()
}
