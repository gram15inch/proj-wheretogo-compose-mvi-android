
plugins {
    alias(libs.plugins.wheretogo.android.library)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "com.wheretogo.domain"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    // Kotlin (BOM)
    implementation(platform(libs.kotlin.bom))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)

    // Dagger / Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    // Libraries
    implementation(libs.firebase.geofire.android)
    implementation(libs.firebase.geofire.android.common)
    implementation(libs.sulky.ulid)
    implementation(libs.komoran)
    implementation(libs.timber)

    // Test
    testImplementation(libs.junit.jupiter)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}