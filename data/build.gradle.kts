plugins {
    alias(libs.plugins.wheretogo.android.library)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "com.wheretogo.data"
    defaultConfig {
        consumerProguardFiles(
            "consumer-default-rules.pro",
            "consumer-retrofit-rules.pro"
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
}

dependencies {
    implementation(project(mapOf("path" to ":domain")))

    // Kotlin (BOM)
    implementation(platform(libs.kotlin.bom))

    // Kotlin
    implementation(libs.kotlinx.serialization.cbor)
    implementation(libs.kotlinx.serialization.json)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Dagger / Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    // Retrofit / Moshi
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.kotlinx.serialization)

    // Firebase (BOM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)

    // Libraries
    implementation(libs.google.identity.googleid)
    implementation(libs.sulky.ulid)
    implementation(libs.timber)
    implementation(libs.coil.compose)
}