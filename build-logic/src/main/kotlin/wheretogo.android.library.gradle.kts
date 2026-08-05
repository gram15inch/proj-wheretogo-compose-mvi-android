plugins {
    id("com.android.library")
}

android {
    compileSdk = 35

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