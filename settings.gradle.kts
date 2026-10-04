import java.io.FileInputStream
import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }
        maven { url = uri("https://repository.map.naver.com/archive/maven") }
        maven { url = uri("https://jitpack.io") }
    }
}

private val localProperties = Properties().apply {
    val file = File(rootDir, "local.properties")
    if (file.exists()) {
        load(FileInputStream(file))
    }
}

private  val modulesDir = localProperties.getProperty("modules.dir")
    ?: throw GradleException(
        "local.properties에 modules.dir 경로를 추가하세요. 예: modules.dir=/Users/Shared/modules"
    )


rootProject.name = "WhereTogo"
include(":app")
include(":data")
include(":domain")
include(":presentation")
include(":app-admin")
include(":core:ui")
include(":feature:media-picker")
include(":feature:provider-picker")
include(":feature:camera-picker")
include(":feature:course-add")
project(":feature:course-add").projectDir = File(modulesDir, "course-add")
