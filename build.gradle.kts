// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.android.junit5) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

val r8 by configurations.creating
dependencies {
    r8(libs.android.tools.r8)
}

tasks.register<JavaExec>("retrace") {
    classpath = r8
    mainClass.set("com.android.tools.r8.retrace.Retrace")
    args(
        "app/build/outputs/mapping/release/mapping.txt",
        "crash.txt"
    )
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}