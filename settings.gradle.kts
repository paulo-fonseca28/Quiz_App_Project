pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.5.2"
        kotlin("android") version "1.9.24"
        kotlin("kapt") version "1.9.24"
        id("com.google.dagger.hilt.android") version "2.51"
        id("com.google.gms.google-services") version "4.4.3"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "QuizAppStarter"
include(":app")
