plugins {
    // Versions are supplied via settings.gradle.kts pluginManagement
    id("com.android.application") apply false
    kotlin("android") apply false
    kotlin("kapt") apply false
    id("com.google.dagger.hilt.android") apply false
    id("com.google.gms.google-services") apply false
}
