import org.gradle.api.tasks.testing.logging.TestLogEvent

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id ("com.android.application") version "8.5.2" apply false
    id ("com.android.library") version "8.5.2" apply false
    id ("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id ("com.google.dagger.hilt.android") version "2.44" apply false
    id("de.mannodermaus.android-junit5") version "1.10.2.0" apply false
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}


