plugins {
    id("com.android.library")
}

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(21)
}