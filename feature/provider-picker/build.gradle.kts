plugins {
    alias(libs.plugins.wheretogo.android.library)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "com.dhkim139.feature.providerpicker"
}

dependencies {
    implementation(project(mapOf("path" to ":domain")))
    implementation(project(mapOf("path" to ":core:ui")))

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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
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

    implementation(libs.coil.compose)

}