// ===== SECTION: APP_PLUGINS =====
// AGP 9 compiles Kotlin with its built-in Kotlin support, so the
// org.jetbrains.kotlin.android plugin is intentionally not applied here.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}
// ===== END SECTION: APP_PLUGINS =====

// ===== SECTION: APP_ANDROID_CONFIG =====
android {
    namespace = "com.kaushalkr.axiom"
    // Compose 1.13 requires Android API 37.1 or later at compile time.
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.kaushalkr.axiom"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // Release-optimized build for on-device performance testing: same R8 shrinking and
        // baseline profile as release, non-debuggable, profileable by Perfetto/Android Studio,
        // and signed with the local debug key so it installs without a release keystore.
        create("perf") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
            isProfileable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
// ===== END SECTION: APP_ANDROID_CONFIG =====

// ===== SECTION: APP_DEPENDENCIES =====
dependencies {
    // Core Android and lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Navigation (type-safe routes) and persistence
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.datastore.preferences)

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Kyant0 Liquid Glass rendering
    implementation(libs.kyant.backdrop)
    implementation(libs.kyant.shapes)

    // JVM unit and Robolectric UI tests
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
// ===== END SECTION: APP_DEPENDENCIES =====

// ===== SECTION: APP_DIST_DEBUG_APK =====
// Every assembleDebug refreshes dist/axiom-<versionName>-debug.apk, the single installable
// debug build kept at the project root. Sync also removes stale debug APKs from older
// versions and keeps the performance APK.
val distDebugApkName = "axiom-${android.defaultConfig.versionName}-debug.apk"
val syncDebugApkToDist = tasks.register<Sync>("syncDebugApkToDist") {
    description = "Copies the latest debug APK to dist/$distDebugApkName."
    from(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
    into(rootProject.layout.projectDirectory.dir("dist"))
    // String-pattern rename keeps the task compatible with the configuration cache.
    rename("app-debug\\.apk", distDebugApkName)
    preserve { include("axiom-*-release-perf.apk") }
}
tasks.matching { it.name == "assembleDebug" }.configureEach {
    finalizedBy(syncDebugApkToDist)
}
// ===== END SECTION: APP_DIST_DEBUG_APK =====

// ===== SECTION: APP_DIST_PERF_APK =====
// Every assemblePerf refreshes dist/axiom-<versionName>-release-perf.apk, the optimized build
// for on-device performance testing, and keeps the debug APK.
val distPerfApkName = "axiom-${android.defaultConfig.versionName}-release-perf.apk"
val syncPerfApkToDist = tasks.register<Sync>("syncPerfApkToDist") {
    description = "Copies the latest perf APK to dist/$distPerfApkName."
    from(layout.buildDirectory.file("outputs/apk/perf/app-perf.apk"))
    into(rootProject.layout.projectDirectory.dir("dist"))
    rename("app-perf\\.apk", distPerfApkName)
    preserve { include("axiom-*-debug.apk") }
}
tasks.matching { it.name == "assemblePerf" }.configureEach {
    finalizedBy(syncPerfApkToDist)
}
// ===== END SECTION: APP_DIST_PERF_APK =====
