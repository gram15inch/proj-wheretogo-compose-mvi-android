plugins {
    id("com.android.application")
}

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        targetSdk = 36
    }

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}