plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}

kotlin {
    jvmToolchain(17)
}
