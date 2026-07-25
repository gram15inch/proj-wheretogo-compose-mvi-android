// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.devtools.ksp) apply false
    id("de.mannodermaus.android-junit5") version "1.11.2.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    kotlin("plugin.serialization") version "1.8.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}