// ===== SECTION: ROOT_PLUGINS =====
// Top-level build file. Plugins are declared here and applied in modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Pins the Kotlin Gradle Plugin version that AGP 9 built-in Kotlin uses. Never applied directly.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
}
// ===== END SECTION: ROOT_PLUGINS =====
