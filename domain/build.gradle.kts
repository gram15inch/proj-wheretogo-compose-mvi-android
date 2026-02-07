
plugins {
    id("wheretogo.android.library")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
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