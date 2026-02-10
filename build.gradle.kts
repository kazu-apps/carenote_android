// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.roborazzi) apply false
}

// Detekt: Run via CLI (do NOT add as Gradle plugin — it conflicts with MockK instrumentation).
// Install: brew install detekt  (macOS) / scoop install detekt (Windows)
// Run:   detekt --config detekt.yml --input app/src/main/java
