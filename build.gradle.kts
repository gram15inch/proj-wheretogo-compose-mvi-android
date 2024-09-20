// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id ("com.android.application") version "8.6.1" apply false
    id ("com.android.library") version "8.6.1" apply false
    id ("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id ("com.google.dagger.hilt.android") version "2.52" apply false
    id("de.mannodermaus.android-junit5") version "1.10.2.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.24" apply false
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}


